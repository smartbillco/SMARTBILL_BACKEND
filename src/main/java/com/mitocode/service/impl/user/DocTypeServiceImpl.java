package com.mitocode.service.impl.user;

import com.mitocode.model.user.DocType;
import com.mitocode.repo.user.IDocTypeRepo;
import com.mitocode.repo.IGenericRepo;
import com.mitocode.service.user.IDocTypeService;
import com.mitocode.service.impl.CRUDImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocTypeServiceImpl extends CRUDImpl<DocType, Integer> implements IDocTypeService {

    @Autowired
    private IDocTypeRepo repo;

    @Override
    protected IGenericRepo<DocType, Integer> getRepo() {
        return repo;
    }

    @Override
    public DocType findByNameDocument(String name) {
        return repo.findByNameDocument(name);
    }

    @Override
    public DocType findById(Integer id) {
        return repo.findById(id).orElse(null);
    }

}
