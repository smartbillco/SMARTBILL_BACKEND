package com.mitocode.service.impl.correspondence;

import com.mitocode.dto.request.correspondence.ComplaintRequest;
import com.mitocode.dto.request.communication.NotificationMessageRequest;
import com.mitocode.model.Complaint;
import com.mitocode.model.ComplaintStatus;
import com.mitocode.repo.correspondence.IComplaintRepo;
import com.mitocode.repo.IGenericRepo;
import com.mitocode.service.correspondence.IComplaintService;
import com.mitocode.service.impl.CRUDImpl;
import com.mitocode.service.impl.communication.MailServiceImpl;
import com.mitocode.util.MapperUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ComplaintServiceImpl extends CRUDImpl<Complaint, Long> implements IComplaintService {

    private final IComplaintRepo complaintRepository;
    private final MailServiceImpl emailService;
    private final MapperUtil mapperUtil;

    @Override
    protected IGenericRepo<Complaint, Long> getRepo() {
        return complaintRepository;
    }

    // Mapeo de tipos de solicitud a correos de departamentos
    private static final Map<String, String> DEPARTMENT_EMAILS = Map.of(
            "Soporte Técnico", "danielsntos06@gmail.com",
            "Atención al Cliente", "clientes@empresa.com",
            "Recursos Humanos", "rrhh@empresa.com"
    );

    @Override
    @Transactional
    public Complaint registerComplaint(ComplaintRequest dto) {
        // Obtener el correo del departamento según el tipo de queja
        String departmentEmail = DEPARTMENT_EMAILS.getOrDefault(dto.getComplaintType(), "otros@empresa.com");


        // Crear entidad
        Complaint complaint = mapperUtil.map(dto, Complaint.class);
        complaint.setName(dto.getName());
        complaint.setEmail(dto.getEmail());
        complaint.setSubject(dto.getSubject());
        complaint.setComplaintType(dto.getComplaintType());
        complaint.setMessage(dto.getMessage());
        complaint.setStatus(ComplaintStatus.PENDING);
        complaint.setDepartmentEmail(departmentEmail);
        complaint.setCreatedAt(LocalDateTime.now());

        // Guardar en la BD
        complaint = complaintRepository.save(complaint);

        // Construir el mensaje de notificación para quejas o sugerencias
        Map<String, Object> model = new HashMap<>();
        model.put("name", complaint.getName());
        model.put("email", complaint.getEmail());
        model.put("subject", complaint.getSubject());
        model.put("complaintType", complaint.getComplaintType());
        model.put("message", complaint.getMessage());

        NotificationMessageRequest notificationMessage = new NotificationMessageRequest();
        notificationMessage.setType("COMPLAINT-EMAIL");
        notificationMessage.setTo(Collections.singletonList(complaint.getDepartmentEmail()));
        notificationMessage.setSubject("Nueva Queja o Sugerencia Recibida");
        notificationMessage.setModel(model);

        try {
            emailService.sendMail(notificationMessage);
            complaint.setSendAt(LocalDateTime.now());
            complaintRepository.save(complaint);
        } catch (Exception e) {
            // Logueamos el error pero no detenemos el flujo
            log.error("Error al enviar el correo de queja: {}", e.getMessage());
        }
        return complaint;
    }
}
