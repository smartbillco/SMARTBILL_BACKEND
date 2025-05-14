package com.mitocode.service.customer;

import com.mitocode.model.customer.Customer;
import com.mitocode.service.ICRUD;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ICustomerService extends ICRUD<Customer, Integer> {

    Page<Customer> listPage(Pageable pageable);
    // Nuevo metodo para verificar la existencia del documentNumber
    boolean existsByDocumentNumber(String documentNumber);

}

