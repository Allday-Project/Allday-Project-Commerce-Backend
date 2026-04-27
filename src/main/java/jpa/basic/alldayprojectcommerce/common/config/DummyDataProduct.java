package jpa.basic.alldayprojectcommerce.common.config;

import jpa.basic.alldayprojectcommerce.domain.product.entity.Product;
import jpa.basic.alldayprojectcommerce.domain.product.entity.Category;
import jpa.basic.alldayprojectcommerce.domain.product.entity.ProductStatus;
import jpa.basic.alldayprojectcommerce.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Profile("local")
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

        // 기존 더미데이터 3개 + 티켓
        create("볼캡", 36000L, 100, "발렌타인 코듀로이 볼캡", ProductStatus.ON_SALE, Category.MERCHANDISE, "cap.png");
        create("바이닐", 70000L, 50, "한정판 LP", ProductStatus.ON_SALE, Category.ALBUM,"vinyl.png");
        create("슬로건", 15000L, 200, "공식 슬로건", ProductStatus.SOLD_OUT, Category.MERCHANDISE,"slogan.png");
        create("티켓", 0L, 100, "티켓 구매권", ProductStatus.ON_SALE, Category.TICKET,"ticket.png");

        // 더미데이터 50,000건 추가
        Category[] categories = Category.values();
        ProductStatus[] statuses = {
                ProductStatus.ON_SALE, ProductStatus.ON_SALE, ProductStatus.ON_SALE,
                ProductStatus.ON_SALE, ProductStatus.ON_SALE, ProductStatus.ON_SALE,
                ProductStatus.ON_SALE, ProductStatus.SOLD_OUT, ProductStatus.SOLD_OUT,
                ProductStatus.DISCONTINUED
        };
        Random random = new Random();

        List<Product> products = new ArrayList<>();
        for (int i = 1; i <= 50000; i++) {
            products.add(Product.builder()
                    .name("ALLDAY PROJECT 상품 " + i)
                    .price((long) (10000 + random.nextInt(90000)))
                    .stock(random.nextInt(200))
                    .description("상품 " + i + " 설명")
                    .status(statuses[random.nextInt(statuses.length)])
                    .category(categories[random.nextInt(categories.length)])
                    .imageUrl("https://image.com/p" + i + ".png")
                    .build());

            // 1000건마다 한 번씩 저장 (메모리 절약)
            if (i % 1000 == 0) {
                productRepository.saveAll(products);
                products.clear();
            }
        }
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
