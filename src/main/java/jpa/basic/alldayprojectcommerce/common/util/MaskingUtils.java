package jpa.basic.alldayprojectcommerce.common.util;

public class MaskingUtils {
    private MaskingUtils() {}

    /*
    * 이메일 마스킹
    * "test@test.com -> "te**@test.com
    * */
    public static String maskEmail(String email) {
        if (email == null || email.isBlank()) return null;
        int atIdx = email.indexOf('@');
        if(atIdx <= 0) return email;

        String local = email.substring(0, atIdx);
        String domain = email.substring(atIdx);  // "@test.com"

        if (local.length() <= 2) return email;  // 너무 짧으면 그대로 출력

        String exposed = local.substring(0, 2);
        String masked = "*".repeat(local.length() -2);
        return exposed + masked + domain;
    }

    /*
     * 이름 마스킹 - 첫글자 노출, 나머지 *
     * "홍길동" -> "홍**"
     * "김사무엘" -> "김***"
     * "이도" -> "이*
     */
    public static String maskName(String name) {
        if(name == null || name.isBlank()) return null;
        int len = name.length();
        if (len == 1) return name;

        return name.charAt(0) + "*".repeat(len - 1);
    }

    /*
     * 비밀번호 마스킹 - 항상 고정 8자리 *
     */
    public static String maskPassword() {
        return "●●●●●●●●";
    }

    /*
     * 전화번호 마스킹 - 가운데 4자리 마스킹
     * "010-1234-1234" -> "010-****-1234"
     */
    public static String maskPhone(String phone) {
        if(phone == null || phone.isBlank()) return null;

        if (phone.matches("\\d{3}-\\d{3,4}-\\d{4}")) {
            String[] parts = phone.split("-");
            return parts[0] + "-****-" + parts[2];
        }

        return phone; //예상치 못한 형식은 그대로
    }

    /*
     * 주소 마스킹 - 공백 기준 3번째부터 마스킹
     * "서울시 강남구 역삼동 123-4" -> "서울시 강남구 ***"
     */
    public static String maskAddress(String address) {
        if (address == null || address.isBlank()) return null;

        String[] tokens = address.split(" ");
        if (tokens.length <= 2) return address; // 시,구까지만 있으면 그대로

        // 앞 2토큰(시, 구)만 노출
        return tokens[0] + " " + tokens[1] + " ***";
    }

}
