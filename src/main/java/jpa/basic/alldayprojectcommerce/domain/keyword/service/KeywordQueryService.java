package jpa.basic.alldayprojectcommerce.domain.keyword.service;

import jpa.basic.alldayprojectcommerce.domain.keyword.dto.response.Top5KeywordResponse;

import java.util.List;

public interface KeywordQueryService {

    List<Top5KeywordResponse> getTop5();
}
