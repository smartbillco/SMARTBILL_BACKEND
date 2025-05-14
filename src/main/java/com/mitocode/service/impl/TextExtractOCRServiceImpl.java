package com.mitocode.service.impl;

import com.mitocode.service.ITextExtractOCRService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TextExtractOCRServiceImpl implements ITextExtractOCRService {

    private final Tesseract tesseract;

    //=============================================== ENDPOINT 1: Extraer texto de imagen ===============================================
    @Override
    public List<String> extractTextFromImage(MultipartFile imageFile) throws IOException, TesseractException {
        validateImageFile(imageFile);

        BufferedImage image = ImageIO.read(imageFile.getInputStream());
        if (image == null) {
            throw new IOException("El archivo no es una imagen válida");
        }

        BufferedImage processedImage = preprocessInvoiceImage(image);
        String uniqueFilename = "processed_" + System.currentTimeMillis() + ".png";
        //saveProcessedImage(processedImage, uniqueFilename);

        try {
            String ocrResult = tesseract.doOCR(processedImage);
            return cleanAndSplitText(ocrResult);
        } catch (TesseractException e) {
            log.error("Error en OCR para imagen: {}", e.getMessage());
            throw new TesseractException("Error al procesar imagen con OCR: " + e.getMessage());
        }
    }

    private void saveProcessedImage(BufferedImage image, String filename) {
        try {
            File outputDir = new File("C:/ocr-output/");
            if (!outputDir.exists()) {
                outputDir.mkdirs(); // crea la carpeta si no existe
            }
            File outputFile = new File(outputDir, filename);
            ImageIO.write(image, "png", outputFile);
            log.info("Imagen procesada guardada en: {}", outputFile.getAbsolutePath());
        } catch (IOException e) {
            log.error("Error al guardar la imagen procesada", e);
        }
    }

    //=============================================== ENDPOINT 2: Extraer texto de PDF ===============================================
    @Override

    public List<String> extractTextFromPdf(MultipartFile pdfFile) throws IOException, TesseractException {
        validatePdfFile(pdfFile);

        try (InputStream inputStream = pdfFile.getInputStream();
             PDDocument document = PDDocument.load(inputStream)) {

            PDFTextStripper stripper = new PDFTextStripper();

            // Configuración para mejorar la extracción
            stripper.setSortByPosition(true);
            stripper.setLineSeparator("\n");
            stripper.setParagraphStart("");
            stripper.setParagraphEnd("\n");
            stripper.setShouldSeparateByBeads(true);

            String text;
            if (isScannedPdf(document)) {
                text = processPdfAsImage(document);
            } else {
                text = stripper.getText(document);
            }

            // Procesamiento adicional para mantener la estructura
            return cleanAndOrganizePdfText(text);
        }
    }

    //=============================================== ENDPOINT 3: Extraer campos de imagen de factura ===============================================
    @Override
    public Map<String, String> extractInvoiceFieldsFromImage(MultipartFile imageFile) throws IOException, TesseractException {
        List<String> textLines = extractTextFromImage(imageFile);
        String fullText = String.join("\n", textLines);
        return extractInvoiceFields(fullText);
    }

    //=============================================== ENDPOINT 4: Extraer campos de PDF de factura ===============================================
    @Override
    public Map<String, String> extractInvoiceFieldsFromPdf(MultipartFile pdfFile) throws IOException, TesseractException {
        List<String> textLines = extractTextFromPdf(pdfFile);
        String fullText = String.join("\n", textLines);
        return extractInvoiceFields(fullText);
    }

    //=============================================== MÉTODOS AUXILIARES PRIVADOS ===============================================

    private List<String> cleanAndSplitText(String text) {
        return Arrays.stream(text.split("\n"))
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .collect(Collectors.toList());
    }

    private List<String> cleanAndOrganizePdfText(String text) {
        // Dividir por líneas y limpiar
        String[] lines = text.split("\\r?\\n");
        List<String> cleanedLines = new ArrayList<>();

        for (String line : lines) {
            // Eliminar espacios múltiples y limpiar la línea
            String cleanedLine = line.trim()
                    .replaceAll("\\s+", " ")
                    .replaceAll("\\s+(?=:)", ":") // Eliminar espacios antes de dos puntos
                    .replaceAll("(?<=\\d)\\s+(?=\\d)", ""); // Unir números separados por espacios

            if (!cleanedLine.isEmpty()) {
                cleanedLines.add(cleanedLine);
            }
        }

        return cleanedLines;
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo no puede ser nulo o vacío");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("El archivo debe ser una imagen (JPEG, PNG, etc.)");
        }
    }

    private void validatePdfFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo no puede ser nulo o vacío");
        }

        if (!"application/pdf".equals(file.getContentType())) {
            throw new IllegalArgumentException("El archivo debe ser un PDF");
        }
    }

    //===============================================PROCESAMIENTO DE IMAGEN ===============================================

    private BufferedImage preprocessInvoiceImage(BufferedImage original) {
        // 1. Convertir a escala de grises con mejor fórmula
        BufferedImage grayImage = new BufferedImage(
                original.getWidth(),
                original.getHeight(),
                BufferedImage.TYPE_BYTE_GRAY);

        for (int y = 0; y < original.getHeight(); y++) {
            for (int x = 0; x < original.getWidth(); x++) {
                int rgb = original.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                // Fórmula mejorada para preservar detalles
                int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                grayImage.setRGB(x, y, (gray << 16) | (gray << 8) | gray);
            }
        }

        // 2. Binarización adaptativa con control de grosor
        BufferedImage binary = new BufferedImage(
                grayImage.getWidth(),
                grayImage.getHeight(),
                BufferedImage.TYPE_BYTE_BINARY);

        int threshold = calculateOtsuThreshold(grayImage);
        // Ajuste fino del umbral (reduce el grosor de letras)
        threshold = (int) (threshold * 0.9); // <-- Ajusta este valor

        for (int y = 0; y < grayImage.getHeight(); y++) {
            for (int x = 0; x < grayImage.getWidth(); x++) {
                int gray = grayImage.getRGB(x, y) & 0xFF;
                binary.setRGB(x, y, gray > threshold ? 0xFFFFFF : 0x000000);
            }
        }

        return binary;
    }

    private int calculateOtsuThreshold(BufferedImage image) {
        // Implementación directa del metodo Otsu (igual a tu versión original)
        int[] histogram = new int[256];
        int total = image.getWidth() * image.getHeight();

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int gray = (int) (0.299 * ((rgb >> 16) & 0xFF) +
                        0.587 * ((rgb >> 8) & 0xFF) +
                        0.114 * (rgb & 0xFF));
                histogram[gray]++;
            }
        }

        float sum = 0;
        for (int i = 0; i < 256; i++) sum += i * histogram[i];

        float sumB = 0;
        int wB = 0;
        int wF = 0;
        float varMax = 0;
        int threshold = 0;

        for (int i = 0; i < 256; i++) {
            wB += histogram[i];
            if (wB == 0) continue;
            wF = total - wB;
            if (wF == 0) break;

            sumB += i * histogram[i];
            float mB = sumB / wB;
            float mF = (sum - sumB) / wF;
            float varBetween = (float) wB * wF * (mB - mF) * (mB - mF);

            if (varBetween > varMax) {
                varMax = varBetween;
                threshold = i;
            }
        }

        return threshold;
    }

    //========================================================================================================

    private boolean isScannedPdf(PDDocument document) throws IOException {
        PDFTextStripper stripper = new PDFTextStripper();
        String text = stripper.getText(document);
        return text.trim().length() < 50 ||
                text.chars().filter(c -> !Character.isWhitespace(c)).count() < 50;
    }

    private String processPdfAsImage(PDDocument document) throws IOException, TesseractException {
        PDFRenderer renderer = new PDFRenderer(document);
        StringBuilder text = new StringBuilder();

        for (int page = 0; page < document.getNumberOfPages(); page++) {
            BufferedImage image = renderer.renderImageWithDPI(page, 300, ImageType.RGB);
            BufferedImage processedImage = preprocessInvoiceImage(image);
            text.append(tesseract.doOCR(processedImage)).append("\n");
        }

        return text.toString();
    }

    // =============================================== METODO PARA EXTRAER CAMPOS DE FACTURA ===============================================
    private Map<String, String> extractInvoiceFields(String text) {
        Map<String, String> fields = new LinkedHashMap<>();
        String cleanedText = cleanInvoiceText(text);

        // Inicializar todos los campos con "NO_ENCONTRADO"
        fields.put("CUFE", "NO_ENCONTRADO");
        fields.put("NIT", "NO_ENCONTRADO");
        fields.put("FECHA", "NO_ENCONTRADO");
        fields.put("TOTAL", "NO_ENCONTRADO");

        // Extraer cada campo
        extractCufe(cleanedText, fields);
        extractNit(cleanedText, fields);
        extractDate(cleanedText, fields);
        extractTotalAmount(cleanedText, fields);

        return fields;
    }

    // =============================================== METODO MEJORADO PARA EXTRAER CUFE ===============================================

    private void extractCufe(String text, Map<String, String> fields) {
        try {
            if (text == null || text.isEmpty()) {
                fields.put("CUFE", "NO_ENCONTRADO");
                return;
            }

            // Patrón optimizado para CUFE de facturación electrónica colombiana
            Pattern pattern = Pattern.compile(
                    "(?:Código Único de Factura - CUFE|CUFE|cufe)[\\s:]*\\s*([a-f0-9]{150})|([a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12})",
                    Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
            );

            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                // Usar el primer grupo que coincida (sin guiones o con guiones)
                String cufe = matcher.group(1) != null ? matcher.group(1) : matcher.group(2).replace("-", "");

                if (cufe != null && !cufe.isEmpty()) {
                    fields.put("CUFE", cufe.toLowerCase());
                    return;
                }
            }

            // Si no se encontró con el patrón principal, buscar cualquier cadena hexadecimal larga
            Pattern fallbackPattern = Pattern.compile("\\b([a-f0-9]{60,96})\\b");
            matcher = fallbackPattern.matcher(text);
            if (matcher.find()) {
                fields.put("CUFE", matcher.group(1).toLowerCase());
                return;
            }

            fields.put("CUFE", "NO_ENCONTRADO");

        } catch (Exception e) {
            log.error("Error al extraer CUFE: {}", e.getMessage());
            fields.put("CUFE", "ERROR_EN_PROCESO");
        }
    }

    private void extractNit(String text, Map<String, String> fields) {
        try {
            if (text == null || text.isEmpty()) {
                fields.put("NIT", "NO_ENCONTRADO");
                return;
            }

            // Normalizar texto (conservar saltos de línea para contextos específicos)
            String normalizedText = text.replaceAll("\\s+", " ");

            // Patrón maestro que cubre todos los formatos observados
            Pattern masterPattern = Pattern.compile(
                    "(?:(?:NIT|Nit|BIC\\s+NIT)\\s*[:]?\\s*|Nit\\s+del\\s+Emisor\\s*[:]?\\s*)" +
                            "([0-9]{1,3}[\\s.]?[0-9]{3}[\\s.]?[0-9]{3}-?[0-9]?)" +
                            "(?=\\s|$|\\n|[^0-9-])",
                    Pattern.CASE_INSENSITIVE
            );

            Matcher matcher = masterPattern.matcher(normalizedText);
            if (matcher.find()) {
                String nit = formatNit(matcher.group(1));
                if (isValidNit(nit)) {
                    fields.put("NIT", nit);
                    return;
                }
            }

            // Búsqueda más agresiva como último recurso
            Pattern fallbackPattern = Pattern.compile(
                    "\\b([0-9]{1,3}[\\s.-][0-9]{3}[\\s.-][0-9]{3}-?[0-9]?)\\b"
            );

            matcher = fallbackPattern.matcher(normalizedText);
            while (matcher.find()) {
                String nit = formatNit(matcher.group(1));
                if (isValidNit(nit)) {
                    fields.put("NIT", nit);
                    return;
                }
            }

            fields.put("NIT", "no_encontrado");
        } catch (Exception e) {
            log.error("Error al extraer NIT: {}", e.getMessage());
            fields.put("NIT", "error_en_proceso");
        }
    }

    private String formatNit(String rawNit) {
        // Limpiar y formatear consistentemente
        String cleanNit = rawNit.replaceAll("[^0-9]", "");

        // Aplicar formato estándar colombiano
        if (cleanNit.length() == 9) {
            return cleanNit.replaceAll("^(\\d{1,3})(\\d{3})(\\d{3})$", "$1.$2.$3");
        } else if (cleanNit.length() == 10) {
            return cleanNit.replaceAll("^(\\d{1,3})(\\d{3})(\\d{3})(\\d)$", "$1.$2.$3-$4");
        }
        return cleanNit; // Para otros casos (aunque no debería ocurrir con validación)
    }

    private boolean isValidNit(String nit) {
        // Validar longitud y formato básico
        String cleanNit = nit.replaceAll("[^0-9]", "");
        return cleanNit.length() >= 8 && cleanNit.length() <= 10;
    }

    private void extractDate(String text, Map<String, String> fields) {
        try {
            if (text == null || text.isEmpty()) {
                fields.put("FECHA", "no_encontrado");
                return;
            }

            // Patrones optimizados para facturas electrónicas colombianas
            Pattern[] patterns = {
                    // 1. Nuevo patrón para formato "08-Ene-2025"
                    Pattern.compile("FECHA\\s+DE\\s+FACTURACI[ÓO]N\\s*[:]?\\s*(\\d{2}-(Ene|Feb|Mar|Abr|May|Jun|Jul|Ago|Sep|Oct|Nov|Dic)-\\d{4})", Pattern.CASE_INSENSITIVE),

                    // 2. Formato ISO (2025-02-12) con prefijo "Fecha Emisión:"
                    Pattern.compile("Fecha\\s+Emisi[óo]n\\s*:\\s*(\\d{4}-\\d{2}-\\d{2})"),

                    // 3. Formato ISO cerca de "validación"
                    Pattern.compile("(\\d{4}-\\d{2}-\\d{2})(?=\\s+\\d{2}:\\d{2}:\\d{2})"),

                    // 4. Formato tradicional colombiano (dd/mm/aaaa)
                    Pattern.compile("\\b(\\d{1,2}[/-]\\d{1,2}[/-]\\d{4})\\b"),

                    // 5. Para documentos con múltiples fechas (toma la primera)
                    Pattern.compile("(\\d{4}[/-]\\d{2}[/-]\\d{2})")
            };

            for (Pattern pattern : patterns) {
                Matcher matcher = pattern.matcher(text);
                if (matcher.find()) {
                    String fecha = matcher.group(1);

                    // Convertir formato "08-Ene-2025" a "08/01/2025" si es el caso
                    if (fecha.matches("\\d{2}-[A-Za-z]{3}-\\d{4}")) {
                        fecha = convertMonthAbbreviationToNumber(fecha);
                    } else {
                        fecha = fecha.replace("-", "/") // Estandarizar separadores
                                .replaceAll("^(\\d{4})/(\\d{2})/(\\d{2})$", "$3/$2/$1"); // Formato dd/mm/aaaa
                    }

                    fields.put("FECHA", fecha);
                    return;
                }
            }

            fields.put("FECHA", "no_encontrado");
        } catch (Exception e) {
            log.error("Error al extraer fecha: {}", e.getMessage());
            fields.put("FECHA", "error_en_proceso");
        }
    }

    // Metodo auxiliar para convertir abreviaturas de mes a número
    private String convertMonthAbbreviationToNumber(String fecha) {
        String[] partes = fecha.split("-");
        String mes = partes[1].toLowerCase();

        Map<String, String> meses = Map.ofEntries(
                Map.entry("ene", "01"),
                Map.entry("feb", "02"),
                Map.entry("mar", "03"),
                Map.entry("abr", "04"),
                Map.entry("may", "05"),
                Map.entry("jun", "06"),
                Map.entry("jul", "07"),
                Map.entry("ago", "08"),
                Map.entry("sep", "09"),
                Map.entry("oct", "10"),
                Map.entry("nov", "11"),
                Map.entry("dic", "12")
        );

        return partes[0] + "/" + meses.get(mes) + "/" + partes[2];
    }

    private void extractTotalAmount(String text, Map<String, String> fields) {
        try {
            if (text == null || text.isEmpty()) {
                fields.put("TOTAL", "NO_ENCONTRADO");
                return;
            }

            String normalizedText = text.replace("\n", " ").replaceAll("\\s+", " ");

            Pattern[] patterns = {
                    Pattern.compile("(VALOR|TOTAL)\\s+A\\s+PAGAR\\s*[:]?\\s*([\\d.,]+)\\s*COP", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("TOTAL\\s*:\\s*([\\d.,]+\\d{2})(?=\\s*\\n|\\s*TOTAL en letras)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE),
                    Pattern.compile("TOTAL\\s*[:=]\\s*([\\d.,]+)(?=\\s|$)"),
                    Pattern.compile("Total\\s+neto\\s+factura\\s*\\(?=\\)?\\s*([\\d.,]+)", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("Total\\s+a\\s+pagar\\s*(?:servicio)?\\s*[:]?\\s*\\$?\\s*([\\d.,]+)", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("TOTAL\\s+PAGADO\\s*[:=]?\\s*([\\d.,]+)"),
                    Pattern.compile("TOTAL\\s+FACTURA\\s*[:=]?\\s*([\\d.,]+)"),
                    Pattern.compile("IMPORTE\\s+TOTAL\\s*[:=]?\\s*([\\d.,]+)"),
                    Pattern.compile("TOTAL\\s+GENERAL\\s*[:=]?\\s*([\\d.,]+)"),
                    Pattern.compile("TOTAL\\s*\\$\\s*([\\d.,]+)")
            };

            for (Pattern pattern : patterns) {
                Matcher matcher = pattern.matcher(normalizedText);
                if (matcher.find()) {
                    String rawAmount = matcher.group(matcher.groupCount());
                    String formattedAmount = normalizeAmount(rawAmount);
                    fields.put("TOTAL", formattedAmount);
                    return;
                }
            }

            // Si no se encontró, buscar el número más grande
            List<String> montos = new ArrayList<>();
            Pattern numberPattern = Pattern.compile("\\b\\d{1,3}(?:[.,]\\d{3})*(?:[.,]\\d{2})?\\b");
            Matcher matcher = numberPattern.matcher(normalizedText);

            while (matcher.find()) {
                String rawMonto = matcher.group();
                String formattedMonto = normalizeAmount(rawMonto);
                montos.add(formattedMonto);
            }

            if (!montos.isEmpty()) {
                montos.sort((a, b) -> Double.compare(parseDouble(b), parseDouble(a)));
                fields.put("TOTAL", montos.get(0));
            } else {
                fields.put("TOTAL", "NO_ENCONTRADO");
            }

        } catch (Exception e) {
            log.error("Error al extraer total: {}", e.getMessage());
            fields.put("TOTAL", "ERROR_EN_PROCESO");
        }
    }

    private String normalizeAmount(String amount) {
        if (amount == null || amount.isEmpty()) {
            return "0,00";
        }

        amount = amount.trim();

        try {
            // Paso 1: detectar el número correctamente
            double value = parseDouble(amount);

            // Paso 2: formatearlo a formato europeo
            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            symbols.setDecimalSeparator(',');
            symbols.setGroupingSeparator('.');

            DecimalFormat europeanFormat = new DecimalFormat("#,##0.00", symbols);

            return europeanFormat.format(value);
        } catch (Exception e) {
            return "0,00";
        }
    }

    private double parseDouble(String amount) {
        if (amount == null || amount.isEmpty()) {
            return 0.0;
        }

        // Paso extra: eliminar apóstrofes
        amount = amount.replace("'", "");

        if (amount.contains(",") && amount.contains(".")) {
            if (amount.lastIndexOf(",") > amount.lastIndexOf(".")) {
                amount = amount.replace(".", "").replace(",", ".");
            } else {
                amount = amount.replace(",", "");
            }
        } else if (amount.contains(",")) {
            amount = amount.replace(".", "").replace(",", ".");
        } else {
            amount = amount.replace(",", "");
        }
        return Double.parseDouble(amount);
    }


    private String cleanInvoiceText(String text) {
        if (text == null) return "";

        return text.replaceAll("[‘’´`]", "'")
                .replaceAll("[“”]", "\"")
                .replaceAll("\\s+", " ")
                .replaceAll("(?i)(nit|n1t|n!t)", "NIT")
                .replaceAll("(?i)(cufe|cuFe|eufe)", "CUFE")
                .replaceAll("([0-9])O", "$10")
                .replaceAll("([0-9])I", "$11")
                .replaceAll("([0-9])l", "$11")
                .replaceAll("([0-9])[|]", "$11")
                .replaceAll("([0-9])[\\\\]", "$11")
                .trim();
    }
}