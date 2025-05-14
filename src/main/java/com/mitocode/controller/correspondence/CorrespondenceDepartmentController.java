package com.mitocode.controller.correspondence;

import com.mitocode.dto.request.correspondence.CorrespondenceDepartmentRequest;
import com.mitocode.dto.response.correspondence.CorrespondenceDepartmentResponse;
import com.mitocode.service.correspondence.ICorrespondenceDepartmentService;
import com.mitocode.util.ApiResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/correspondence/department")
@RequiredArgsConstructor
public class CorrespondenceDepartmentController {

    private final ICorrespondenceDepartmentService service;

    @GetMapping
    public ResponseEntity<ApiResponseUtil<List<CorrespondenceDepartmentResponse>>> getAll() {
        List<CorrespondenceDepartmentResponse> list = service.findAll();
        return ResponseEntity.ok(new ApiResponseUtil<>(true, "Departamentos obtenidos correctamente", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseUtil<CorrespondenceDepartmentResponse>> getById(@PathVariable Long id) {
        CorrespondenceDepartmentResponse response = service.findById(id);
        return ResponseEntity.ok(new ApiResponseUtil<>(true, "Departamento obtenido correctamente", response));
    }

    @PostMapping
    public ResponseEntity<ApiResponseUtil<CorrespondenceDepartmentResponse>> create(@RequestBody CorrespondenceDepartmentRequest dto) {
        CorrespondenceDepartmentResponse created = service.create(dto);
        return ResponseEntity.ok(new ApiResponseUtil<>(true, "Departamento creado correctamente", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseUtil<CorrespondenceDepartmentResponse>> update(@PathVariable Long id, @RequestBody CorrespondenceDepartmentRequest dto) {
        CorrespondenceDepartmentResponse updated = service.update(id, dto);
        return ResponseEntity.ok(new ApiResponseUtil<>(true, "Departamento actualizado correctamente", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseUtil<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(new ApiResponseUtil<>(true, "Departamento eliminado correctamente", null));
    }
}
