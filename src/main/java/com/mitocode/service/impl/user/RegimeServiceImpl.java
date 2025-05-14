package com.mitocode.service.impl.user;

import com.mitocode.model.user.Regime;
import com.mitocode.repo.IGenericRepo;
import com.mitocode.repo.user.IRegimeRepo;
import com.mitocode.service.user.IRegimeService;
import com.mitocode.service.impl.CRUDImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegimeServiceImpl extends CRUDImpl<Regime, Integer> implements IRegimeService {

    @Autowired
    private IRegimeRepo repo;

    @Override
    protected IGenericRepo<Regime, Integer> getRepo() {
        return repo;
    }

    @Override
    public Regime findByNameRegime(String name) {
        return repo.findByNameRegime(name);
    }
}
