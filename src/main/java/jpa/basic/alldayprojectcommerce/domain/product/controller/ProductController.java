package jpa.basic.alldayprojectcommerce.domain.product.controller;


import jakarta.validation.Valid;
import jpa.basic.alldayprojectcommerce.common.ApiResponse;
import jpa.basic.alldayprojectcommerce.domain.product.dto.request.FilterProductRequest;
import jpa.basic.alldayprojectcommerce.domain.product.dto.request.SearchProductRequest;
import jpa.basic.alldayprojectcommerce.domain.product.dto.response.GetAllProductResponse;
import jpa.basic.alldayprojectcommerce.domain.product.dto.response.GetOneProductResponse;
import jpa.basic.alldayprojectcommerce.domain.product.dto.response.SearchProductResponse;
import jpa.basic.alldayprojectcommerce.domain.product.service.ProductQueryServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    // ProductController.java
  private final ProductQueryService productQueryService;


    @GetMapping("/v1/boards/search")
    public ResponseEntity<ApiResponse<Page<GetAllProductResponse>>> search(
            @Valid SearchProductRequest searchRequest,
            @RequestParam(defaultValue = "1") int page,
            Pageable pageable) {

        Pageable adjustedPageable = PageRequest.of(page - 1, pageable.getPageSize(), pageable.getSort());

        Page<GetAllProductResponse> responses = productQueryService.searchProducts(searchRequest, adjustedPageable);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, responses));
    }


    @GetMapping("/v2/boards/search")
    public ResponseEntity<ApiResponse<Page<SearchProductResponse>>> searchV2(
            @Valid SearchProductRequest request,
            @RequestParam(defaultValue = "1") int page,
            Pageable pageable){
        Pageable adjustedPageable = PageRequest.of(page - 1, pageable.getPageSize(), pageable.getSort());
        Page<SearchProductResponse> responses = productQueryService.searchProductsV2(request, adjustedPageable);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, responses));
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        // true: 공백만 있는 문자열을 null로 바꿀 것인가? (여기선 false로 해서 빈 문자열로 둠)
        StringTrimmerEditor editor = new StringTrimmerEditor(false);
        binder.registerCustomEditor(String.class, editor);
    }


    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<GetOneProductResponse>> getOne(@PathVariable("productId") Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(HttpStatus.OK, productQueryServiceImpl.getOneProduct(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<GetAllProductResponse>>> getAll(
            @ModelAttribute FilterProductRequest filterRequest,
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(defaultValue = "1") int page) {

        Pageable adjustedPageable = PageRequest.of(page - 1, pageable.getPageSize(), pageable.getSort());

        Page<GetAllProductResponse> responses = productQueryService.getAllProduct(filterRequest, adjustedPageable);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, responses));
    }


}
