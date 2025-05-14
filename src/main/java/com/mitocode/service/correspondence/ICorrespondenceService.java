package com.mitocode.service.correspondence;

import com.mitocode.dto.request.correspondence.CorrespondenceRequest;
import com.mitocode.dto.response.correspondence.AttachmentResponse;
import com.mitocode.dto.response.correspondence.CorrespondenceResponse;
import com.mitocode.model.ComplaintStatus;
import com.mitocode.model.correspondence.Correspondence;
import com.mitocode.model.user.User;
import com.mitocode.service.ICRUD;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ICorrespondenceService extends ICRUD<Correspondence, Long> {
    //CorrespondenceResponse createCorrespondence(CorrespondenceRequest request, List<MultipartFile> attachments) throws IOException;

    CorrespondenceResponse createCorrespondence(CorrespondenceRequest request) throws IOException;

    List<CorrespondenceResponse> getExpiredCorrespondences();

    void deleteExpiredCorrespondences();

    List<Correspondence> getCorrespondencesBySender(User sender);

    //List<Correspondence> getCorrespondencesByRecipient(User recipient);

    List<Correspondence> getCorrespondencesByStatus(ComplaintStatus status);

    Optional<CorrespondenceResponse> getCorrespondenceById(Long id);

    void updateStatus(Long id, ComplaintStatus status);

    List<CorrespondenceResponse> getByDepartment(Long departmentId);

    List<CorrespondenceResponse> getByDate(String date);

    //List<CorrespondenceResponse> getReceivedCorrespondences();

    List<CorrespondenceResponse> getSentCorrespondences();

    Map<String, Long> getCorrespondenceSummary();

    List<CorrespondenceResponse> searchCorrespondences(String query);

    List<AttachmentResponse> getAttachmentsByCorrespondence(Long id);
}
