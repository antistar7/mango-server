package com.mango.fukuoka.member;

import com.mango.fukuoka.content.FukuokaContent;
import com.mango.fukuoka.content.FukuokaContentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class MemberSaveService {

    private final MemberAuthService authService;
    private final MemberSaveRepository saveRepository;
    private final FukuokaContentRepository contentRepository;

    public MemberSaveService(
            MemberAuthService authService,
            MemberSaveRepository saveRepository,
            FukuokaContentRepository contentRepository
    ) {
        this.authService = authService;
        this.saveRepository = saveRepository;
        this.contentRepository = contentRepository;
    }

    @Transactional(
            transactionManager = "fukuokaTransactionManager",
            readOnly = true
    )
    public List<Long> savedContentIds(Long memberId) {
        return saveRepository.findContentIdsByMemberId(memberId);
    }

    @Transactional(transactionManager = "fukuokaTransactionManager")
    public void save(Long memberId, Long contentId) {
        if (saveRepository.existsByMember_IdAndContent_Id(memberId, contentId)) {
            return;
        }

        Member member = authService.requireMember(memberId);
        FukuokaContent content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "콘텐츠를 찾을 수 없습니다."
                ));

        saveRepository.save(MemberSave.create(member, content));
    }

    @Transactional(transactionManager = "fukuokaTransactionManager")
    public void unsave(Long memberId, Long contentId) {
        saveRepository.deleteByMember_IdAndContent_Id(memberId, contentId);
    }
}
