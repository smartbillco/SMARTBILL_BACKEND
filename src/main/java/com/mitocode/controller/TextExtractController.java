package com.mitocode.controller;

import com.mitocode.service.ITextExtractOCRService;
import com.mitocode.util.ApiResponseUtil;
import lombok.RequiredArgsConstructor;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ocr")
@RequiredArgsConstructor
public class TextExtractController {

    private final ITextExtractOCRService textExtractOCRService;

    @PostMapping("/extractImage")
    public ResponseEntity<ApiResponseUtil<List<String>>> extractTextFromImage(@RequestParam MultipartFile multipartFile) {
        try {
            List<String> extractedText = textExtractOCRService.extractTextFromImage(multipartFile);
            ApiResponseUtil<List<String>> apiResponse = new ApiResponseUtil<>(true, "Texto extraído correctamente", extractedText);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            ApiResponseUtil<List<String>> apiResponse = new ApiResponseUtil<>(false, "Error al extraer texto de la imagen", Collections.emptyList());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @PostMapping("/extractPdf")
    public ResponseEntity<ApiResponseUtil<List<String>>> extractTextFromPDF(@RequestParam("file") MultipartFile file) {
        try {
            List<String> extractedData = textExtractOCRService.extractTextFromPdf(file);
            ApiResponseUtil<List<String>> apiResponse = new ApiResponseUtil<>(true, "Texto extraído correctamente", extractedData);
            return ResponseEntity.ok(apiResponse);
        } catch (IOException e) {
            ApiResponseUtil<List<String>> apiResponse = new ApiResponseUtil<>(false, "Error al procesar el PDF", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        } catch (IllegalArgumentException e) {
            ApiResponseUtil<List<String>> apiResponse = new ApiResponseUtil<>(false, "Error de entrada", null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
        } catch (TesseractException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/extract-details/pdf")
    public ResponseEntity<ApiResponseUtil<Map<String, String>>> extractDetailsFromPdf(@RequestParam("file") MultipartFile file) {
        try {
            Map<String, String> data = textExtractOCRService.extractInvoiceFieldsFromPdf(file);
            ApiResponseUtil<Map<String, String>> apiResponse = new ApiResponseUtil<>(true, "Texto extraído correctamente", data);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            ApiResponseUtil<Map<String, String>> apiResponse = new ApiResponseUtil<>(false, "Error al procesar el PDF", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @PostMapping("/extract-details/image")
    public ResponseEntity<ApiResponseUtil<Map<String, String>>> extractDetailsFromImage(@RequestParam("file") MultipartFile file) throws TesseractException, IOException {
        Map<String, String> data = textExtractOCRService.extractInvoiceFieldsFromImage(file);
        ApiResponseUtil<Map<String, String>> apiResponse = new ApiResponseUtil<>(true, "Texto extraído correctamente", data);
        return ResponseEntity.ok(apiResponse);
    }
}

