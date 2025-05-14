package com.mitocode.repo.correspondence;

import com.mitocode.model.ComplaintStatus;
import com.mitocode.model.correspondence.Correspondence;
import com.mitocode.model.user.User;
import com.mitocode.repo.IGenericRepo;

import java.time.LocalDateTime;
import java.util.List;

public interface ICorrespondenceRepo extends IGenericRepo<Correspondence, Long> {

    List<Correspondence> findBySender(User sender); // Quejas enviadas por un usuario

    //List<Correspondence> findByRecipientsContaining(User recipient); // Quejas donde un usuario es destinatario

    List<Correspondence> findByStatus(ComplaintStatus status); // Filtrar por estado

    // 📄 Buscar correspondencias por departamento
    List<Correspondence> findByCorrespondenceDepartmentId(Long departmentId);

    // 📄 Contar correspondencias por estado
    Long countByStatus(ComplaintStatus status);

    // 📌 Buscar correspondencias por rango de fecha (LocalDateTime)
    List<Correspondence> findBySendDateBetween(LocalDateTime start, LocalDateTime end);

    // 🔍 Buscar correspondencias por texto en asunto o contenido (Ignorando mayúsculas y minúsculas)
    List<Correspondence> findBySubjectContainingIgnoreCaseOrTextContainingIgnoreCase(String subject, String text);

    List<Correspondence> findByExpireDateBefore(LocalDateTime date);

    // 📌 Buscar correspondencias por fecha de envío
    //List<Correspondence> findBySentDate(LocalDateTime sentDate);

    // 📥 Buscar correspondencias recibidas por usuario autenticado
    //List<Correspondence> findByReceiverIdUser(Integer receiverId);

    // 📤 Buscar correspondencias enviadas por usuario autenticado
    List<Correspondence> findBySenderIdUser(Integer senderId);

}
