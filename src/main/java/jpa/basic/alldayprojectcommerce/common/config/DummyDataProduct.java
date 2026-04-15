package jpa.basic.alldayprojectcommerce.common.config;

import jpa.basic.alldayprojectcommerce.domain.product.entity.Product;
import jpa.basic.alldayprojectcommerce.domain.product.entity.Category;
import jpa.basic.alldayprojectcommerce.domain.product.entity.ProductStatus;
import jpa.basic.alldayprojectcommerce.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class DummyDataProduct implements CommandLineRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        // 이미 데이터가 있으면 중복으로 넣지 않기 위해 체크
        if (productRepository.count() > 0) {
            return;
        }


            create("볼캡", 36000L, 100, "발렌타인 코듀로이 볼캡", ProductStatus.ON_SALE, Category.MERCHANDISE, "cap.png");
            create("바이닐", 70000L, 50, "한정판 LP", ProductStatus.ON_SALE, Category.ALBUM,"vinyl.png");
            create("슬로건", 15000L, 200, "공식 슬로건", ProductStatus.SOLD_OUT, Category.MERCHANDISE,"slogan.png");
    }

    private void create(String name, Long price, int stock, String desc, ProductStatus status, Category category, String img) {
        productRepository.save(Product.builder()
                .name("ALLDAY PROJECT " + name)
                .price(price)
                .stock(stock)
                .description(desc)
                .status(status)
                .category(category)
                .imageUrl("https://image.com/" + img)
                .build());
    }
}
