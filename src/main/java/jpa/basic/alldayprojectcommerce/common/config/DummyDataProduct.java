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
        create("FAMOUS 로고 후드", 65000L, 50, "FAMOUS 공식 로고 후드 스웨트셔츠", ProductStatus.ON_SALE, Category.MERCHANDISE, "famous_hoodie.png");
        create("WICKED 그래픽 티셔츠", 38000L, 100, "WICKED 아트워크 반팔 티셔츠", ProductStatus.ON_SALE, Category.MERCHANDISE, "wicked_tee.png");
        create("LOOK AT ME 아크릴 키링", 12000L, 300, "LOOK AT ME 캐릭터 아크릴 키링", ProductStatus.ON_SALE, Category.MERCHANDISE, "look_keyring.png");
        create("Allday 스티커팩", 8000L, 500, "AlldayProject 시그니처 디자인 스티커 10종", ProductStatus.ON_SALE, Category.MERCHANDISE, "sticker_pack.png");
        create("FAMOUS 정규 앨범 CD", 18500L, 150, "FAMOUS 정규 1집 CD 패키지", ProductStatus.ON_SALE, Category.ALBUM, "famous_cd.png");
        create("WICKED 카세트 테이프", 25000L, 30, "WICKED 한정판 핑크 카세트 테이프", ProductStatus.SOLD_OUT, Category.ALBUM, "wicked_tape.png");
        create("LOOK AT ME 포스터", 12000L, 100, "LOOK AT ME A2 아트 포스터", ProductStatus.ON_SALE, Category.MERCHANDISE, "look_poster.png");
        create("시그니처 스마트톡", 15000L, 80, "AlldayProject 공식 로고 스마트톡", ProductStatus.ON_SALE, Category.MERCHANDISE, "smart_tok.png");
        create("FAMOUS 자수 비니", 32000L, 60, "FAMOUS 타이틀 자수 골지 비니", ProductStatus.ON_SALE, Category.MERCHANDISE, "famous_beanie.png");
        create("WICKED 젤리 폰케이스", 22000L, 0, "WICKED 일러스트 투명 젤리 케이스", ProductStatus.SOLD_OUT, Category.MERCHANDISE, "wicked_phone_case.png");
        create("LOOK AT ME 캔버스 백", 28000L, 120, "LOOK AT ME 그래픽 캔버스 토트백", ProductStatus.ON_SALE, Category.MERCHANDISE, "look_bag.png");
        create("미공개 포토카드 세트", 10000L, 200, "AlldayProject 미공개 셀카 포토카드 5종", ProductStatus.ON_SALE, Category.MERCHANDISE, "photocard.png");
        create("FAMOUS 세라믹 머그", 18000L, 45, "FAMOUS 로고 프린팅 화이트 머그", ProductStatus.ON_SALE, Category.MERCHANDISE, "famous_mug.png");
        create("노트북 파우치", 35000L, 25, "AlldayProject 13인치 자수 노트북 파우치", ProductStatus.ON_SALE, Category.MERCHANDISE, "pouch.png");
        create("WICKED 배지 세트", 14000L, 150, "WICKED 캐릭터 및 로고 금속 배지 2종", ProductStatus.ON_SALE, Category.MERCHANDISE, "wicked_badge.png");
        create("FAMOUS 한정판 LP", 75000L, 0, "FAMOUS 한정판 투명 옐로우 컬러 바이닐", ProductStatus.SOLD_OUT, Category.ALBUM, "famous_lp.png");
        create("썸머 오픈카라 셔츠", 48000L, 70, "AlldayProject 로고 패턴 오픈카라 셔츠", ProductStatus.ON_SALE, Category.MERCHANDISE, "shirt.png");
        create("LOOK AT ME 마스킹 테이프", 5500L, 400, "LOOK AT ME 테마 디자인 마스킹 테이프", ProductStatus.ON_SALE, Category.MERCHANDISE, "masking_tape.png");
        create("스텐 텀블러", 29000L, 35, "AlldayProject 로고 각인 스테인리스 텀블러", ProductStatus.ON_SALE, Category.MERCHANDISE, "tumbler.png");
        create("WICKED 버킷햇", 36000L, 55, "WICKED 로고 자수 블랙 버킷햇", ProductStatus.ON_SALE, Category.MERCHANDISE, "wicked_hat.png");
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
