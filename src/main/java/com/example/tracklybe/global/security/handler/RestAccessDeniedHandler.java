package com.example.tracklybe.global.security.handler;

import com.example.tracklybe.domain.common.dto.ApiError;
import com.example.tracklybe.domain.common.dto.ApiResponse;
import com.example.tracklybe.global.exception.enumeration.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        writeError(response, ErrorCode.FORBIDDEN, "해당 리소스에 접근할 권한이 없습니다.");
    }

    private void writeError(HttpServletResponse response, ErrorCode errorCode, String detail) throws IOException {
        response.setStatus(errorCode.getStatus().value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiError apiError = ApiError.of(errorCode, detail);
        ApiResponse<Void> body = ApiResponse.fail(apiError);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
