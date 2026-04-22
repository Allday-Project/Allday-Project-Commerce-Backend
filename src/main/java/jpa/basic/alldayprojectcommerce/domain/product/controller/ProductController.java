package jpa.basic.alldayprojectcommerce.domain.product.controller;


import jpa.basic.alldayprojectcommerce.common.ApiResponse;
import jpa.basic.alldayprojectcommerce.domain.product.dto.response.GetAllProductResponse;
import jpa.basic.alldayprojectcommerce.domain.product.dto.response.GetOneProductResponse;
import jpa.basic.alldayprojectcommerce.domain.product.service.ProductQueryService;
import jpa.basic.alldayprojectcommerce.domain.product.service.ProductQueryServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductQueryService productQueryService;

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<GetOneProductResponse>> getOne (@PathVariable("productId") Long id){
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(HttpStatus.OK, productQueryService.getOneProduct(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<GetAllProductResponse>>> getAll(
            @PageableDefault(size =  10, page = 0, sort = "id",
                    direction = Sort.Direction.DESC) Pageable pageable){
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(HttpStatus.OK, productQueryService.getAllProduct(pageable)));
    }
}
