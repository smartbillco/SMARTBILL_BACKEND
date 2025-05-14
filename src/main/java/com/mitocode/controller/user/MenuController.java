package com.mitocode.controller.user;

import com.mitocode.dto.request.user.MenuRequest;
import com.mitocode.service.user.IMenuService;
import com.mitocode.util.ApiResponseUtil;
import com.mitocode.util.MapperUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/menus")
@RequiredArgsConstructor
public class MenuController {

    private final IMenuService service;
    private final MapperUtil mapperUtil;

    @PostMapping("/user")
    public ResponseEntity<ApiResponseUtil<?>> getMenusByUser(@RequestBody String username) {
        try {
            List<MenuRequest> menusDTO = mapperUtil.mapList(service.getMenusByUsername(username), MenuRequest.class);
            return ResponseEntity.ok(new ApiResponseUtil<>(true, "Menús obtenidos correctamente", menusDTO));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ApiResponseUtil<>(false, "Error al obtener menús para el usuario", null));
        }
    }
}
