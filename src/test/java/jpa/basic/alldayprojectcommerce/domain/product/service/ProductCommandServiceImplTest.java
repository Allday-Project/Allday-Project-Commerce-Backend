package jpa.basic.alldayprojectcommerce.domain.product.service;

import jpa.basic.alldayprojectcommerce.common.cache.CacheName;
import jpa.basic.alldayprojectcommerce.common.cache.LocalCacheManager;
import jpa.basic.alldayprojectcommerce.common.exception.CustomException;
import jpa.basic.alldayprojectcommerce.common.exception.ErrorCode;
import jpa.basic.alldayprojectcommerce.domain.product.dto.request.SearchProductRequest;
import jpa.basic.alldayprojectcommerce.domain.product.dto.response.SearchProductResponse;
import jpa.basic.alldayprojectcommerce.domain.product.entity.Category;
import jpa.basic.alldayprojectcommerce.domain.product.entity.Product;
import jpa.basic.alldayprojectcommerce.domain.product.entity.ProductStatus;
import jpa.basic.alldayprojectcommerce.domain.product.repository.ProductRepository;
import jpa.basic.alldayprojectcommerce.domain.product.repository.StockRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@Slf4j
@SpringBootTest
@Transactional
@ExtendWith(MockitoExtension.class)
@TestPropertySource(properties = {
        "JWT_SECRET_KEY=dGVzdHNlY3JldGtleWZvcnRlc3RpbmdtaW5pbXVtMjU2Yml0c2xvbmc="
})
public class ProductCommandServiceImplTest {


    @InjectMocks
    private ProductCommandServiceImpl productCommandService;

//    @Mock
//    private ProductRepository productRepository;

    @Mock
    private StockRepository stockRepository;


    // ✅ 1. 정상 차감
    @Test
    @DisplayName("결제 시 재고가 정상적으로 차감되어야 한다")
    void decreaseStock_Success() {
        // given
        Long productId = 1L;
        int decreaseQuantity = 5;
        Long orderId = 1L;


        Product mockProduct = Product.builder()
                .stock(10)
                .build();

        ReflectionTestUtils.setField(mockProduct, "id", productId); // id 강제 주입

        when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));
//        when(productRepository.save(any(Product.class))).thenReturn(mockProduct); // save 스터빙

        // when
        productCommandService.decreaseStock(productId, decreaseQuantity, orderId);

        // then
        assertEquals(5, mockProduct.getStock());
        verify(productRepository, times(1)).findById(productId);
//        verify(productRepository, times(1)).save(mockProduct); // ✅ save 검증 추가
    }


    // 2. 재고 부족 예외
    @Test
    @DisplayName("재고가 부족하면 예외가 발생해야 한다")
    void decreaseStockOnPayment_InsufficientStock() {
        Long productId = 1L;
        Long orderId = 1L;

        Product mockProduct = Product.builder()
                .stock(10)
                .build();
        ReflectionTestUtils.setField(mockProduct, "id", productId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));

        // ✅ CustomException으로 변경
        CustomException ex = assertThrows(CustomException.class, () ->
                productCommandService.decreaseStock(productId, 15, orderId)
        );

        // ✅ ErrorCode까지 검증하면 더 정확함
        assertEquals(ErrorCode.PRODUCT_OUT_OF_STOCK, ex.getErrorCode());
    }


    // 3. 상품 미존재 예외
    @Test
    @DisplayName("존재하지 않는 상품이면 예외가 발생해야 한다")
    void decreaseStock_ProductNotFound() {
        Long productId = 999L;
        Long orderId = 1L;

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // ✅ CustomException으로 변경
        CustomException ex = assertThrows(CustomException.class, () ->
                productCommandService.decreaseStock(productId, 5, orderId)
        );

        assertEquals(ErrorCode.PRODUCT_NOT_FOUND, ex.getErrorCode());
    }


    // 4. ✅ 보너스 - 재고가 0이 되면 SOLD_OUT 상태로 변경되는지 검증
    @Test
    @DisplayName("재고가 0이 되면 품절 상태로 변경되어야 한다")
    void decreaseStock_SoldOut() {
        Long productId = 1L;
        Long orderId = 1L;

        Product mockProduct = Product.builder()
                .stock(5)
                .status(ProductStatus.ON_SALE)
                .build();
        ReflectionTestUtils.setField(mockProduct, "id", productId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));

        productCommandService.decreaseStock(productId, 5, orderId); // 재고 전부 소진

        assertEquals(0, mockProduct.getStock());
        assertEquals(ProductStatus.SOLD_OUT, mockProduct.getStatus()); // ✅ 품절 상태 검증
    }


    // ✅ 재고 증가 - 정상
    @Test
    @DisplayName("취소 시 재고가 정상적으로 증가되어야 한다")
    void increaseStock_Success() {
        // given
        Long productId = 1L;
        Long orderId = 1L;
        int increaseQuantity = 5;

        Product mockProduct = Product.builder()
                .stock(10)
                .build();
        ReflectionTestUtils.setField(mockProduct, "id", productId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));

        // when
        productCommandService.increaseStock(productId, increaseQuantity, orderId);

        // then
        assertEquals(15, mockProduct.getStock()); // 10 + 5 = 15
        verify(productRepository, times(1)).findById(productId);
    }


    // ✅ 재고 증가 - 상품 미존재 예외
    @Test
    @DisplayName("취소 시 존재하지 않는 상품이면 예외가 발생해야 한다")
    void increaseStock_ProductNotFound() {
        // given
        Long productId = 999L;
        Long orderId = 1L;

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // when & then
        CustomException ex = assertThrows(CustomException.class, () ->
                productCommandService.increaseStock(productId, 5, orderId)
        );

        assertEquals(ErrorCode.PRODUCT_NOT_FOUND, ex.getErrorCode());
    }


