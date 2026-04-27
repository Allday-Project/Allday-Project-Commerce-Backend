USE allday_project_commerce;

-- 혹시 기존 데이터가 있으면 초기화
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE users;
TRUNCATE TABLE products;
SET FOREIGN_KEY_CHECKS = 1;

-- 유저 10,000명 생성
DROP PROCEDURE IF EXISTS insert_dummy_users;

DELIMITER $$

CREATE PROCEDURE insert_dummy_users()
BEGIN
    DECLARE i INT DEFAULT 1;

    WHILE i <= 10000 DO
        INSERT INTO users (
            name,
            email,
            password,
            phone,
            address,
            role,
            created_at,
            updated_at
          )
        VALUES (
            CONCAT('유저', i),
            CONCAT('user', i, '@test.com'),
            CONCAT('encoded-password-', i),
            CONCAT('010-1234-', LPAD(MOD(i, 10000), 4, '0')),
            CONCAT('서울시 강남구 테스트로 ', i),
            'USER',
            NOW(),
            NOW()
        );

        SET i = i + 1;
END WHILE;
END$$

DELIMITER ;

CALL insert_dummy_users();

DROP PROCEDURE IF EXISTS insert_dummy_users;


-- 기본 상품 4개 생성
INSERT INTO products (
    name,
    price,
    stock,
    description,
    status,
    category,
    image_url,
    created_at,
    updated_at
)
VALUES
    ('ALLDAY PROJECT 볼캡', 36000, 100, '발렌타인 코듀로이 볼캡', 'ON_SALE', 'MERCHANDISE', 'https://image.com/cap.png', NOW(), NOW()),
    ('ALLDAY PROJECT 바이닐', 70000, 50, '한정판 LP', 'ON_SALE', 'ALBUM', 'https://image.com/vinyl.png', NOW(), NOW()),
    ('ALLDAY PROJECT 슬로건', 15000, 200, '공식 슬로건', 'SOLD_OUT', 'MERCHANDISE', 'https://image.com/slogan.png', NOW(), NOW()),
    ('ALLDAY PROJECT 티켓', 0, 100, '티켓 구매권', 'ON_SALE', 'TICKET', 'https://image.com/ticket.png', NOW(), NOW());


-- 상품 50,000개 생성
DROP PROCEDURE IF EXISTS insert_dummy_products;

DELIMITER $$

CREATE PROCEDURE insert_dummy_products()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE random_status VARCHAR(30);
    DECLARE random_category VARCHAR(30);

    WHILE i <= 50000 DO

        SET random_status = ELT(
            FLOOR(1 + RAND() * 10),
            'ON_SALE', 'ON_SALE', 'ON_SALE',
            'ON_SALE', 'ON_SALE', 'ON_SALE',
            'ON_SALE', 'SOLD_OUT', 'SOLD_OUT',
            'DISCONTINUED'
        );

        SET random_category = ELT(
            FLOOR(1 + RAND() * 3),
            'TICKET', 'MERCHANDISE', 'ALBUM'
        );

INSERT INTO products (
    name,
    price,
    stock,
    description,
    status,
    category,
    image_url,
    created_at,
    updated_at)
VALUES (
           CONCAT('ALLDAY PROJECT 상품 ', i),
           FLOOR(10000 + RAND() * 90000),
           FLOOR(RAND() * 200),
           CONCAT('상품 ', i, ' 설명'),
           random_status,
           random_category,
           CONCAT('https://image.com/p', i, '.png'),
           NOW(),
           NOW()
       );

SET i = i + 1;
END WHILE;
END$$

DELIMITER ;

CALL insert_dummy_products();

DROP PROCEDURE IF EXISTS insert_dummy_products;