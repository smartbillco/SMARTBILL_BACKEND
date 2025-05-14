package com.mitocode.controller.correspondence;

import com.mitocode.dto.request.correspondence.CorrespondenceTypeRequest;
import com.mitocode.dto.response.correspondence.CorrespondenceTypeResponse;
import com.mitocode.service.correspondence.ICorrespondenceTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.mitocode.util.ApiResponseUtil;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/correspondence/type")
@RequiredArgsConstructor
public class CorrespondenceTypeController {

    private final ICorrespondenceTypeService service;

    @GetMapping
    public ResponseEntity<ApiResponseUtil<List<CorrespondenceTypeResponse>>> getAll() {
        List<CorrespondenceTypeResponse> list = service.findAll();
        return ResponseEntity.ok(new ApiResponseUtil<>(true, "Listado obtenido correctamente", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseUtil<CorrespondenceTypeResponse>> getById(@PathVariable Long id) {
        CorrespondenceTypeResponse response = service.findById(id);
        return ResponseEntity.ok(new ApiResponseUtil<>(true, "Elemento encontrado", response));
    }

    @PostMapping
    public ResponseEntity<ApiResponseUtil<CorrespondenceTypeResponse>> create(@RequestBody CorrespondenceTypeRequest dto) {
        CorrespondenceTypeResponse created = service.create(dto);
        return ResponseEntity.ok(new ApiResponseUtil<>(true, "Creado correctamente", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseUtil<CorrespondenceTypeResponse>> update(@PathVariable Long id, @RequestBody CorrespondenceTypeRequest dto) {
        CorrespondenceTypeResponse updated = service.update(id, dto);
        return ResponseEntity.ok(new ApiResponseUtil<>(true, "Actualizado correctamente", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseUtil<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(new ApiResponseUtil<>(true, "Eliminado correctamente", null));
    }
}

