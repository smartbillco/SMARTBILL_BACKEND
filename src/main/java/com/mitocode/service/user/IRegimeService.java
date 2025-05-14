package com.mitocode.service.user;

import com.mitocode.model.user.Regime;
import com.mitocode.service.ICRUD;

public interface IRegimeService extends ICRUD<Regime, Integer> {

    Regime findByNameRegime(String nameRegime);

}
