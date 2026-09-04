package com.mango.fukuoka.member;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/me")
public class MemberMeController {

    private final MemberSaveService saveService;

    public MemberMeController(MemberSaveService saveService) {
        this.saveService = saveService;
    }

    @GetMapping
    public MemberAuthService.MemberResponse me(Authentication authentication) {
        MemberPrincipal member = MemberAuthController.requireMember(authentication);

        return new MemberAuthService.MemberResponse(
                member.id(),
                member.nickname()
        );
    }

    @GetMapping("/saves")
    public Map<String, List<Long>> saves(Authentication authentication) {
        MemberPrincipal member = MemberAuthController.requireMember(authentication);

        return Map.of(
                "contentIds",
                saveService.savedContentIds(member.id())
        );
    }

    @PutMapping("/saves/{contentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void save(
            Authentication authentication,
            @PathVariable Long contentId
    ) {
        MemberPrincipal member = MemberAuthController.requireMember(authentication);

        saveService.save(member.id(), contentId);
    }

    @DeleteMapping("/saves/{contentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unsave(
            Authentication authentication,
            @PathVariable Long contentId
    ) {
        MemberPrincipal member = MemberAuthController.requireMember(authentication);

        saveService.unsave(member.id(), contentId);
    }
}
