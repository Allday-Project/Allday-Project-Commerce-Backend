package jpa.basic.alldayprojectcommerce.domain.product.entity;

public enum ProductStatus {
    ON_SALE("판매중"),
    SOLD_OUT("품절"),
    DISCONTINUED("단종");

    private final String displayName;

    ProductStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}