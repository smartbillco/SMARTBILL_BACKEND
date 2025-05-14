package com.mitocode.service.impl.auth;

import com.mitocode.model.user.User;
import com.mitocode.repo.auth.ILoginRepo;
import com.mitocode.service.auth.ILoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements ILoginService {

    private final ILoginRepo repo;
    private final PasswordEncoder bcrypt;

    @Override
    public User checkUsername(String username) {
        return repo.checkUsername(username);
    }

    @Override
    public void changePassword(String password, String username) {
        repo.changePassword(bcrypt.encode(password), username);
    }
}
