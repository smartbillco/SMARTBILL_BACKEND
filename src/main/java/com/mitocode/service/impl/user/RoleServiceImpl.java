package com.mitocode.service.impl.user;

import com.mitocode.model.user.Role;
import com.mitocode.repo.IGenericRepo;
import com.mitocode.repo.user.IRoleRepo;
import com.mitocode.service.user.IRoleService;
import com.mitocode.service.impl.CRUDImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends CRUDImpl<Role, Integer> implements IRoleService {

    @Autowired
    private IRoleRepo repo;

    @Override
    public Optional<Role> findByName(String name) {
        return repo.findByName(name);  // Buscar rol por nombre
    }

    @Override
    protected IGenericRepo<Role, Integer> getRepo() {
        return repo;
    }
}
