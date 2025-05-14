package com.mitocode.service.auth;

import com.mitocode.model.user.User;

public interface ILoginService {

    User checkUsername(String username);
    void changePassword(String password, String username);
}
