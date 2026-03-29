package com.pathkeeper.backend.controller.auth;

import com.pathkeeper.backend.controller.auth.dto.LoginRequest;
import com.pathkeeper.backend.controller.auth.dto.SignupRequest;
import com.pathkeeper.backend.controller.auth.dto.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "회원가입, 로그인, 파트너 연결 API")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Operation(summary = "이메일 회원가입", description = "새로운 유저(보호자/피보호자)를 등록합니다.")
    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@Valid @RequestBody SignupRequest request) {

        return ResponseEntity.ok(null); // 임시 반환값
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하여 Access Token을 발급받습니다.")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(null); // 임시 반환값
    }
}