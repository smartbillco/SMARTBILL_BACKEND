package com.mitocode.service.correspondence;

import com.mitocode.dto.request.correspondence.CorrespondenceTypeRequest;
import com.mitocode.dto.response.correspondence.CorrespondenceTypeResponse;

import java.util.List;

public interface ICorrespondenceTypeService {
    List<CorrespondenceTypeResponse> findAll();

    CorrespondenceTypeResponse findById(Long id);

    CorrespondenceTypeResponse create(CorrespondenceTypeRequest dto);

    CorrespondenceTypeResponse update(Long id, CorrespondenceTypeRequest dto);

    void delete(Long id);
}
