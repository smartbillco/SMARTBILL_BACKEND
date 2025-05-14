package com.mitocode.config;

import com.mitocode.dto.request.user.UserRequest;
import com.mitocode.dto.response.UserResponse;
import com.mitocode.model.user.User;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapperConfig {

    @Bean(name = "defaultMapper")
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        // Configuración para mapear Customer a userRequest
        modelMapper.addMappings(new PropertyMap<User, UserRequest>() {
            @Override
            protected void configure() {
                map().setUsername(source.getUsername());
                skip(destination.getPassword()); // Omitir el campo de la contraseña en UserDTO
                map().setRegime(source.getRegime().getNameRegime());
                map().setDocumentType(source.getDocumentType().getNameDocument());
            }
        });

        // 🔹 Mapeo de User → UserResponse (para evitar conflicto en el error actual)
        modelMapper.addMappings(new PropertyMap<User, UserResponse>() {
            @Override
            protected void configure() {
                map().setUsername(source.getUsername());
                //skip(destination.getPassword()); // Omitir el campo de la contraseña en UserDTO
                map().setRegime(source.getRegime().getNameRegime()); // Usa nameRegime
                map().setDocumentType(source.getDocumentType().getNameDocument()); // Usa nameDocument
            }
        });

        return modelMapper;
    }

}