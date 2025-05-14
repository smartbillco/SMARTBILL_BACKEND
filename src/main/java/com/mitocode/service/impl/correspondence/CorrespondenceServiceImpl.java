package com.mitocode.service.impl.correspondence;

import com.mitocode.dto.request.correspondence.CorrespondenceRequest;
import com.mitocode.dto.response.correspondence.AttachmentResponse;
import com.mitocode.dto.response.correspondence.CorrespondenceResponse;
import com.mitocode.exception.ModelNotFoundException;
import com.mitocode.model.ComplaintStatus;
import com.mitocode.model.correspondence.*;
import com.mitocode.model.user.User;
import com.mitocode.repo.*;
import com.mitocode.repo.correspondence.*;
import com.mitocode.repo.user.IUserRepo;
import com.mitocode.security.JwtTokenUtil;
import com.mitocode.service.correspondence.ICorrespondenceService;
import com.mitocode.service.impl.CRUDImpl;
import com.mitocode.util.MapperUtil;
import com.mitocode.util.NotificationProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class CorrespondenceServiceImpl extends CRUDImpl<Correspondence, Long> implements ICorrespondenceService {

    private final ICorrespondenceRepo correspondenceRepository;
    private final NotificationProducer correspondenceProducer;
    private final ICorrespondenceTypeRepo typeRepository;
    private final ICorrespondenceSubtypeRepo subtypeRepository;
    private final IAttachmentRepo attachmentRepo;
    private final IUserRepo userRepository;
    private final IDepartmentRepo departmentRepository;
    private final CorrespondenceDepartmentServiceimpl correspondenceDepartmentServiceimpl;
    private final JwtTokenUtil jwtTokenUtil;
    private final AttachmentServiceImpl attachmentService;
    private final MapperUtil mapperUtil;

    @Override
    @Transactional
    protected IGenericRepo<Correspondence, Long> getRepo() {
        return correspondenceRepository;
    }

    @Override
    @Transactional
    public CorrespondenceResponse createCorrespondence(CorrespondenceRequest request) throws IOException {

        CorrespondenceType type = typeRepository.findByName(request.getTypeName())
                .orElseThrow(() -> new IllegalArgumentException("Tipo de correspondencia no encontrado"));

        CorrespondenceSubtype subtype = subtypeRepository
                .findByNameAndTypeId(request.getSubtypeName(), type.getId())
                .orElseThrow(() -> new IllegalArgumentException("Subtipo de correspondencia no encontrado"));

        CorrespondenceDepartment department = departmentRepository.findByName(request.getDepartment())
                .orElseThrow(() -> new IllegalArgumentException("Departamento de correspondencia no encontrado"));

        String username = jwtTokenUtil.getAuthenticatedUsername();
        User sender = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        Correspondence correspondence = Correspondence.builder()
                .sender(sender)
                .type(type)
                .subtype(subtype)
                .correspondenceDepartment(department)
                .recipient(request.getRecipient())
                .subject(request.getSubject())
                .text(request.getText())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .priority(request.getPriority())
                .filingDate(LocalDateTime.now())
                .externalDate(request.getExternalDate())
                .sendDate(LocalDateTime.now().plusMinutes(1))
                .resolvedDate(request.getResolvedDate())
                .expireDate(LocalDateTime.now().plusMinutes(90))
                .status(ComplaintStatus.PENDING)
                .build();

        Correspondence saved = correspondenceRepository.save(correspondence);

        if (request.getAttachments() != null && !request.getAttachments().isEmpty()) {
            saveAttachments(saved, request.getAttachments());
        }

        return mapToResponse(saved);
    }

    @Override
    public List<Correspondence> getCorrespondencesBySender(User sender) {
        return correspondenceRepository.findBySender(sender);
    }

    /*
    @Override
    public List<Correspondence> getCorrespondencesByRecipient(User recipient) {
        return correspondenceRepository.findByRecipientsContaining(recipient);
    }
     */

    @Override
    public List<Correspondence> getCorrespondencesByStatus(ComplaintStatus status) {
        return correspondenceRepository.findByStatus(status);
    }

    @Override
    @Transactional
    public Optional<CorrespondenceResponse> getCorrespondenceById(Long id) {
        return correspondenceRepository.findById(id)
                .map(this::mapToResponse)
                .or(() -> {
                    throw new ModelNotFoundException("Correspondencia no encontrada");
                });
    }

    @Override
    @Transactional
    public void updateStatus(Long id, ComplaintStatus status) {
        correspondenceRepository.findById(id).ifPresent(correspondence -> {
            correspondence.setStatus(status);
            correspondenceRepository.save(correspondence);
        });
    }

    @Override
    @Transactional
    public void deleteExpiredCorrespondences() {
        LocalDateTime now = LocalDateTime.now();
        List<Correspondence> expiredCorrespondences = correspondenceRepository.findByExpireDateBefore(now);

        if (expiredCorrespondences.isEmpty()) {
            log.info("No hay correspondencias expiradas");
            return;
        }

        correspondenceRepository.deleteAll(expiredCorrespondences);
        log.info("Se eliminaron {} correspondencias expiradas.", expiredCorrespondences.size());
    }

    @Override
    @Transactional
    public List<CorrespondenceResponse> getExpiredCorrespondences() {
        LocalDateTime now = LocalDateTime.now();
        List<CorrespondenceResponse> expiredCorrespondences = correspondenceRepository.findByExpireDateBefore(now)
                .stream().map(this::mapToResponse).toList();

        if (expiredCorrespondences.isEmpty()) {
            throw new ModelNotFoundException("No se encontraron correspondencias expiradas");
        }

        return expiredCorrespondences;
    }

    //@Scheduled(fixedRate = 600000)
    public void scheduledDeleteExpiredCorrespondences() {
        try {
            deleteExpiredCorrespondences();
        } catch (Exception e) {
            log.error("Error al eliminar correspondencias expiradas: {}", e.getMessage());
        }
    }

    @Override
    @Transactional
    public List<CorrespondenceResponse> getByDepartment(Long departmentId) {
        List<CorrespondenceResponse> responses = correspondenceRepository.findByCorrespondenceDepartmentId(departmentId)
                .stream().map(this::mapToResponse).toList();

        if (responses.isEmpty()) {
            throw new ModelNotFoundException("No se encontró correspondencia para el departamento con ID: " + departmentId);
        }
        return responses;
    }

    @Override
    @Transactional
    public List<CorrespondenceResponse> getByDate(String date) {
        // Convertir la fecha String en LocalDate
        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // Obtener el inicio y fin del día
        LocalDateTime start = localDate.atStartOfDay();
        LocalDateTime end = localDate.plusDays(1).atStartOfDay();

        // Buscar las correspondencias entre esas fechas
        List<CorrespondenceResponse> responses = correspondenceRepository.findBySendDateBetween(start, end)
                .stream().map(this::mapToResponse).toList();

        if (responses.isEmpty()) {
            throw new ModelNotFoundException("No se encontraron correspondencias para la fecha: " + date);
        }
        return responses;
    }

    /*

    @Override
    @Transactional
    public List<CorrespondenceResponse> getReceivedCorrespondences() {
        String username = jwtTokenUtil.getAuthenticatedUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ModelNotFoundException("Usuario no encontrado: " + username));

        List<CorrespondenceResponse> responses = correspondenceRepository.findByRecipientsContaining(user)
                .stream().map(this::mapToResponse).toList();

        if (responses.isEmpty()) {
            throw new ModelNotFoundException("No se encontraron correspondencias recibidas para el usuario: " + username);
        }
        return responses;
    }
     */

    @Override
    @Transactional
    public List<CorrespondenceResponse> getSentCorrespondences() {
        String username = jwtTokenUtil.getAuthenticatedUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ModelNotFoundException("Usuario no encontrado: " + username));

        List<CorrespondenceResponse> responses = correspondenceRepository.findBySender(user)
                .stream().map(this::mapToResponse).toList();

        if (responses.isEmpty()) {
            throw new ModelNotFoundException("No se encontraron correspondencias enviadas para el usuario: " + username);
        }
        return responses;
    }


    @Override
    public Map<String, Long> getCorrespondenceSummary() {
        Map<String, Long> summary = new HashMap<>();
        summary.put("PENDING", correspondenceRepository.countByStatus(ComplaintStatus.PENDING));
        summary.put("IN_PROGRESS", correspondenceRepository.countByStatus(ComplaintStatus.IN_PROGRESS));
        summary.put("RESOLVED", correspondenceRepository.countByStatus(ComplaintStatus.RESOLVED));
        summary.put("REJECTED", correspondenceRepository.countByStatus(ComplaintStatus.CLOSED));
        return summary;
    }

    @Override
    @Transactional
    public List<CorrespondenceResponse> searchCorrespondences(String query) {
        List<CorrespondenceResponse> responses = correspondenceRepository
                .findBySubjectContainingIgnoreCaseOrTextContainingIgnoreCase(query, query)
                .stream().map(this::mapToResponse).toList();

        if (responses.isEmpty()) {
            throw new ModelNotFoundException("No se encontraron correspondencias que coincidan con la búsqueda: " + query);
        }
        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttachmentResponse> getAttachmentsByCorrespondence(Long id) {
        // Verificar si la correspondencia existe
        if (!correspondenceRepository.existsById(id)) {
            throw new ModelNotFoundException("La correspondencia con ID " + id + " no existe.");
        }

        // Obtener los adjuntos
        List<CorrespondenceAttachment> attachments = attachmentRepo.findByCorrespondenceIdCorrespondence(id);

        if (attachments.isEmpty()) {
            throw new ModelNotFoundException("No se encontraron adjuntos para la correspondencia con ID " + id);
        }

        // Mapear usando MapperUtil
        return mapperUtil.mapList(attachments, AttachmentResponse.class);
    }

    public List<AttachmentResponse> saveAttachments(Correspondence correspondence, List<MultipartFile> files) throws IOException {
        List<CorrespondenceAttachment> savedAttachments = new ArrayList<>();

        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                String fileKey = attachmentService.uploadFile(file); // Devuelve clave o ruta

                CorrespondenceAttachment attachment = new CorrespondenceAttachment();
                attachment.setFileName(file.getOriginalFilename());
                attachment.setFileType(file.getContentType());
                attachment.setFileUrl(fileKey);
                attachment.setCorrespondence(correspondence);

                CorrespondenceAttachment saved = attachmentRepo.save(attachment);
                savedAttachments.add(saved);
            }
        }

        // Mapear a DTO usando MapperUtil
        return mapperUtil.mapList(savedAttachments, AttachmentResponse.class);
    }

    /*

    private NotificationMessageRequest mapToNotificationRequest(Correspondence correspondence) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        Map<String, Object> model = Map.of(
                "sender", correspondence.getSender().getEmail(),
                "recipients", correspondence.getRecipients().stream().map(User::getEmail).toList(),
                "subject", correspondence.getSubject(),
                "filingDate", correspondence.getFilingDate().format(formatter),
                "text", correspondence.getText(),
                "department", correspondence.getCorrespondenceDepartment().getName(),
                "id", correspondence.getIdCorrespondence(),
                "url", "https://smartbill/correspondence/" + correspondence.getIdCorrespondence()
        );

        return new NotificationMessageRequest(
                "CORRESPONDENCE",
                correspondence.getSender().getEmail(),
                correspondence.getRecipients().stream().map(User::getEmail).toList(),
                correspondence.getSubject(),
                model
        );
    }
     */

    private CorrespondenceResponse mapToResponse(Correspondence correspondence) {
        return CorrespondenceResponse.builder()
                .id(correspondence.getIdCorrespondence())
                .sender(correspondence.getSender().getEmail())
                .recipient(correspondence.getRecipient())
                .type(correspondence.getType().getName())
                .subtype(correspondence.getSubtype().getName())
                .department(correspondence.getCorrespondenceDepartment().getName())
                .subject(correspondence.getSubject())
                .text(correspondence.getText())
                .createdAt(correspondence.getCreatedAt())
                .externalDate(correspondence.getExternalDate())
                .resolvedDate(correspondence.getResolvedDate())
                .filingDate(correspondence.getFilingDate())
                .sendDate(correspondence.getSendDate())
                .expireDate(correspondence.getExpireDate())
                .priority(correspondence.getPriority())
                .status(correspondence.getStatus())
                .build();
    }

}
