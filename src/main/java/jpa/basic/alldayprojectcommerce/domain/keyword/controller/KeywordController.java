package jpa.basic.alldayprojectcommerce.domain.keyword.controller;

import jakarta.validation.Valid;
import jpa.basic.alldayprojectcommerce.common.ApiResponse;
import jpa.basic.alldayprojectcommerce.common.security.auth.LoginUser;
import jpa.basic.alldayprojectcommerce.common.security.auth.LoginUserInfo;
import jpa.basic.alldayprojectcommerce.domain.keyword.dto.request.SearchRequest;
import jpa.basic.alldayprojectcommerce.domain.keyword.service.KeywordCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/keywords")
public class KeywordController {

    private final KeywordCommandService keywordCommandService;

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
}
