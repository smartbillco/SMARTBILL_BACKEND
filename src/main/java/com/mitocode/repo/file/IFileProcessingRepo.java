package com.mitocode.repo.file;

import com.mitocode.model.file.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IFileProcessingRepo extends JpaRepository<FileEntity, Integer> {

    List<FileEntity> findByUsers_UsernameAndFileType(String username, String fileType);

    // Buscar archivos por usuario y tipos (xml, pdf)
    List<FileEntity> findByUsers_UsernameAndFileTypeIn(String username, List<String> fileTypes);
}
