package com.accounts.project.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ResourceNotFound extends RuntimeException{

    public ResourceNotFound(String resourceName,String fieldName,String fieldValue){
        super(String.format("%s not found for %s and %s",resourceName,fieldName,fieldValue));
    }

}
