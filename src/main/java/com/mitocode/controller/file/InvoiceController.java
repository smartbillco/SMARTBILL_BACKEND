package com.mitocode.controller.file;

import com.mitocode.service.impl.invoice.InvoiceServiceImpl;
import com.mitocode.util.ApiResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/invoices")
@RequiredArgsConstructor
public class InvoiceController {
    private final InvoiceServiceImpl invoiceService;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ApiResponseUtil<String>> uploadXML(@RequestParam("files") List<MultipartFile> files) {
        ApiResponseUtil<String> response = invoiceService.processXMLFiles(files);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}
