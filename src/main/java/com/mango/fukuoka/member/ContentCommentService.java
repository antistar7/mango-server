package com.mango.fukuoka.member;

import com.mango.fukuoka.content.FukuokaContent;
import com.mango.fukuoka.content.FukuokaContentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ContentCommentService {

    private static final int MAX_BODY = 300;

    private final MemberAuthService authService;
    private final ContentCommentRepository commentRepository;
    private final FukuokaContentRepository contentRepository;

    public ContentCommentService(
            MemberAuthService authService,
            ContentCommentRepository commentRepository,
            FukuokaContentRepository contentRepository
    ) {
        this.authService = authService;
        this.commentRepository = commentRepository;
        this.contentRepository = contentRepository;
    }

    @Transactional(
            transactionManager = "fukuokaTransactionManager",
            readOnly = true
    )
    public List<CommentResponse> list(Long contentId, Long viewerMemberId) {
        return commentRepository
                .findByContent_IdAndHiddenFalseOrderByCreatedAtDesc(contentId)
                .stream()
                .map(comment -> toResponse(comment, viewerMemberId))
                .toList();
    }

    @Transactional(transactionManager = "fukuokaTransactionManager")
    public CommentResponse create(
            Long contentId,
            Long memberId,
            String rawBody
    ) {
        String body = normalizeBody(rawBody);
        Member member = authService.requireMember(memberId);
        FukuokaContent content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "콘텐츠를 찾을 수 없습니다."
                ));

        ContentComment saved = commentRepository.save(
                ContentComment.create(content, member, body)
        );

        return toResponse(saved, memberId);
    }

    @Transactional(transactionManager = "fukuokaTransactionManager")
    public void delete(Long commentId, Long memberId) {
        ContentComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "댓글을 찾을 수 없습니다."
                ));

        if (!comment.getMember().getId().equals(memberId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "내 댓글만 지울 수 있습니다."
            );
        }

        commentRepository.delete(comment);
    }

    private static String normalizeBody(String rawBody) {
        if (rawBody == null) {
            throw new IllegalArgumentException("댓글을 입력해주세요.");
        }

        String body = rawBody.trim();

        if (body.isEmpty()) {
            throw new IllegalArgumentException("댓글을 입력해주세요.");
        }

        if (body.length() > MAX_BODY) {
            throw new IllegalArgumentException(
                    "댓글은 " + MAX_BODY + "자까지 쓸 수 있습니다."
            );
        }

        return body;
    }

    private static CommentResponse toResponse(
            ContentComment comment,
            Long viewerMemberId
    ) {
        return new CommentResponse(
                comment.getId(),
                comment.getMember().getNickname(),
                comment.getBody(),
                comment.getCreatedAt(),
                viewerMemberId != null
                        && viewerMemberId.equals(comment.getMember().getId())
        );
    }

    public record CommentResponse(
            Long id,
            String nickname,
            String body,
            LocalDateTime createdAt,
            boolean mine
    ) {
    }
}
