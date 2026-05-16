package cn.edu.sdu.java.server.configs;

import cn.edu.sdu.java.server.payload.response.DataResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public DataResponse handleException(Exception e) {
        e.printStackTrace();
        return new DataResponse(1, null, "服务器内部错误: " + e.getMessage());
    }
}
