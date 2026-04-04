package com.accounts.project.exception;

import com.accounts.project.dto.ErrorResponseDto;
import org.springframework.lang.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> validationErrors = new HashMap<>();
        List<ObjectError> listOfErrors = ex.getBindingResult().getAllErrors();
        listOfErrors.forEach((error)->
                { String fieldName = ((FieldError)error).getField();
                    String validationMessage = error.getDefaultMessage();
                    validationErrors.put(fieldName,validationMessage);
                }
                );
        return new ResponseEntity<>(validationErrors,HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CustomerAlreadyExists.class)
    public ResponseEntity<ErrorResponseDto> handleExceptionGlobally(CustomerAlreadyExists customerAlreadyExists, WebRequest webRequest){

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDto(webRequest.getDescription(false),HttpStatus.BAD_REQUEST, customerAlreadyExists.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(ResourceNotFound.class)
    public ResponseEntity<ErrorResponseDto> handleResourceNotFound(ResourceNotFound resourceNotFound,WebRequest webRequest){

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDto(webRequest.getDescription(false),HttpStatus.NOT_FOUND,resourceNotFound.getMessage(),LocalDateTime.now()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleRuntimeException(Exception exception,WebRequest webRequest){

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDto(webRequest.getDescription(false),HttpStatus.INTERNAL_SERVER_ERROR,exception.getMessage(),LocalDateTime.now()));
    }
}
