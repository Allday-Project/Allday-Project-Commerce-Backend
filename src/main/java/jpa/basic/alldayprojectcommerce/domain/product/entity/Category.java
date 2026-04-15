package jpa.basic.alldayprojectcommerce.domain.product.entity;

public enum Category {
    ALBUM("앨범"),
    MERCHANDISE("굿즈"),
    TICKET("티켓");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
