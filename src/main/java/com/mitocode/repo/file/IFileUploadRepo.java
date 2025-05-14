package com.mitocode.repo.file;

import com.mitocode.model.file.FileEntity;
import com.mitocode.repo.IGenericRepo;

import java.util.List;

public interface IFileUploadRepo extends IGenericRepo<FileEntity, Long> {
    /* Subir un archivo normal (imagen, XML, PDF)
    String uploadFile(MultipartFile file, String username, String fileType);

    // Procesar archivos ZIP

    String processZip(MultipartFile file);
     */
    // Buscar archivos por tipo y por usuario (relación muchos a muchos)
    List<FileEntity> findByFileTypeAndUsers_Username(String fileType, String username);

    boolean existsByFileNameAndFileTypeAndUsers_Username(String fileName, String fileType, String username);
}
