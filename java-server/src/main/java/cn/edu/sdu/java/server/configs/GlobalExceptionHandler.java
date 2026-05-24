package cn.edu.sdu.java.server.configs;

import cn.edu.sdu.java.server.payload.response.DataResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(BadCredentialsException.class)
    public DataResponse handleBadCredentials(BadCredentialsException e) {
        return new DataResponse(1, null, "用户名或密码错误");
    }

    @ExceptionHandler(Exception.class)
    public DataResponse handleException(Exception e) {
        e.printStackTrace();
        return new DataResponse(1, null, "服务器内部错误: " + e.getMessage());
    }
}
