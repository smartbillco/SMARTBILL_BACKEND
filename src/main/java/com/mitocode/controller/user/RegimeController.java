package com.mitocode.controller.user;

import com.mitocode.dto.request.user.RegimeRequest;
import com.mitocode.model.user.Regime;
import com.mitocode.service.user.IRegimeService;
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
@RequestMapping("/regimes")
@RequiredArgsConstructor
public class RegimeController {

    private final IRegimeService service;
    private final MapperUtil mapperUtil;

    @GetMapping
    public ResponseEntity<ApiResponseUtil<List<RegimeRequest>>> findAll() {
        List<RegimeRequest> list = mapperUtil.mapList(service.findAll(), RegimeRequest.class);
        return ResponseEntity.ok(new ApiResponseUtil<>(true, "Regímenes encontrados", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseUtil<RegimeRequest>> findById(@PathVariable("id") Integer id) {
        Regime obj = service.findById(id);
        return ResponseEntity.ok(new ApiResponseUtil<>(true, "Régimen encontrado", mapperUtil.map(obj, RegimeRequest.class)));
    }

    @PostMapping
    public ResponseEntity<ApiResponseUtil<RegimeRequest>> save(@Valid @RequestBody RegimeRequest dto) {
        Regime obj = service.save(mapperUtil.map(dto, Regime.class));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(obj.getIdRegime()).toUri();
        return ResponseEntity.created(location).body(new ApiResponseUtil<>(true, "Régimen creado exitosamente", mapperUtil.map(obj, RegimeRequest.class)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseUtil<RegimeRequest>> update(@Valid @PathVariable("id") Integer id, @RequestBody RegimeRequest dto) {
        dto.setIdRegime(id);
        Regime obj = service.update(id, mapperUtil.map(dto, Regime.class));
        return ResponseEntity.ok(new ApiResponseUtil<>(true, "Régimen actualizado exitosamente", mapperUtil.map(obj, RegimeRequest.class)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseUtil<Void>> delete(@PathVariable("id") Integer id) {
        service.delete(id);
        return ResponseEntity.ok(new ApiResponseUtil<>(true, "Régimen eliminado exitosamente", null));
    }
}
