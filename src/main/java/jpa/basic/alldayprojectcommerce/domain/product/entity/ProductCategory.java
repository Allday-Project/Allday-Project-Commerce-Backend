package jpa.basic.alldayprojectcommerce.domain.product.entity;

public enum ProductCategory {
    ALBUM("앨범"),
    MERCHANDISE("굿즈"),
    TICKET("티켓");

    private final String displayName;

    ProductCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
