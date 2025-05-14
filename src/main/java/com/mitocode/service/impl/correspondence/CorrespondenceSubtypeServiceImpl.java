package com.mitocode.service.impl.correspondence;

import com.mitocode.dto.request.correspondence.CorrespondenceSubtypeRequest;
import com.mitocode.dto.response.correspondence.CorrespondenceSubtypeResponse;
import com.mitocode.dto.response.correspondence.CorrespondenceTypeResponse;
import com.mitocode.exception.ModelNotFoundException;
import com.mitocode.model.correspondence.CorrespondenceSubtype;
import com.mitocode.model.correspondence.CorrespondenceType;
import com.mitocode.repo.correspondence.ICorrespondenceSubtypeRepo;
import com.mitocode.repo.correspondence.ICorrespondenceTypeRepo;
import com.mitocode.service.correspondence.ICorrespondenceSubtypeService;
import com.mitocode.util.MapperUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CorrespondenceSubtypeServiceImpl implements ICorrespondenceSubtypeService {

    private final ICorrespondenceSubtypeRepo subtypeRepo;
    private final ICorrespondenceTypeRepo typeRepo;
    private final MapperUtil mapperUtil;

    @Override
    public List<CorrespondenceSubtypeResponse> findAll() {
        return mapperUtil.mapList(subtypeRepo.findAll(), CorrespondenceSubtypeResponse.class);
    }

    @Override
    public List<CorrespondenceSubtypeResponse> findByType(Long typeId) {
        return mapperUtil.mapList(subtypeRepo.findByTypeId(typeId), CorrespondenceSubtypeResponse.class);
    }

    @Override
    public CorrespondenceSubtypeResponse create(CorrespondenceSubtypeRequest dto) {
        CorrespondenceType type = typeRepo.findById(dto.getTypeId())
                .orElseThrow(() -> new ModelNotFoundException("Tipo no encontrado"));

        CorrespondenceSubtype subtype = new CorrespondenceSubtype();
        subtype.setName(dto.getName());
        subtype.setDescription(dto.getDescription());
        subtype.setType(type);

        return mapperUtil.map(subtypeRepo.save(subtype), CorrespondenceSubtypeResponse.class);
    }

    @Override
    public CorrespondenceSubtypeResponse update(Long id, CorrespondenceSubtypeRequest dto) {
        CorrespondenceSubtype existing = subtypeRepo.findById(id)
                .orElseThrow(() -> new ModelNotFoundException("Subtipo no encontrado"));

        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());

        if (!existing.getType().getId().equals(dto.getTypeId())) {
            CorrespondenceType newType = typeRepo.findById(dto.getTypeId())
                    .orElseThrow(() -> new ModelNotFoundException("Tipo no encontrado"));
            existing.setType(newType);
        }

        return mapperUtil.map(subtypeRepo.save(existing), CorrespondenceSubtypeResponse.class);
    }

    @Override
    public void delete(Long id) {
        if (!subtypeRepo.existsById(id)) {
            throw new ModelNotFoundException("Subtipo no encontrado");
        }
        subtypeRepo.deleteById(id);
    }
}
