package com.mitocode.exception;

import com.mitocode.exception.user.DocumentAlreadyRegisteredException;
import com.mitocode.exception.user.InvalidDocumentTypeException;
import com.mitocode.exception.user.UsernameAlreadyExistsException;
import com.mitocode.util.ApiResponseUtil;
import lombok.NonNull;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestControllerAdvice  // Anotación para indicar que esta clase maneja excepciones de forma global
public class ResponseExceptionHandler extends ResponseEntityExceptionHandler {

    // Mapa estático que asocia clases de excepciones con códigos de estado HTTP
    private static final Map<Class<? extends Exception>, HttpStatus> exceptionStatusMap = new HashMap<>();

    static {
        // Inicializa el mapa con excepciones específicas y sus códigos de estado
        exceptionStatusMap.put(UsernameAlreadyExistsException.class, HttpStatus.CONFLICT);
        exceptionStatusMap.put(DocumentAlreadyRegisteredException.class, HttpStatus.CONFLICT);
        exceptionStatusMap.put(InvalidDocumentTypeException.class, HttpStatus.BAD_REQUEST);
    }

    // Metodo privado para construir un ApiResponse a partir de una excepción
    private ResponseEntity<ApiResponseUtil<String>> buildErrorResponse(Exception ex, HttpStatus status, WebRequest request) {
        String errorDetails = "Error en: " + request.getDescription(false);  // Obtiene detalles de la solicitud
        ApiResponseUtil<String> response = new ApiResponseUtil<>(false, ex.getMessage(), null);  // Crea un ApiResponse de error
        return new ResponseEntity<>(response, status);  // Devuelve un ResponseEntity con ApiResponse y el código HTTP
    }

    @ExceptionHandler(Exception.class)  // Maneja cualquier excepción no especificada
    public ResponseEntity<ApiResponseUtil<String>> handleAllExceptions(Exception ex, WebRequest request) {
        return buildErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler(ModelNotFoundException.class)  // Maneja excepciones de modelo no encontrado
    public ResponseEntity<ApiResponseUtil<String>> handleModelNotFoundException(ModelNotFoundException ex, WebRequest request) {
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(ArithmeticException.class)  // Maneja excepciones aritméticas
    public ResponseEntity<ApiResponseUtil<String>> handleArithmeticException(ArithmeticException ex, WebRequest request) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, request);
    }

    // Sobrescribe el manejo de errores de validación de argumentos en los controladores
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            @NonNull MethodArgumentNotValidException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {

        // Recopila los errores de validación y los une en un solo string
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField().concat(": ").concat(Objects.requireNonNull(err.getDefaultMessage())))
                .collect(Collectors.joining(", "));

        ApiResponseUtil<String> response = new ApiResponseUtil<>(false, "Validación fallida", msg);  // Crea un ApiResponse para el error de validación

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);  // Devuelve la respuesta con el estado HTTP
    }

    // Maneja excepciones específicas
    @ExceptionHandler({DocumentAlreadyRegisteredException.class, InvalidDocumentTypeException.class})
    public ResponseEntity<ApiResponseUtil<String>> handleSpecificExceptions(Exception ex, WebRequest request) {
        HttpStatus status = exceptionStatusMap.getOrDefault(ex.getClass(), HttpStatus.INTERNAL_SERVER_ERROR);  // Obtiene el estado del mapa o usa 500
        return buildErrorResponse(ex, status, request);
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ApiResponseUtil<String>> handleUsernameAlreadyExistsException(UsernameAlreadyExistsException ex, WebRequest request) {
        return buildErrorResponse(ex, HttpStatus.CONFLICT, request);
    }

}
