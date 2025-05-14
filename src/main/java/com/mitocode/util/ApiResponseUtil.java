package com.mitocode.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponseUtil<T> {
    private boolean success;
    private String message;
    private T data;
}
