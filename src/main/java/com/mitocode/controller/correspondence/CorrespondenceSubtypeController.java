package com.mitocode.controller.correspondence;

import com.mitocode.dto.request.correspondence.CorrespondenceSubtypeRequest;
import com.mitocode.dto.response.correspondence.CorrespondenceSubtypeResponse;
import com.mitocode.dto.response.correspondence.CorrespondenceTypeResponse;
import com.mitocode.service.correspondence.ICorrespondenceSubtypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.mitocode.util.ApiResponseUtil;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/correspondence/subtype")
@RequiredArgsConstructor
public class CorrespondenceSubtypeController {

    private final ICorrespondenceSubtypeService service;

    @GetMapping
    public ResponseEntity<ApiResponseUtil<List<CorrespondenceSubtypeResponse>>> getAll() {
        List<CorrespondenceSubtypeResponse> list = service.findAll();
        return ResponseEntity.ok(new ApiResponseUtil<>(true, "Listado obtenido correctamente", list));
    }

    @GetMapping("/type/{typeId}")
    public ResponseEntity<ApiResponseUtil<List<CorrespondenceSubtypeResponse>>> getByType(@PathVariable Long typeId) {
        List<CorrespondenceSubtypeResponse> list = service.findByType(typeId);
        return ResponseEntity.ok(new ApiResponseUtil<>(true, "Subtipos obtenidos correctamente", list));
    }

    @PostMapping
    public ResponseEntity<ApiResponseUtil<CorrespondenceSubtypeResponse>> create(@RequestBody CorrespondenceSubtypeRequest dto) {
        CorrespondenceSubtypeResponse created = service.create(dto);
        return ResponseEntity.ok(new ApiResponseUtil<>(true, "Creado correctamente", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseUtil<CorrespondenceSubtypeResponse>> update(@PathVariable Long id, @RequestBody CorrespondenceSubtypeRequest dto) {
        CorrespondenceSubtypeResponse updated = service.update(id, dto);
        return ResponseEntity.ok(new ApiResponseUtil<>(true, "Actualizado correctamente", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseUtil<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(new ApiResponseUtil<>(true, "Eliminado correctamente", null));
    }
}
