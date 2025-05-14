package com.mitocode.service.impl.correspondence;

import com.mitocode.dto.request.correspondence.CorrespondenceTypeRequest;
import com.mitocode.dto.response.correspondence.CorrespondenceTypeResponse;
import com.mitocode.exception.ModelNotFoundException;
import com.mitocode.model.correspondence.CorrespondenceType;
import com.mitocode.repo.correspondence.ICorrespondenceTypeRepo;
import com.mitocode.service.correspondence.ICorrespondenceTypeService;
import com.mitocode.util.MapperUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CorrespondenceTypeServiceImpl implements ICorrespondenceTypeService {

    private final ICorrespondenceTypeRepo repository;
    private final MapperUtil mapperUtil;

    @Override
    public List<CorrespondenceTypeResponse> findAll() {
        return mapperUtil.mapList(repository.findAll(), CorrespondenceTypeResponse.class);
    }

    @Override
    public CorrespondenceTypeResponse findById(Long id) {
        CorrespondenceType type = repository.findById(id)
                .orElseThrow(() -> new ModelNotFoundException("Tipo no encontrado"));
        return mapperUtil.map(type, CorrespondenceTypeResponse.class);
    }

    @Override
    public CorrespondenceTypeResponse create(CorrespondenceTypeRequest dto) {
        CorrespondenceType entity = mapperUtil.map(dto, CorrespondenceType.class);
        return mapperUtil.map(repository.save(entity), CorrespondenceTypeResponse.class);
    }

    @Override
    public CorrespondenceTypeResponse update(Long id, CorrespondenceTypeRequest dto) {
        CorrespondenceType existing = repository.findById(id)
                .orElseThrow(() -> new ModelNotFoundException("Tipo no encontrado"));
        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        return mapperUtil.map(repository.save(existing), CorrespondenceTypeResponse.class);
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ModelNotFoundException("Tipo no encontrado");
        }
        repository.deleteById(id);
    }
}
