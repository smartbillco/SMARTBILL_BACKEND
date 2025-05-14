package com.mitocode.service.impl.file;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.mitocode.config.FileUploadConfig;
import com.mitocode.exception.ModelNotFoundException;
import com.mitocode.model.file.FileEntity;
import com.mitocode.model.user.User;
import com.mitocode.model.user.UserConfig;
import com.mitocode.repo.file.IFileUploadRepo;
import com.mitocode.repo.IGenericRepo;
import com.mitocode.repo.user.IUserRepo;
import com.mitocode.security.JwtTokenUtil;
import com.mitocode.service.file.IFileProcessingService;
import com.mitocode.service.file.IFileUploadService;
import com.mitocode.service.impl.CRUDImpl;
import com.mitocode.service.user.IUserConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileUploadServiceImpl extends CRUDImpl<FileEntity, Long> implements IFileUploadService {

    @Value("${s3.bucket.name}")
    private String BUCKET;

    private final AmazonS3 amazonS3;
    private final IUserRepo userService;
    private final IFileUploadRepo fileRepository;
    private final IFileProcessingService fileProcessingService;
    private final FileUploadConfig fileUploadConfig;
    private final JwtTokenUtil jwtTokenUtil;
    private final IUserConfigService userConfigService;

    @Override
    protected IGenericRepo<FileEntity, Long> getRepo() {
        return fileRepository;
    }

    @Override
    public List<String> uploadFiles(List<MultipartFile> files) {

        // Obtener el username desde el token JWT
        String username = jwtTokenUtil.getAuthenticatedUsername();

        // Verificar si el usuario existe en la base de datos
        Optional<User> optionalUser = userService.findByUsername(username);
        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("El usuario no existe en la base de datos");
        }

        User user = optionalUser.get();

        // Obtener la configuración del usuario
        UserConfig userConfig = userConfigService.getUserConfigByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la configuración del usuario"));

        String storageType = userConfig.getStorageType();

        List<String> resultMessages = new ArrayList<>();

        for (MultipartFile file : files) {
            String originalFileName = file.getOriginalFilename();
            String fileExtension = fileProcessingService.getFileExtension(originalFileName);

            try {
                String fileType = fileProcessingService.determineFileType(fileExtension);

                // Si es tipo "invoice", validar el código de factura
                if ("pdf".equalsIgnoreCase(fileType) || "xml".equalsIgnoreCase(fileType)) {
                    try {
                        fileProcessingService.extractInvoiceCode(originalFileName);
                    } catch (Exception e) {
                        resultMessages.add("Archivo ignorado por código de factura inválido: " + originalFileName);
                        continue; // Saltar este archivo
                    }
                }

                // Verificar si el archivo ya fue subido por este usuario
                boolean exists = fileRepository.existsByFileNameAndFileTypeAndUsers_Username(originalFileName, fileType, username);
                if (exists) {
                    resultMessages.add("Archivo duplicado omitido: " + originalFileName);
                    continue;
                }

                if ("zip".equalsIgnoreCase(fileType)) {
                    resultMessages.addAll(fileProcessingService.processZipFile(file));
                } else {
                    String fullFilePath = fileProcessingService.buildFilePath(originalFileName, fileType, username);
                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(file.getBytes());

                    if ("s3".equalsIgnoreCase(storageType)) {
                        uploadToS3(byteArrayInputStream, fullFilePath, file.getSize(), file.getContentType());
                    } else {
                        uploadFileToLocal(file, username, fileType);
                    }

                    FileEntity fileEntity = new FileEntity();
                    fileEntity.setFileName(originalFileName);
                    fileEntity.setStorageType(storageType);
                    fileEntity.setFileType(fileType);
                    fileEntity.setFileUrl("s3".equalsIgnoreCase(storageType)
                            ? "https://" + BUCKET + ".s3.amazonaws.com/" + fullFilePath
                            : fileUploadConfig.getUploadDir() + fullFilePath);
                    fileEntity.setUsers(Collections.singletonList(user));

                    fileRepository.save(fileEntity);

                    resultMessages.add("Archivo subido exitosamente: " + originalFileName);
                }

            } catch (IOException e) {
                resultMessages.add("Error al subir archivo: " + originalFileName + " - " + e.getMessage());
            }
        }

        return resultMessages;
    }

    @Override
    public void deleteImageByUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            throw new IllegalArgumentException("La URL del archivo es inválida o está vacía");
        }

        // Verificar si es un archivo S3 o local
        if (fileUrl.contains("s3.amazonaws.com")) {
            // Eliminar de Amazon S3
            String s3Key = extractS3KeyFromUrl(fileUrl);
            amazonS3.deleteObject(BUCKET, s3Key);
            System.out.println("Archivo eliminado de S3: " + s3Key);
        } else {
            // Eliminar de almacenamiento local
            String localPath = fileUrl.replace(fileUploadConfig.getUploadDir(), ""); // baseUrl ej: http://localhost:8080/uploads/
            File file = new File(fileUploadConfig.getUploadDir() + localPath);
            if (file.exists()) {
                file.delete();
                System.out.println("Archivo eliminado del sistema local: " + file.getAbsolutePath());
            } else {
                System.out.println("El archivo local no existe: " + file.getAbsolutePath());
            }
        }
    }

    @Override
    public String deleteFileById(Long fileId) {
        // Obtener el username desde el token JWT
        String username = jwtTokenUtil.getAuthenticatedUsername();

        // Buscar el archivo y verificar que pertenece al usuario autenticado
        Optional<FileEntity> optionalFile = fileRepository.findById(fileId);
        if (optionalFile.isEmpty()) {
            throw new IllegalArgumentException("Archivo no encontrado con ID: " + fileId);
        }

        FileEntity fileEntity = optionalFile.get();

        boolean belongsToUser = fileEntity.getUsers().stream()
                .anyMatch(user -> user.getUsername().equals(username));

        if (!belongsToUser) {
            throw new SecurityException("No tienes permiso para eliminar este archivo");
        }

        String storageType = fileEntity.getStorageType();
        String fileUrl = fileEntity.getFileUrl(); // ya tiene la ruta completa

        // Eliminar del sistema de almacenamiento
        if ("s3".equalsIgnoreCase(storageType)) {
            String s3Key = extractS3KeyFromUrl(fileUrl);
            amazonS3.deleteObject(BUCKET, s3Key);
        } else {
            String localPath = fileUrl.replace(fileUploadConfig.getUploadDir(), "");
            File file = new File(fileUploadConfig.getUploadDir() + localPath);
            if (file.exists()) {
                file.delete();
            }
        }

        // Eliminar de la base de datos
        fileRepository.delete(fileEntity);

        return "Archivo eliminado correctamente: " + fileEntity.getFileName();
    }

    private String extractS3KeyFromUrl(String url) {
        String bucketPrefix = "https://" + BUCKET + ".s3.amazonaws.com/";
        return url.replace(bucketPrefix, "");
    }

    @Override
    public String uploadImage(MultipartFile file, String username) {

        // Verificar si el usuario existe en la base de datos
        Optional<User> optionalUser = userService.findByUsername(username);
        if (optionalUser.isEmpty()) {
            throw new ModelNotFoundException("El usuario no existe en la base de datos");
        }

        User user = optionalUser.get();

        // Obtener la configuración del usuario
        UserConfig userConfig = userConfigService.getUserConfigByUsername(username)
                .orElseThrow(() -> new ModelNotFoundException("No se encontró la configuración del usuario"));

        String storageType = userConfig.getStorageType();

        String fileExtension = fileProcessingService.getFileExtension(file.getOriginalFilename());
        String fileType = fileProcessingService.determineFileType(fileExtension);

        try {
            String fullFilePath = fileProcessingService.buildFilePath(file.getOriginalFilename(), fileType, username);
            String fileUrl;

            if ("s3".equalsIgnoreCase(storageType)) {
                uploadToS3(new ByteArrayInputStream(file.getBytes()), fullFilePath, file.getSize(), file.getContentType());
                fileUrl = "https://" + BUCKET + ".s3.amazonaws.com/" + fullFilePath;
            } else {
                uploadFileToLocal(file, username, fileType);
                fileUrl = fileUploadConfig.getUploadDir() + fullFilePath;
            }

            FileEntity fileEntity = new FileEntity();
            fileEntity.setFileName(file.getOriginalFilename());
            fileEntity.setStorageType(storageType);
            fileEntity.setFileType("image");
            fileEntity.setFileUrl(fileUrl);
            fileEntity.setUsers(Collections.singletonList(user));

            fileRepository.save(fileEntity);

            return fileUrl;

        } catch (IOException e) {
            throw new RuntimeException("Error al subir imagen: " + file.getOriginalFilename(), e);
        }
    }

    // Metodo para subir un archivo a Amazon S3
    @Override
    public void uploadToS3(ByteArrayInputStream inputStream, String fileName, long fileSize, String contentType) {
        // Crear metadatos para el archivo, como el tamaño y el tipo de contenido
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(fileSize);
        metadata.setContentType(contentType);

        // Subir el archivo a S3 usando el cliente de Amazon S3
        amazonS3.putObject(BUCKET, fileName, inputStream, metadata);
    }

    // Metodo para guardar un archivo localmente
    @Override
    public String uploadFileToLocal(MultipartFile file, String username, String fileType) throws IOException {
        // Construir la ruta del directorio basada en el tipo de archivo y el usuario
        String folderPath = fileUploadConfig.getUploadDir() + fileProcessingService.getFolderByFileType(fileType, username);

        // Crear el directorio si no existe
        File directory = new File(folderPath);
        if (!directory.exists()) {
            directory.mkdirs();  // Crear la carpeta y las subcarpetas necesarias
        }

        // Construir la ruta completa del archivo (incluyendo el nombre del archivo)
        String filePath = folderPath + "/" + file.getOriginalFilename();

        // Guardar el archivo físicamente en el directorio especificado
        file.transferTo(new File(filePath));

        // Retorna la ruta completa donde se guardó el archivo
        return filePath;
    }

    @Override
    public Object searchFiles(String fileType) {
        // Obtener el username desde el token JWT
        String username = jwtTokenUtil.getAuthenticatedUsername();
        //System.out.println(username);

        if ("invoice".equalsIgnoreCase(fileType)) {
            // Buscar archivos XML y PDF para las facturas
            List<FileEntity> xmlFiles = fileRepository.findByFileTypeAndUsers_Username("xml", username);
            List<FileEntity> pdfFiles = fileRepository.findByFileTypeAndUsers_Username("pdf", username);

            // Combinar todos los archivos de facturas
            List<FileEntity> invoiceFiles = new ArrayList<>();
            invoiceFiles.addAll(xmlFiles);
            invoiceFiles.addAll(pdfFiles);

            // Agrupar por código de factura
            Map<String, List<FileEntity>> groupedInvoices = invoiceFiles.stream()
                    .collect(Collectors.groupingBy(file -> fileProcessingService.extractInvoiceCode(file.getFileName())));

            return groupedInvoices;
        } else {
            // Para otros tipos de archivos, devolver la lista sin agrupar
            return fileRepository.findByFileTypeAndUsers_Username(fileType, username);
        }
    }

    // Buscar archivos en Amazon S3 por ruta y tipo de archivo
    private List<FileEntity> searchFilesInS3(String username, String fileType) {
        // Lógica para buscar archivos en S3 utilizando el nombre de usuario y tipo de archivo
        // En S3, debes estructurar las carpetas con el nombre de usuario y tipo de archivo
        String prefix = username + "/" + fileType + "/"; // Asumiendo una estructura de carpeta como username/fileType/

        List<FileEntity> files = new ArrayList<>();
        // Aquí puedes usar el cliente de Amazon S3 para listar los objetos bajo ese prefix
        // Este es solo un ejemplo de cómo podrías hacerlo, es posible que tengas que adaptarlo

        // Ejemplo para obtener objetos de un bucket en S3
        ObjectListing objectListing = amazonS3.listObjects(BUCKET, prefix);
        for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
            FileEntity fileEntity = new FileEntity();
            fileEntity.setFileName(objectSummary.getKey()); // Nombre del archivo en S3
            fileEntity.setFileType(fileType); // Tipo de archivo
            fileEntity.setStorageType("s3"); // Indicamos que está en S3
            fileEntity.setFileUrl("https://" + BUCKET + ".s3.amazonaws.com/" + objectSummary.getKey()); // URL del archivo en S3
            files.add(fileEntity);
        }
        return files;
    }

    // Buscar archivos en almacenamiento local por ruta y tipo de archivo
    private List<FileEntity> searchFilesInLocal(String username, String fileType) {
        // Lógica para buscar archivos en el almacenamiento local
        String folderPath = fileUploadConfig.getUploadDir() + username + "/" + fileType + "/"; // Ruta local estructurada como username/fileType/
        File directory = new File(folderPath);

        List<FileEntity> files = new ArrayList<>();

        if (directory.exists() && directory.isDirectory()) {
            File[] localFiles = directory.listFiles();
            if (localFiles != null) {
                for (File localFile : localFiles) {
                    FileEntity fileEntity = new FileEntity();
                    fileEntity.setFileName(localFile.getName()); // Nombre del archivo
                    fileEntity.setFileType(fileType); // Tipo de archivo
                    fileEntity.setStorageType("local"); // Indicamos que está en almacenamiento local
                    fileEntity.setFileUrl("file://" + localFile.getAbsolutePath()); // Ruta completa del archivo local
                    files.add(fileEntity);
                }
            }
        }
        return files;
    }
}
