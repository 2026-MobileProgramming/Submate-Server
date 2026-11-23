package com.submate.submate.common;

// 앱 카테고리 문자열("OTT") ↔ DB category_id 변환
public enum CategoryCode {
    OTT(1),
    MUSIC(2),
    FOOD(3),
    SHOPPING(4),
    AI(5),
    DIGITAL_CONTENT(6),
    ETC(7);

    private final int id;

    CategoryCode(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static CategoryCode fromId(Integer id) {
        if (id != null) {
            for (CategoryCode code : values()) {
                if (code.id == id) {
                    return code;
                }
            }
        }
        throw new IllegalArgumentException("알 수 없는 카테고리 ID: " + id);
    }

    // 앱에서 받은 카테고리 문자열을 변환
    public static CategoryCode fromName(String name) {
        if (name != null) {
            for (CategoryCode code : values()) {
                if (code.name().equals(name)) {
                    return code;
                }
            }
        }
        throw new ApiException(ApiErrorCode.INVALID_INPUT, "존재하지 않는 카테고리입니다.");
    }
}
