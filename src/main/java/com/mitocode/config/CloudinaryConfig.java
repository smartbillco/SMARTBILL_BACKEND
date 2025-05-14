package com.mitocode.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "duyclmhp0",
                "api_key", "748577457953894",
                "api_secret", "qr8NQ2qz0MPVsWJN5M2YbpWfWxc"
        ));
    }
}
