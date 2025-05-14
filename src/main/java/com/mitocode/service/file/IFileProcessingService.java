package com.mitocode.service.file;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface IFileProcessingService {
    // Procesar archivos ZIP
    List<String> processZipFile(MultipartFile zipFile) throws IOException;

    MultipartFile convertToMultipartFile(File file) throws IOException;

    String getFileExtension(String fileName);

    String determineFileType(String fileExtension);

    String getFolderByFileType(String fileType, String username);

    String buildFilePath(String originalFileName, String fileType, String username);

    String extractInvoiceCode(String fileName);

}
