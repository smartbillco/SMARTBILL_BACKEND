package com.mitocode.service.impl.customer;

import com.mitocode.dto.request.customer.ConsultProcRequest;
import com.mitocode.model.customer.Customer;
import com.mitocode.repo.customer.IConsultRepo;
import com.mitocode.repo.IGenericRepo;
import com.mitocode.service.customer.IConsultService;
import com.mitocode.service.impl.CRUDImpl;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ConsultServiceImpl extends CRUDImpl<Customer, Integer> implements IConsultService {

    @Autowired
    private IConsultRepo consultRepo;

    @Override
    protected IGenericRepo<Customer, Integer> getRepo() {
        return consultRepo;
    }

    @Override
    public List<ConsultProcRequest> callProcedureOrFunctionNative() {
        List<ConsultProcRequest> list = new ArrayList<>();

        consultRepo.callProcedureOrFunctionNative().forEach(e -> {
            ConsultProcRequest dto = new ConsultProcRequest();

            // Establecer la cantidad como el phone_number convertido
            String phoneNumberStr = String.valueOf(e[0]);
            Integer phoneNumber = phoneNumberStr != null && !phoneNumberStr.isEmpty() ? Integer.parseInt(phoneNumberStr) : null;

            dto.setQuantity(phoneNumber);
            dto.setConsultdate(String.valueOf(e[1]));

            list.add(dto);
        });
        return list;
    }

    @Override
    public byte[] generateReport() throws Exception {
        byte[] data;

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("txt_title", "caam.ingenierias@gmail.com");

        File file = new ClassPathResource("/reports/consultas.jasper").getFile();
        JasperPrint print = JasperFillManager.fillReport(file.getPath(), parameters, new JRBeanCollectionDataSource(callProcedureOrFunctionNative()));
        data = JasperExportManager.exportReportToPdf(print);

        return data;
    }
}
