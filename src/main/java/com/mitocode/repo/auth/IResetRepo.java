package com.mitocode.repo.auth;

import com.mitocode.model.auth.Reset;
import com.mitocode.repo.IGenericRepo;

import java.time.LocalDateTime;
import java.util.List;

public interface IResetRepo extends IGenericRepo<Reset, Integer> {

    //FROM ResetMail rm WHERE rm.random = ?
    Reset findByRandom(String random);

    // Metodo para encontrar todos los tokens expirados
    List<Reset> findAllByExpirationDateBefore(LocalDateTime now);
}
