package com.mitocode.dto.request.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileRequest {

    private String fileName;
    private String storageType;
    private String fileType;
    private String fileUrl;
}

