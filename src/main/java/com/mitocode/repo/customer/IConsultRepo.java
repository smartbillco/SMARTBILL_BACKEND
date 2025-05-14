package com.mitocode.repo.customer;

import com.mitocode.model.customer.Customer;
import com.mitocode.repo.IGenericRepo;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IConsultRepo extends IGenericRepo<Customer, Integer> {

    @Query(value = "select * from fn_list()", nativeQuery = true)
    List<Object[]> callProcedureOrFunctionNative();
}
