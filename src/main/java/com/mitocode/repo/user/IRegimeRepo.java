package com.mitocode.repo.user;

import com.mitocode.model.user.Regime;
import com.mitocode.repo.IGenericRepo;

public interface IRegimeRepo extends IGenericRepo<Regime, Integer> {
    Regime findByNameRegime(String nameRegime);
}
