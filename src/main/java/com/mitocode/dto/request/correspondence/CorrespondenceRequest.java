package com.mitocode.dto.request.correspondence;

import com.mitocode.model.correspondence.CorrespondenceDepartment;
import com.sun.jna.WString;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CorrespondenceRequest {
    private String typeName;
    private String subtypeName;
    private String department;
    private String subject;
    private String text;
    private String recipient;
    private String priority;
    private LocalDateTime externalDate;
    private LocalDateTime resolvedDate;

    private List<MultipartFile> attachments;
}
