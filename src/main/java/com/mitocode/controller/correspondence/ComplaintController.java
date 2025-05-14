package com.mitocode.controller.correspondence;

import com.mitocode.dto.request.correspondence.ComplaintRequest;
import com.mitocode.model.Complaint;
import com.mitocode.service.correspondence.IComplaintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/complaint")
@RequiredArgsConstructor
public class ComplaintController {

    private final IComplaintService complaintService;

    @PostMapping
    public ResponseEntity<Complaint> createComplaint(@Valid @RequestBody ComplaintRequest dto) {
        Complaint complaint = complaintService.registerComplaint(dto);
        return ResponseEntity.ok(complaint);
    }
}
