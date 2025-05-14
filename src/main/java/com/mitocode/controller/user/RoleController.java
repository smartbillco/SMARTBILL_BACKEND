package com.mitocode.controller.user;

import com.mitocode.dto.request.user.RoleRequest;
import com.mitocode.model.user.Role;
import com.mitocode.service.user.IRoleService;
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
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {

    private final IRoleService service;
    private final MapperUtil mapperUtil;

    @GetMapping
    public ResponseEntity<ApiResponseUtil<List<RoleRequest>>> findAll() {
        List<RoleRequest> list = mapperUtil.mapList(service.findAll(), RoleRequest.class);
        return ResponseEntity.ok(new ApiResponseUtil<>(true, "Roles encontrados", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseUtil<RoleRequest>> findById(@PathVariable("id") Integer id) {
        Role obj = service.findById(id);
        return ResponseEntity.ok(new ApiResponseUtil<>(true, "Rol encontrado", mapperUtil.map(obj, RoleRequest.class)));
    }

    @PostMapping
    public ResponseEntity<ApiResponseUtil<RoleRequest>> save(@Valid @RequestBody RoleRequest dto) {
        Role obj = service.save(mapperUtil.map(dto, Role.class));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(obj.getIdRole()).toUri();
        return ResponseEntity.created(location)
                .body(new ApiResponseUtil<>(true, "Rol creado exitosamente", mapperUtil.map(obj, RoleRequest.class)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseUtil<RoleRequest>> update(@Valid @PathVariable("id") Integer id, @RequestBody RoleRequest dto) {
        dto.setIdRole(id);
        Role obj = service.update(id, mapperUtil.map(dto, Role.class));
        return ResponseEntity.ok(new ApiResponseUtil<>(true, "Rol actualizado exitosamente", mapperUtil.map(obj, RoleRequest.class)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseUtil<Void>> delete(@PathVariable("id") Integer id) {
        service.delete(id);
        return ResponseEntity.ok(new ApiResponseUtil<>(true, "Rol eliminado exitosamente", null));
    }
}
