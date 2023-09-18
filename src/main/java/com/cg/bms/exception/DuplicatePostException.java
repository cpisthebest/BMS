package com.cg.bms.exception;

public class DuplicatePostException extends RuntimeException{
    public DuplicatePostException(String message){
        super(message);
    }
}
