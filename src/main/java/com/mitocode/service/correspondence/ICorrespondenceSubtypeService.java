package com.mitocode.service.correspondence;

import com.mitocode.dto.request.correspondence.CorrespondenceSubtypeRequest;
import com.mitocode.dto.response.correspondence.CorrespondenceSubtypeResponse;

import java.util.List;

public interface ICorrespondenceSubtypeService {

    List<CorrespondenceSubtypeResponse> findAll();

    List<CorrespondenceSubtypeResponse> findByType(Long typeId);

    CorrespondenceSubtypeResponse create(CorrespondenceSubtypeRequest dto);

    CorrespondenceSubtypeResponse update(Long id, CorrespondenceSubtypeRequest dto);

    void delete(Long id);
}
