package jpa.basic.alldayprojectcommerce.domain.keyword.service;

public interface KeywordCommandService {

    // 회원용   - userId 기반 중복 방지
    void recordSearch(Long userId, String query);

    // 비회원용 - IP 기반 중복 방지
    void recordSearchByIp(String ip, String query);

}
