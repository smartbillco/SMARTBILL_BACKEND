package com.mitocode.controller.user;

import com.mitocode.dto.request.user.DocTypeRequest;
import com.mitocode.model.user.DocType;
import com.mitocode.service.user.IDocTypeService;
import com.mitocode.util.ApiResponseUtil;
import com.mitocode.util.MapperUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/doctypes")
@RequiredArgsConstructor
public class DocTypeController {

    private final IDocTypeService service;
    private final MapperUtil mapperUtil;

    @GetMapping
    public ResponseEntity<ApiResponseUtil<List<DocTypeRequest>>> findAll() {
        List<DocTypeRequest> list = mapperUtil.mapList(service.findAll(), DocTypeRequest.class);
        return ResponseEntity.ok(new ApiResponseUtil<>(true, "Tipos de documento encontrados", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseUtil<DocTypeRequest>> findById(@PathVariable("id") Integer id) {
        DocType obj = service.findById(id);
        return ResponseEntity.ok(new ApiResponseUtil<>(true, "Tipo de documento encontrado", mapperUtil.map(obj, DocTypeRequest.class)));
    }

    @PostMapping
    public ResponseEntity<ApiResponseUtil<DocTypeRequest>> save(@Valid @RequestBody DocTypeRequest dto) {
        DocType obj = service.save(mapperUtil.map(dto, DocType.class));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(obj.getIdDocType()).toUri();
        return ResponseEntity.created(location)
                .body(new ApiResponseUtil<>(true, "Tipo de documento creado exitosamente", mapperUtil.map(obj, DocTypeRequest.class)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseUtil<DocTypeRequest>> update(@Valid @PathVariable("id") Integer id, @RequestBody DocTypeRequest dto) {
        dto.setIdDocType(id);
        DocType obj = service.update(id, mapperUtil.map(dto, DocType.class));
        return ResponseEntity.ok(new ApiResponseUtil<>(true, "Tipo de documento actualizado exitosamente", mapperUtil.map(obj, DocTypeRequest.class)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseUtil<Void>> delete(@PathVariable("id") Integer id) {
        service.delete(id);
        return ResponseEntity.ok(new ApiResponseUtil<>(true, "Tipo de documento eliminado exitosamente", null));
    }
}
