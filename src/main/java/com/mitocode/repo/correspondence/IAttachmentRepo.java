package com.mitocode.repo.correspondence;

import com.mitocode.model.correspondence.Correspondence;
import com.mitocode.model.correspondence.CorrespondenceAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IAttachmentRepo extends JpaRepository<CorrespondenceAttachment, Long> {

    List<CorrespondenceAttachment> findByCorrespondence(Correspondence correspondence); // Adjuntos de una correspondencia

    // 📎 Obtener adjuntos por ID de correspondencia
    List<CorrespondenceAttachment> findByCorrespondenceIdCorrespondence(Long correspondenceId);
}
