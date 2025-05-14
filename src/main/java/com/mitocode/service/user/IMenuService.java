package com.mitocode.service.user;

import com.mitocode.model.user.Menu;

import java.util.List;

public interface IMenuService {

    List<Menu> getMenusByUsername(String username);

}
