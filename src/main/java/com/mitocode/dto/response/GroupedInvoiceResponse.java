package com.mitocode.dto.response;

import com.mitocode.dto.request.file.FileRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class GroupedInvoiceResponse {
    private String invoiceCode; // Código de la factura
    private List<FileResponse> files; // Lista de archivos relacionados

}
