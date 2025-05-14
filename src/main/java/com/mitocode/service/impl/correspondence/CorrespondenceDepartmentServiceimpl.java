package com.mitocode.service.impl.correspondence;

import com.mitocode.dto.request.correspondence.CorrespondenceDepartmentRequest;
import com.mitocode.dto.response.correspondence.CorrespondenceDepartmentResponse;
import com.mitocode.exception.ModelNotFoundException;
import com.mitocode.model.correspondence.CorrespondenceSubtype;
import com.mitocode.model.correspondence.CorrespondenceType;
import com.mitocode.model.correspondence.CorrespondenceDepartment;
import com.mitocode.repo.correspondence.IDepartmentRepo;
import com.mitocode.service.correspondence.ICorrespondenceDepartmentService;
import com.mitocode.util.MapperUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CorrespondenceDepartmentServiceimpl implements ICorrespondenceDepartmentService {

    private final IDepartmentRepo repository;
    private final MapperUtil mapperUtil;

    public CorrespondenceDepartment assignDepartment(CorrespondenceType type, CorrespondenceSubtype subtype) {
        // Ejemplo de lógica de asignación
        if (type.getName().equalsIgnoreCase("Legal") && subtype.getName().equalsIgnoreCase("Contracts")) {
            return repository.findById(1L).orElseThrow(() -> new RuntimeException("Department not found"));
        } else if (type.getName().equalsIgnoreCase("Finance")) {
            return repository.findById(2L).orElseThrow(() -> new RuntimeException("Department not found"));
        } else {
            return repository.findById(3L).orElseThrow(() -> new RuntimeException("Department not found"));
        }
    }

    @Override
    public List<CorrespondenceDepartmentResponse> findAll() {
        return mapperUtil.mapList(repository.findAll(), CorrespondenceDepartmentResponse.class);
    }

    @Override
    public CorrespondenceDepartmentResponse findById(Long id) {
        CorrespondenceDepartment department = repository.findById(id)
                .orElseThrow(() -> new ModelNotFoundException("Department no encontrado"));
        return mapperUtil.map(department, CorrespondenceDepartmentResponse.class);
    }

    @Override
    public CorrespondenceDepartmentResponse create(CorrespondenceDepartmentRequest dto) {
        CorrespondenceDepartment entity = mapperUtil.map(dto, CorrespondenceDepartment.class);
        return mapperUtil.map(repository.save(entity), CorrespondenceDepartmentResponse.class);
    }

    @Override
    public CorrespondenceDepartmentResponse update(Long id, CorrespondenceDepartmentRequest dto) {
        CorrespondenceDepartment existing = repository.findById(id)
                .orElseThrow(() -> new ModelNotFoundException("Deparment no encontrado"));
        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setEmail(dto.getEmail());
        return mapperUtil.map(repository.save(existing), CorrespondenceDepartmentResponse.class);
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ModelNotFoundException("Department no encontrado");
        }
        repository.deleteById(id);
    }
}
