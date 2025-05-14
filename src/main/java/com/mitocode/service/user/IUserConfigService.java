package com.mitocode.service.user;

import com.mitocode.model.user.UserConfig;

import java.util.Optional;

public interface IUserConfigService {
    Optional<UserConfig> getUserConfigByUsername(String username);

    Optional<UserConfig> getUserConfigById(Integer id);
}