//    // ✅ 재고 이력 저장 - 정상
//    @Test
//    @DisplayName("재고 이력이 정상적으로 저장되어야 한다")
//    void saveStockHistory_Success() {
//        // given
//        Long productId = 1L;
//        Long orderId = 100L;
//        int quantity = 5;
//
//        Product mockProduct = Product.builder()
//                .stock(15)
//                .build();
//        ReflectionTestUtils.setField(mockProduct, "id", productId);
//
//        when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));
//
//        // when
//        productCommandService.saveStockHistory(product, quantity, orderId);
//
//        // then
//        verify(stockRepository, times(1)).save(any(Stock.class));
//    }


//    // ✅ 재고 이력 저장 - 상품 미존재 예외
//    @Test
//    @DisplayName("재고 이력 저장 시 존재하지 않는 상품이면 예외가 발생해야 한다")
//    void saveStockHistory_ProductNotFound() {
//        // given
//        Long productId = 999L;
//
//        when(productRepository.findById(productId)).thenReturn(Optional.empty());
//
//        // when & then
//        CustomException ex = assertThrows(CustomException.class, () ->
//                productCommandService.saveStockHistory(productId, 100L, 5)
//        );
//
//        assertEquals(ErrorCode.PRODUCT_NOT_FOUND, ex.getErrorCode());
//    }


    @Autowired
    private ProductQueryService productQueryService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private ProductRepository productRepository;

    private static final String KEYWORD = "테스트상품";
    private static final Pageable PAGEABLE = PageRequest.of(0, 10);
    private static final SearchProductRequest SEARCH_REQUEST = new SearchProductRequest(KEYWORD);

    @BeforeEach
    void setUp() {
        Cache cache = localCacheManager.getCache("productSearchCache");
        if (cache != null) cache.clear();

        List<Product> products = IntStream.range(0, 20)
                .mapToObj(i -> Product.builder()
                        .name(KEYWORD + i)
                        .price(10000L)
                        .description("테스트 설명 " + i)
                        .category(Category.MERCHANDISE)  // 본인 프로젝트 enum 값으로
                        .imageUrl("http://test.com/image" + i + ".jpg")
                        .stock(100)         // ← stock도 NOT NULL이면 추가
                        .status(ProductStatus.ON_SALE)
                        .build())
                .toList();
        productRepository.saveAll(products);
    }

    @Test
    @DisplayName("캐시 미스(DB 조회) vs 캐시 히트 속도 비교")
    void compareDbVsCacheSpeed() {
        int repeat = 50;
        StopWatch sw = new StopWatch("productSearchCache 성능 비교");

        // ── 1. DB 조회 (캐시 미스 반복) ──────────────────────
        sw.start("DB 조회 (캐시 미스)");
        for (int i = 0; i < repeat; i++) {
            localCacheManager.getCache("productSearchCache").clear();
            productQueryService.searchProductsV2(SEARCH_REQUEST, PAGEABLE);
        }
        sw.stop();

        // ── 2. 캐시 워밍업 ────────────────────────────────────
        localCacheManager.getCache("productSearchCache").clear();
        productQueryService.searchProductsV2(SEARCH_REQUEST, PAGEABLE);

        // ── 3. 캐시 히트 반복 ────────────────────────────────
        sw.start("Cache 조회 (캐시 히트)");
        for (int i = 0; i < repeat; i++) {
            productQueryService.searchProductsV2(SEARCH_REQUEST, PAGEABLE);
        }
        sw.stop();

        // ── 4. 결과 로그 ──────────────────────────────────────
        long dbNanos = sw.getTaskInfo()[0].getTimeNanos();
        long cacheNanos = sw.getTaskInfo()[1].getTimeNanos();

        log.info("\n{}", sw.prettyPrint(TimeUnit.MILLISECONDS));
        log.info("DB    평균: {} ms", String.format("%.3f", dbNanos / (double) repeat / 1_000_000));
        log.info("Cache 평균: {} ms", String.format("%.3f", cacheNanos / (double) repeat / 1_000_000));
        log.info("속도 향상:  {}x", String.format("%.1f", (double) dbNanos / cacheNanos));

        assertThat(cacheNanos).isLessThan(dbNanos);
    }

    @Test
    @DisplayName("캐시 키 검증 - keyword/page 조합별로 독립 캐시 생성 확인")
    void cacheKeyIsolationTest() {
        Cache cache = localCacheManager.getCache("productSearchCache");

        SearchProductRequest req1 = new SearchProductRequest("키워드A");
        SearchProductRequest req2 = new SearchProductRequest("키워드B");
        Pageable page0 = PageRequest.of(0, 10);
        Pageable page1 = PageRequest.of(1, 10);

        productQueryService.searchProductsV2(req1, page0);
        productQueryService.searchProductsV2(req2, page0);
        productQueryService.searchProductsV2(req1, page1);

        assertThat(cache.get("search:키워드A:0:10")).isNotNull();
        assertThat(cache.get("search:키워드B:0:10")).isNotNull();
        assertThat(cache.get("search:키워드A:1:10")).isNotNull();

        log.info("캐시 키 분리 검증 통과 ✅");
    }

    @Test
    @DisplayName("캐시 히트 시 동일 객체 반환 확인")
    void cacheHitShouldReturnCachedResult() {
        // 첫 번째 호출: 캐시 MISS → DB 조회
        Page<SearchProductResponse> firstResult =
                productQueryService.searchProductsV2(SEARCH_REQUEST, PAGEABLE);

        // 두 번째 호출: 캐시 HIT → 동일 객체 반환
        Page<SearchProductResponse> secondResult =
                productQueryService.searchProductsV2(SEARCH_REQUEST, PAGEABLE);

        assertThat(secondResult).isSameAs(firstResult);

        log.info("캐시 히트 시 동일 객체 반환 검증 통과 ✅");
    }


    @Autowired
    private LocalCacheManager localCacheManager;

    @Test
    @DisplayName("maximumSize를 초과하면 오래된 데이터가 캐시에서 밀려나야 한다")
    void testMaximumSizeEviction() {
        // given: 테스트용으로 maximumSize가 10으로 설정된 캐시를 가져옴 (CacheName에 미리 세팅 필요)
        Cache cache = localCacheManager.getCache(CacheName.PRODUCT_SEARCH.getCacheName());
        com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache =
                (com.github.benmanes.caffeine.cache.Cache<Object, Object>) cache.getNativeCache();

        // when: 제한(10개)보다 많은 15개의 데이터를 캐시에 강제로 저장
        for (int i = 1; i <= 15; i++) {
            cache.put("key" + i, "value" + i);
        }

        // 핵심 포인트: Caffeine의 eviction(삭제) 처리는 성능 최적화를 위해 '비동기'로 일어남.
        // 데이터를 넣자마자 사이즈를 재면 15개가 나올 수 있으므로, 확실한 측정을 위해 정리 작업을 강제 호출함.
        nativeCache.cleanUp();

        // then: 캐시 사이즈는 maximumSize인 10을 초과하지 않아야 함
        long currentSize = nativeCache.estimatedSize();
        System.out.println("현재 캐시 사이즈: " + currentSize);

        assertThat(currentSize).isLessThanOrEqualTo(10);

        // 먼저 들어간 key1은 밀려나서 없어야 하고, 마지막에 들어간 key15는 남아있어야 함
        assertThat(cache.get("key1")).isNull();
        assertThat(cache.get("key15")).isNotNull();
    }
}