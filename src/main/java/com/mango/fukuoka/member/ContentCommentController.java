package com.mango.fukuoka.member;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ContentCommentController {

    private final ContentCommentService commentService;

    public ContentCommentController(ContentCommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/api/v1/contents/{contentId}/comments")
    public List<ContentCommentService.CommentResponse> list(
            Authentication authentication,
            @PathVariable Long contentId
    ) {
        return commentService.list(
                contentId,
                MemberAuthController.optionalMemberId(authentication)
        );
    }

    @PostMapping("/api/v1/contents/{contentId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public ContentCommentService.CommentResponse create(
            Authentication authentication,
            @PathVariable Long contentId,
            @RequestBody CommentRequest request
    ) {
        MemberPrincipal member = MemberAuthController.requireMember(authentication);

        return commentService.create(
                contentId,
                member.id(),
                request == null ? null : request.body()
        );
    }

    @DeleteMapping("/api/v1/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            Authentication authentication,
            @PathVariable Long commentId
    ) {
        MemberPrincipal member = MemberAuthController.requireMember(authentication);

        commentService.delete(commentId, member.id());
    }

    public record CommentRequest(String body) {
    }
}
