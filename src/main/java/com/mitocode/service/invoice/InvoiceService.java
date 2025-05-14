package com.mitocode.service.invoice;

import com.mitocode.model.file.Invoice;
import com.mitocode.model.user.User;
import com.mitocode.service.ICRUD;
import com.mitocode.util.ApiResponseUtil;

import java.io.File;

public interface InvoiceService extends ICRUD<Invoice, Integer> {

    ApiResponseUtil<String> processXMLFile(File xmlFile, User user);
}
