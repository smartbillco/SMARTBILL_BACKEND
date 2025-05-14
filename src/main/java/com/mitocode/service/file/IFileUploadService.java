package com.mitocode.service.file;

import com.mitocode.model.file.FileEntity;
import com.mitocode.service.ICRUD;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

public interface IFileUploadService extends ICRUD<FileEntity, Long> {

    // Subir un archivo normal (imagen, XML, PDF)
    List<String> uploadFiles(List<MultipartFile> files);

    String deleteFileById(Long fileId);

    void deleteImageByUrl(String fileUrl);

    String uploadImage(MultipartFile file, String username);

    void uploadToS3(ByteArrayInputStream inputStream, String fileName, long fileSize, String contentType);

    String uploadFileToLocal(MultipartFile file, String fileName, String fileType) throws IOException;

    Object searchFiles(String fileType);
}


