package com.mitocode.repo.user;

import com.mitocode.model.user.DocType;
import com.mitocode.repo.IGenericRepo;

public interface IDocTypeRepo extends IGenericRepo<DocType, Integer> {
    DocType findByNameDocument(String nameDocument);
}
