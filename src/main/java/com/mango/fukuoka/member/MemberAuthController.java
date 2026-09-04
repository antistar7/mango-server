package com.mango.fukuoka.member;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class MemberAuthController {

    private final MemberAuthService authService;

    public MemberAuthController(MemberAuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public MemberAuthService.AuthResponse signup(
            @RequestBody MemberAuthService.SignupRequest request
    ) {
        return authService.signup(request);
    }

    @PostMapping("/login")
    public MemberAuthService.AuthResponse login(
            @RequestBody MemberAuthService.LoginRequest request
    ) {
        return authService.login(request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            @RequestHeader(value = "Authorization", required = false)
            String authorization
    ) {
        authService.logout(bearerToken(authorization));
    }

    static MemberPrincipal requireMember(Authentication authentication) {
        if (authentication == null
                || !(authentication.getPrincipal() instanceof MemberPrincipal principal)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "로그인이 필요합니다."
            );
        }

        return principal;
    }

    static Long optionalMemberId(Authentication authentication) {
        if (authentication != null
                && authentication.getPrincipal() instanceof MemberPrincipal principal) {
            return principal.id();
        }

        return null;
    }

    static String bearerToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }

        return authorization.substring("Bearer ".length()).trim();
    }
}
