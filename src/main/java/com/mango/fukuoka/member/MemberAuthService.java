package com.mango.fukuoka.member;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class MemberAuthService {

    private static final Pattern EMAIL = Pattern.compile(
            "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"
    );
    private static final int SESSION_DAYS = 30;

    private final MemberRepository memberRepository;
    private final MemberSessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberAuthService(
            MemberRepository memberRepository,
            MemberSessionRepository sessionRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.memberRepository = memberRepository;
        this.sessionRepository = sessionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(transactionManager = "fukuokaTransactionManager")
    public AuthResponse signup(SignupRequest request) {
        String email = normalizeEmail(request.email());
        String nickname = normalizeNickname(request.nickname());
        String password = requirePassword(request.password());

        if (memberRepository.existsByEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "이미 가입된 이메일입니다."
            );
        }

        if (memberRepository.existsByNickname(nickname)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "이미 사용 중인 닉네임입니다."
            );
        }

        Member member = memberRepository.save(
                Member.create(
                        email,
                        passwordEncoder.encode(password),
                        nickname
                )
        );

        return issueSession(member);
    }

    @Transactional(transactionManager = "fukuokaTransactionManager")
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        String password = request.password() == null ? "" : request.password();

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(MemberAuthService::badCredentials);

        if (!passwordEncoder.matches(password, member.getPasswordHash())) {
            throw badCredentials();
        }

        return issueSession(member);
    }

    @Transactional(transactionManager = "fukuokaTransactionManager")
    public void logout(String token) {
        if (token == null || token.isBlank()) {
            return;
        }

        sessionRepository.deleteByTokenHash(hashToken(token));
    }

    @Transactional(
            transactionManager = "fukuokaTransactionManager",
            readOnly = true
    )
    public Optional<MemberPrincipal> findPrincipal(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        return sessionRepository
                .findByTokenHash(hashToken(token))
                .filter(session -> !session.expired())
                .map(session -> toPrincipal(session.getMember()));
    }

    @Transactional(
            transactionManager = "fukuokaTransactionManager",
            readOnly = true
    )
    public Member requireMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "로그인이 필요합니다."
                ));
    }

    private AuthResponse issueSession(Member member) {
        String token = UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");

        sessionRepository.save(
                MemberSession.create(
                        member,
                        hashToken(token),
                        LocalDateTime.now().plusDays(SESSION_DAYS)
                )
        );

        return new AuthResponse(
                token,
                new MemberResponse(
                        member.getId(),
                        member.getNickname()
                )
        );
    }

    private static MemberPrincipal toPrincipal(Member member) {
        return new MemberPrincipal(
                member.getId(),
                member.getEmail(),
                member.getNickname()
        );
    }

    private static String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("이메일을 입력해주세요.");
        }

        String normalized = email.trim().toLowerCase(Locale.ROOT);

        if (!EMAIL.matcher(normalized).matches() || normalized.length() > 200) {
            throw new IllegalArgumentException("이메일 형식이 올바르지 않습니다.");
        }

        return normalized;
    }

    private static String normalizeNickname(String nickname) {
        if (nickname == null) {
            throw new IllegalArgumentException("닉네임을 입력해주세요.");
        }

        String trimmed = nickname.trim();

        if (trimmed.length() < 2 || trimmed.length() > 20) {
            throw new IllegalArgumentException("닉네임은 2~20자로 입력해주세요.");
        }

        return trimmed;
    }

    private static String requirePassword(String password) {
        if (password == null || password.length() < 8 || password.length() > 72) {
            throw new IllegalArgumentException(
                    "비밀번호는 8자 이상이어야 합니다."
            );
        }

        return password;
    }

    private static String hashToken(String token) {
        try {
            byte[] digest = MessageDigest
                    .getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static ResponseStatusException badCredentials() {
        return new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "이메일 또는 비밀번호가 올바르지 않습니다."
        );
    }

    public record SignupRequest(
            String email,
            String password,
            String nickname
    ) {
    }

    public record LoginRequest(
            String email,
            String password
    ) {
    }

    public record MemberResponse(
            Long id,
            String nickname
    ) {
    }

    public record AuthResponse(
            String token,
            MemberResponse member
    ) {
    }
}
