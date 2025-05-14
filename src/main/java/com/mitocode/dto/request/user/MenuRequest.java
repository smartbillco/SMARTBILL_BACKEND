package com.mitocode.dto.request.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MenuRequest {

    private Integer idMenu;
    private String icon;
    private String title;
    private String url;
    private String caret;
    private List<MenuRequest> submenu; // Lista de submenús opcional
}
