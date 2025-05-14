package com.mitocode.service.customer;

import com.mitocode.dto.request.customer.ConsultProcRequest;
import com.mitocode.model.customer.Customer;
import com.mitocode.service.ICRUD;

import java.util.List;

public interface IConsultService extends ICRUD<Customer, Integer> {
    List<ConsultProcRequest> callProcedureOrFunctionNative();
    byte[] generateReport() throws Exception;
}
