package com.mitocode.service;

import net.sourceforge.tess4j.TesseractException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ITextExtractOCRService {
    List<String> extractTextFromImage(MultipartFile imageFile) throws IOException, TesseractException;

    List<String> extractTextFromPdf(MultipartFile pdfFile) throws IOException, TesseractException;

    Map<String, String> extractInvoiceFieldsFromImage(MultipartFile imageFile) throws IOException, TesseractException;

    Map<String, String> extractInvoiceFieldsFromPdf(MultipartFile pdfFile) throws IOException, TesseractException;
}
