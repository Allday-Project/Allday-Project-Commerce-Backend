package jpa.basic.alldayprojectcommerce.domain.keyword.controller;

import jakarta.validation.Valid;
import jpa.basic.alldayprojectcommerce.common.ApiResponse;
import jpa.basic.alldayprojectcommerce.common.security.auth.LoginUser;
import jpa.basic.alldayprojectcommerce.common.security.auth.LoginUserInfo;
import jpa.basic.alldayprojectcommerce.domain.keyword.dto.request.SearchRequest;
import jpa.basic.alldayprojectcommerce.domain.keyword.dto.response.Top5KeywordResponse;
import jpa.basic.alldayprojectcommerce.domain.keyword.service.KeywordCommandService;
import jpa.basic.alldayprojectcommerce.domain.keyword.service.KeywordQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/keywords")
public class KeywordController {

    private final KeywordCommandService keywordCommandService;
    private final KeywordQueryService   keywordQueryService;

    /**
     * 고객이 검색창에서 검색할 때 호출
     * 검색어 기록만 담당
     */
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<Void>> createRecordSearch(
            @LoginUser LoginUserInfo loginUserInfo,
            @RequestBody @Valid SearchRequest request) {

        keywordCommandService.createRecordSearch(loginUserInfo.id(), request.query());
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK));
    }

    @GetMapping("/top5")
    public ResponseEntity<ApiResponse<List<Top5KeywordResponse>>> getTop5Keywords() {
        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK, keywordQueryService.getTop5())
        );
    }
}
