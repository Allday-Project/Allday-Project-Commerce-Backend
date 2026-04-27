package jpa.basic.alldayprojectcommerce.domain.product.entity;

public enum Category {
    ALBUM("앨범"),
    MERCH("굿즈"),
    EVENT("이벤트");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
