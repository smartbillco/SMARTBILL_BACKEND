package com.mitocode.repo.correspondence;

import com.mitocode.model.file.Invoice;
import com.mitocode.model.user.User;
import com.mitocode.repo.IGenericRepo;


public interface InvoiceRepo extends IGenericRepo<Invoice, Integer> {
    boolean existsByUserAndInvoiceCode(User user, String invoiceCode);
}
