package com.mitocode.config;

import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.Tesseract;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
@Slf4j
public class TesseractConfig {

    private final TesseractProperties properties;

    public TesseractConfig(TesseractProperties properties) {
        this.properties = properties;
    }

    @Bean
    public Tesseract tesseract() {
        Tesseract tesseract = new Tesseract();

        //HABILITAR PARA EJECUCION LOCAL
        //tesseract.setDatapath(properties.getDatapath());
        tesseract.setLanguage(properties.getLanguage());
        tesseract.setOcrEngineMode(ITessAPI.TessOcrEngineMode.OEM_LSTM_ONLY);
        tesseract.setTessVariable("tessedit_char_whitelist", "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-/:.,$€");
        tesseract.setTessVariable("preserve_interword_spaces", "1");
        tesseract.setTessVariable("user_defined_dpi", "300");
        tesseract.setTessVariable("textord_min_linesize", "2.5");
        tesseract.setTessVariable("tessedit_pageseg_mode", "6");
        log.info("Datapath absoluto Tesseract: {}", new File(properties.getDatapath()));

        return tesseract;
    }
}
