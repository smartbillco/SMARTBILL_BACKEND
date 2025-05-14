package com.mitocode.service.impl.invoice;

import com.mitocode.model.file.Invoice;
import com.mitocode.model.user.User;
import com.mitocode.repo.IGenericRepo;
import com.mitocode.repo.user.IUserRepo;
import com.mitocode.repo.correspondence.InvoiceRepo;
import com.mitocode.service.invoice.InvoiceService;
import com.mitocode.service.impl.CRUDImpl;
import com.mitocode.util.ApiResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.nio.file.Files;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.StandardCopyOption;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl extends CRUDImpl<Invoice, Integer> implements InvoiceService {

    private final InvoiceRepo invoiceRepo;
    private final IUserRepo userRepository;

    public ApiResponseUtil<String> processXMLFiles(List<MultipartFile> xmlFiles) {
        try {
            // Obtener el usuario autenticado
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado: " + username));

            for (MultipartFile xmlFile : xmlFiles) {
                if (xmlFile.isEmpty()) {
                    return new ApiResponseUtil<>(false, "Archivo vacío: " + xmlFile.getOriginalFilename(), null);
                }

                // Convertir MultipartFile a File temporal
                File tempFile = File.createTempFile("invoice_", ".xml");
                Files.copy(xmlFile.getInputStream(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                // Procesar el archivo XML
                ApiResponseUtil<String> response = processXMLFile(tempFile, user);

                // Eliminar archivo temporal después del procesamiento
                tempFile.delete();

                // Si hubo un error en el procesamiento, devolver la respuesta de error
                if (!response.isSuccess()) {
                    return response;
                }
            }

            return new ApiResponseUtil<>(true, "Todos los archivos procesados correctamente.", null);

        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponseUtil<>(false, "Error al procesar los archivos XML: " + e.getMessage(), null);
        }
    }

    public ApiResponseUtil<String> processXMLFile(File xmlFile, User user) {
        try {
            // Preparar el parser
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlFile);

            // XPath para extraer datos
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xPath = xPathFactory.newXPath();

            XPathExpression countryExpr = xPath.compile("//*[local-name()='dDistr' or local-name()='DocumentCurrencyCode' or local-name()='IdentificationCode']");
            XPathExpression codeExpr = xPath.compile("//*[local-name()='ParentDocumentID' or local-name()='dId' or local-name()='ID']");

            Node countryNode = (Node) countryExpr.evaluate(document, XPathConstants.NODE);
            String country = countryNode != null ? countryNode.getTextContent().trim() : "No encontrado";

            Node codeNode = (Node) codeExpr.evaluate(document, XPathConstants.NODE);
            String code = codeNode != null ? codeNode.getTextContent().trim() : "No encontrado";

            // Validar que el código no sea nulo ni vacío
            if (code.isEmpty() || "No encontrado".equals(code)) {
                return new ApiResponseUtil<>(false, "Código de factura no encontrado. Archivo no guardado.", null);
            }

            // Normalizar código
            String cleanCode = code.trim();

            // Verificar si ya existe una factura con este código para el usuario
            boolean exists = invoiceRepo.existsByUserAndInvoiceCode(user, cleanCode);
            if (exists) {
                return new ApiResponseUtil<>(false, "Factura con código '" + cleanCode + "' ya ha sido procesada.", null);
            }

            // Determinar el país
            String fileCountry = determineCountry(country);
            String xmlContent = Files.readString(xmlFile.toPath());

            // Verificar si el país es válido antes de guardar
            if ("Desconocido".equals(fileCountry)) {
                return new ApiResponseUtil<>(false, "No se pudo identificar el país. Archivo no guardado.", null);
            }

            // Guardar en base de datos
            Invoice xmlFileEntity = new Invoice();
            xmlFileEntity.setXml(xmlContent);
            xmlFileEntity.setCountry(fileCountry);
            xmlFileEntity.setInvoiceCode(cleanCode);
            xmlFileEntity.setUser(user);
            invoiceRepo.save(xmlFileEntity);

            return new ApiResponseUtil<>(true, "Archivo procesado y guardado en la base de datos correctamente.", null);

        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponseUtil<>(false, "Error al procesar el archivo: " + e.getMessage(), null);
        }
    }

    private String determineCountry(String countryCode) {
        return switch (countryCode.toUpperCase()) {
            case "CO" -> "Colombia";
            case "PEN" -> "Peru";
            case "Panama" -> "Panama";
            default -> "Desconocido";
        };
    }

    @Override
    protected IGenericRepo<Invoice, Integer> getRepo() {
        return invoiceRepo;
    }
}

