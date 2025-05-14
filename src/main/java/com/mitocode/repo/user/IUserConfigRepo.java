package com.mitocode.repo.user;

import com.mitocode.model.user.UserConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IUserConfigRepo extends JpaRepository<UserConfig, Long> {
    Optional<UserConfig> findByUserUsername(String username);
    Optional<UserConfig> findByUserIdUser(Integer id);
}