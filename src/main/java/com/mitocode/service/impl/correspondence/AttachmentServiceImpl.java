package com.mitocode.service.impl.correspondence;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.mitocode.model.correspondence.Correspondence;
import com.mitocode.model.correspondence.CorrespondenceAttachment;
import com.mitocode.repo.correspondence.IAttachmentRepo;
import com.mitocode.service.correspondence.IAttachmentService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl implements IAttachmentService {

    private final IAttachmentRepo attachmentRepository;

    @Override
    public List<CorrespondenceAttachment> getAttachmentsByCorrespondence(Correspondence correspondence) {
        return attachmentRepository.findByCorrespondence(correspondence);
    }

    @Override
    public CorrespondenceAttachment saveAttachment(CorrespondenceAttachment attachment) {
        return attachmentRepository.save(attachment);
    }

    private final AmazonS3 s3Client;

    @Value("${s3.bucket.name2}")
    private String bucketName;

    // Subir archivo (v1)
    public String uploadFile(MultipartFile file) throws IOException {
        String key = UUID.randomUUID() + "_" + file.getOriginalFilename();
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());

        s3Client.putObject(
                bucketName,
                key,
                file.getInputStream(),
                metadata
        );
        return key;
    }

    // Descargar archivo (v1)
    public byte[] downloadFile(String key) throws IOException {
        S3Object object = s3Client.getObject(bucketName, key);
        return IOUtils.toByteArray(object.getObjectContent());
    }

    // Eliminar archivo (v1)
    public void deleteFile(String key) {
        s3Client.deleteObject(bucketName, key);
    }
}
