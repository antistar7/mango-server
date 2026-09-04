package com.mango.fukuoka.site;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LandingService {

    static final String HERO_IMAGE = "landing_hero_image";
    static final String HERO_CAPTION = "landing_hero_caption";

    private final SiteSettingRepository settingRepository;

    public LandingService(SiteSettingRepository settingRepository) {
        this.settingRepository = settingRepository;
    }

    @Transactional(
            transactionManager = "fukuokaTransactionManager",
            readOnly = true
    )
    public LandingResponse get() {
        return new LandingResponse(
                value(HERO_IMAGE),
                value(HERO_CAPTION)
        );
    }

    @Transactional(transactionManager = "fukuokaTransactionManager")
    public LandingResponse update(LandingRequest request) {
        put(HERO_IMAGE, blankToNull(request.heroImage()));
        put(HERO_CAPTION, blankToNull(request.caption()));
        return new LandingResponse(
                blankToNull(request.heroImage()),
                blankToNull(request.caption())
        );
    }

    private String value(String key) {
        return settingRepository.findById(key)
                .map(SiteSetting::getSettingValue)
                .filter(value -> value != null && !value.isBlank())
                .orElse(null);
    }

    private void put(String key, String value) {
        SiteSetting setting = settingRepository.findById(key)
                .orElseGet(() -> SiteSetting.of(key, value));
        setting.setSettingValue(value);
        settingRepository.save(setting);
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    public record LandingResponse(
            String heroImage,
            String caption
    ) {
    }

    public record LandingRequest(
            String heroImage,
            String caption
    ) {
    }
}
