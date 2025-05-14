package com.mitocode.service.impl.user;

import com.mitocode.dto.request.user.UserConfigRequest;
import com.mitocode.dto.response.UserConfigResponse;
import com.mitocode.exception.ModelNotFoundException;
import com.mitocode.model.user.UserConfig;
import com.mitocode.repo.user.IUserConfigRepo;
import com.mitocode.security.JwtTokenUtil;
import com.mitocode.service.user.IUserConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserConfigServiceImpl implements IUserConfigService {

    private final IUserConfigRepo userConfigRepo;
    private final JwtTokenUtil jwtTokenUtil;

    @Override
    public Optional<UserConfig> getUserConfigByUsername(String username) {
        return userConfigRepo.findByUserUsername(username)
                .or(() -> {
                    throw new ModelNotFoundException("configuracion para el usuario" + username + " no encontrada");
                });
    }

    @Override
    public Optional<UserConfig> getUserConfigById(Integer id) {
        return userConfigRepo.findByUserIdUser(id)
                .or(() -> {
                    throw new ModelNotFoundException("configuracion para el usuario" + id + " no encontrada");
                });
    }

    @Transactional
    public UserConfigResponse updateUserConfig(UserConfigRequest newConfig) {
        String username = jwtTokenUtil.getAuthenticatedUsername();
        Optional<UserConfig> existingConfig = userConfigRepo.findByUserUsername(username);

        if (existingConfig.isEmpty()) {
            throw new RuntimeException("Configuración no encontrada para el usuario: " + username);
        }

        UserConfig config = existingConfig.get();
        config.setCurrencyKey(newConfig.getCurrencyKey());
        config.setCurrency(newConfig.getCurrency());
        config.setStorageType(newConfig.getStorageType());
        config.setLanguage(newConfig.getLanguage());
        config.setSmsNotifications(newConfig.isSmsNotifications());
        config.setEmailNotifications(newConfig.isEmailNotifications());
        config.setPushNotifications(newConfig.isPushNotifications());

        userConfigRepo.save(config);

        // Mapear a UserConfigResponse
        UserConfigResponse response = new UserConfigResponse();
        response.setCurrencyKey(config.getCurrencyKey());
        response.setCurrency(config.getCurrency());
        response.setStorageType(config.getStorageType());
        response.setLanguage(config.getLanguage());
        response.setSmsNotifications(config.isSmsNotifications());
        response.setEmailNotifications(config.isEmailNotifications());
        response.setPushNotifications(config.isPushNotifications());

        return response;
    }
}
