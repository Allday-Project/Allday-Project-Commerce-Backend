package jpa.basic.alldayprojectcommerce.common.factory;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import lombok.experimental.UtilityClass;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * ID 생성 유틸리티 클래스
 *
 * <p>NanoId를 기반으로 고유한 식별자를 생성합니다.
 * prefix 및 날짜를 포함한 다양한 형태의 ID 생성 기능을 제공합니다.</p>
 *
 * @author 홍성현
 * @since 2026-04-13
 */
@UtilityClass
public class IdFactory {

    private final SecureRandom random = new SecureRandom();
    private final char[] DEFAULT_ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    private final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * prefix + 날짜(yyyyMMdd) + NanoId를 조합한 ID를 생성한다.
     *
     * @param prefix ID 앞에 붙일 문자열 (예: ORDER, USER)
     * @param length 생성할 NanoId 길이
     * @return prefix.yyyyMMdd.nanoId 형태의 문자열
     *
     * 예: ORDER.20260413.aB3xYz91Qp
     */
    public String generateWithDate(String prefix, int length) {
        String nanoId = NanoIdUtils.randomNanoId(
                random,
                DEFAULT_ALPHABET,
                length
        );

        String date = LocalDate.now().format(DATE_FORMATTER);

        return String.format("%s-%s-%s", prefix, date, nanoId);
    }
}