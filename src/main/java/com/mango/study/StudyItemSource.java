package com.mango.study;

public enum StudyItemSource {

    /**
     * mango DB의 학습 콘텐츠. 학습 이력(study_histories)을 남길 수 있다.
     */
    LEARNING("learning"),

    /**
     * mango_fukuoka DB의 여행 표현. 별도 DB라 학습 이력 FK를 걸 수 없다.
     */
    TRAVEL("travel");

    private final String keyPrefix;

    StudyItemSource(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public String keyOf(Long id) {
        return keyPrefix + "-" + id;
    }
}
