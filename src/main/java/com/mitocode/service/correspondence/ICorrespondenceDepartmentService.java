package com.mitocode.service.correspondence;


import com.mitocode.dto.request.correspondence.CorrespondenceDepartmentRequest;
import com.mitocode.dto.response.correspondence.CorrespondenceDepartmentResponse;

import java.util.List;

public interface ICorrespondenceDepartmentService {
    List<CorrespondenceDepartmentResponse> findAll();

    CorrespondenceDepartmentResponse findById(Long id);

    CorrespondenceDepartmentResponse create(CorrespondenceDepartmentRequest dto);

    CorrespondenceDepartmentResponse update(Long id, CorrespondenceDepartmentRequest dto);

    void delete(Long id);
}
