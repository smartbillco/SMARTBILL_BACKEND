package com.mitocode.service.impl.customer;

import com.mitocode.model.customer.Customer;
import com.mitocode.repo.customer.ICustomerRepo;
import com.mitocode.repo.IGenericRepo;
import com.mitocode.service.customer.ICustomerService;
import com.mitocode.service.impl.CRUDImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl extends CRUDImpl<Customer, Integer> implements ICustomerService {

    @Autowired
    private ICustomerRepo repo;

    @Override
    protected IGenericRepo<Customer, Integer> getRepo() {
        return repo;
    }

    @Override
    public Page<Customer> listPage(Pageable pageable) {
        return repo.findAll(pageable);
    }

    @Override
    public boolean existsByDocumentNumber(String documentNumber) {
        return repo.findByDocumentNumber(documentNumber).isPresent();
    }
}
