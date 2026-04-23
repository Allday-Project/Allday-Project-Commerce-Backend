package jpa.basic.alldayprojectcommerce.domain.keyword.controller;

import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/keywords")
public class KeywordController {

    private final KeywordCommandService keywordCommandService;
    private final KeywordQueryService   keywordQueryService;

    /**
     * 회원   -> userId 기반 중복 방지
     * 비회원 -> IP 기반 중복 방지
     */
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<Void>> createRecordSearch(
            @AuthenticationPrincipal LoginUserInfo loginUserInfo,
            @RequestBody @Valid SearchRequest request,
            HttpServletRequest httpServletRequest) {

        if (loginUserInfo != null) {
            keywordCommandService.recordSearch(loginUserInfo.id(), request.query());
        } else {
            String ip = httpServletRequest.getRemoteAddr();
            keywordCommandService.recordSearchByIp(ip, request.query());
        }

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK));
    }

    @GetMapping("/top5")
    public ResponseEntity<ApiResponse<List<Top5KeywordResponse>>> getTop5Keywords() {
        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK, keywordQueryService.getTop5())
        );
    }
}
