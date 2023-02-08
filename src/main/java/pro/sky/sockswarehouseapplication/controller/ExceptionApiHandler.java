package pro.sky.sockswarehouseapplication.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pro.sky.sockswarehouseapplication.exceptions.BadRequestException;
import pro.sky.sockswarehouseapplication.exceptions.FileProcessingException;
import pro.sky.sockswarehouseapplication.exceptions.ModelNotFoundException;

@RestControllerAdvice
public class ExceptionApiHandler {

    @ExceptionHandler(FileProcessingException.class)
    public ResponseEntity<String> fileProcessingException(FileProcessingException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<String> BadRequestException(BadRequestException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
    }

    @ExceptionHandler(ModelNotFoundException.class)
    public ResponseEntity<String> modelNotFoundException(ModelNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> jsonParseException(HttpMessageNotReadableException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("Ошибка в теле запроса, введите корректные данные");
    }
}
