package com.mitocode.service.user;

import com.mitocode.model.user.DocType;
import com.mitocode.service.ICRUD;

public interface IDocTypeService extends ICRUD<DocType, Integer> {

    DocType findByNameDocument(String nameDocument);

    DocType findById(Integer id);
}