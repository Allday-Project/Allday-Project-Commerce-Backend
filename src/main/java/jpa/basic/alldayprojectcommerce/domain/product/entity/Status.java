package jpa.basic.alldayprojectcommerce.domain.product.entity;

public enum Status {
    ON_SALE("판매중"),
    OUT_OF_STOCK("품절"),
    DISCONTINUED("단종");

    private final String displayName;

    Status(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}