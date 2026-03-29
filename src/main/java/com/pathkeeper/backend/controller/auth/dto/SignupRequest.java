package com.pathkeeper.backend.controller.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "회원가입 요청 DTO")
public record SignupRequest(

        @Schema(description = "사용자 이메일 (로그인 아이디)", example = "user@naver.com")
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email,

        @Schema(description = "비밀번호 (8~16자 영문 대소문자, 숫자, 특수문자 포함)", example = "Password123!")
        @NotBlank(message = "비밀번호는 필수입니다.")
        @Pattern(regexp = "(?=.*[0-9])(?=.*[a-zA-Z])(?=.*\\W)(?=\\S+$).{8,16}",
                message = "비밀번호는 8~16자 영문 대소문자, 숫자, 특수문자를 사용하세요.")
        String password,

        @Schema(description = "사용자 이름", example = "홍길동")
        @NotBlank(message = "이름은 필수입니다.")
        String name
) {}