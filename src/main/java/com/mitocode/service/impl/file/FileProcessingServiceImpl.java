package com.mitocode.service.impl.file;

import com.mitocode.config.FileUploadConfig;
import com.mitocode.model.file.FileEntity;
import com.mitocode.model.user.User;
import com.mitocode.model.user.UserConfig;
import com.mitocode.repo.file.IFileUploadRepo;
import com.mitocode.repo.user.IUserRepo;
import com.mitocode.security.JwtTokenUtil;
import com.mitocode.service.file.IFileProcessingService;
import com.mitocode.service.file.IFileUploadService;
import com.mitocode.service.impl.user.UserConfigServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


@Service
@RequiredArgsConstructor
public class FileProcessingServiceImpl implements IFileProcessingService {

    @Value("${s3.bucket.name}")
    private String BUCKET;

    @Lazy
    @Autowired
    private IFileUploadService fileUploadService;
    private final IFileUploadRepo fileRepository;
    private final IUserRepo userService;
    private final JwtTokenUtil jwtTokenUtil;
    private final FileUploadConfig fileUploadConfig;
    private final UserConfigServiceImpl userConfigServiceImpl;

    // Metodo para procesar un archivo ZIP
    @Override
    public List<String> processZipFile(MultipartFile zipFile) throws IOException {
        String username = jwtTokenUtil.getAuthenticatedUsername();

        Optional<User> optionalUser = userService.findByUsername(username);
        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("El usuario no existe en la base de datos");
        }

        User user = optionalUser.get();

        Optional<UserConfig> optionalUserConfig = userConfigServiceImpl.getUserConfigByUsername(username);
        if (optionalUserConfig.isEmpty()) {
            throw new IllegalArgumentException("No se encontró configuración para el usuario");
        }

        UserConfig userConfig = optionalUserConfig.get();
        String storageType = userConfig.getStorageType(); // 's3' o 'local'

        List<String> resultMessages = new ArrayList<>();

        File tempDir = new File(System.getProperty("java.io.tmpdir"), "decompressed_" + System.currentTimeMillis());
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

        try (ZipInputStream zis = new ZipInputStream(zipFile.getInputStream())) {
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    File tempFile = new File(tempDir, entry.getName());
                    tempFile.getParentFile().mkdirs();
                    Files.copy(zis, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                    MultipartFile decompressedFile = convertToMultipartFile(tempFile);
                    String fileExtension = getFileExtension(decompressedFile.getOriginalFilename());
                    String fileType = determineFileType(fileExtension);
                    String fullFilePath = buildFilePath(decompressedFile.getOriginalFilename(), fileType, username);

                    // Si es tipo "invoice", validar el código de factura
                    if ("pdf".equalsIgnoreCase(fileType) || "xml".equalsIgnoreCase(fileType)) {
                        try {
                            extractInvoiceCode(decompressedFile.getOriginalFilename());
                        } catch (Exception e) {
                            resultMessages.add("Archivo ignorado por código de factura inválido: " + decompressedFile.getOriginalFilename());
                            continue; // Saltar este archivo
                        }
                    }

                    boolean exists = fileRepository.existsByFileNameAndFileTypeAndUsers_Username(decompressedFile.getOriginalFilename(), fileType, username);
                    if (exists) {
                        resultMessages.add("Archivo duplicado omitido: " + decompressedFile.getOriginalFilename());
                        continue;
                    }

                    if ("s3".equalsIgnoreCase(storageType)) {
                        fileUploadService.uploadToS3(new ByteArrayInputStream(decompressedFile.getBytes()), fullFilePath, decompressedFile.getSize(), decompressedFile.getContentType());
                    } else {
                        fileUploadService.uploadFileToLocal(decompressedFile, username, fileType);
                    }

                    FileEntity fileEntity = new FileEntity();
                    fileEntity.setFileName(decompressedFile.getOriginalFilename());
                    fileEntity.setStorageType(storageType);
                    fileEntity.setFileType(fileType);
                    fileEntity.setFileUrl("s3".equalsIgnoreCase(storageType) ? "https://" + BUCKET + ".s3.amazonaws.com/" + fullFilePath : fileUploadConfig.getUploadDir() + fullFilePath);
                    fileEntity.setUsers(List.of(user));

                    fileRepository.save(fileEntity);

                    resultMessages.add("Archivo dentro del ZIP procesado: " + decompressedFile.getOriginalFilename());
                }
                zis.closeEntry();
            }
        } finally {
            deleteDirectoryRecursively(tempDir);
        }

        return resultMessages;
    }

    private void deleteDirectoryRecursively(File dir) {
        if (dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                deleteDirectoryRecursively(file);
            }
        }
        dir.delete();
    }

    //Metodo para convertir el zip descomprimido a multipart
    @Override
    public MultipartFile convertToMultipartFile(File file) throws IOException {
        return new MockMultipartFile(
                file.getName(),
                file.getName(),
                Files.probeContentType(file.toPath()),
                Files.readAllBytes(file.toPath())
        );
    }

    //Metodo que construye la ruta completa del archivo basada en el tipo y usuario.
    @Override
    public String buildFilePath(String originalFileName, String fileType, String username) {
        // Obtener la ruta adecuada según el tipo de archivo
        String folderPath = getFolderByFileType(fileType, username);

        // Concatenar la ruta con el nombre del archivo
        return folderPath + "/" + originalFileName;
    }

    // Metodo para obtener la extensión del archivo
    @Override
    public String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1).toLowerCase();
    }

    // Metodo para determinar el tipo de archivo por la extensión
    @Override
    public String determineFileType(String fileExtension) {
        return switch (fileExtension) {
            case "png", "jpeg", "jpg" -> "image";
            case "pdf" -> "pdf";
            case "xml" -> "xml";
            case "zip" -> "zip";
            default -> throw new IllegalArgumentException("Tipo de archivo no soportado: " + fileExtension);
        };
    }

    // Obtener la carpeta según el tipo de archivo
    @Override
    public String getFolderByFileType(String fileType, String username) {
        return switch (fileType) {
            case "image" -> "images/" + username;
            case "pdf" -> "invoices/pdf/" + username;
            case "xml" -> "invoices/xml/" + username;
            default -> throw new IllegalArgumentException("Tipo de archivo no soportado para crear carpeta");
        };
    }

    // Metodo para extraer el código de factura desde el nombre del archivo
    @Override
    public String extractInvoiceCode(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            throw new IllegalArgumentException("Nombre de archivo inválido: " + fileName);
        }

        // Quitar la extensión del archivo
        int lastDotIndex = fileName.lastIndexOf('.');
        String coreName = fileName.substring(0, lastDotIndex);

        // Extraer solo los números
        String invoiceCode = coreName.replaceAll("\\D", ""); // Reemplaza todo lo que NO sea dígito

        if (invoiceCode.isEmpty()) {
            throw new IllegalArgumentException("No se encontraron números en el nombre del archivo: " + fileName);
        }

        return invoiceCode;
    }

}

