#!/bin/bash

set -u
VERSIONS=("v1" "v2" "v3" "v4" "v5" "v6" "v7" "v8")

VUS=${VUS:-200}
PRODUCT_ID=${PRODUCT_ID:-4}
BASE_URL=${BASE_URL:-http://app:8090}
DB_PASSWORD=${DB_PASSWORD:-12345678}

reset_db() {
  echo "DB reset: PRODUCT_ID=${PRODUCT_ID}"

  docker compose exec -T mysql mysql \
    -uroot \
    -p"${DB_PASSWORD}" \
    allday_project_commerce <<SQL
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE order_users;
TRUNCATE TABLE order_products;
TRUNCATE TABLE orders;
TRUNCATE TABLE product_stock_logs;

SET FOREIGN_KEY_CHECKS = 1;

UPDATE products
SET stock = 100,
    status = 'ON_SALE'
WHERE id = ${PRODUCT_ID};

SELECT id, name, stock, status
FROM products
WHERE id = ${PRODUCT_ID};
SQL
}

run_k6() {
  local VERSION=$1

  echo ""
  echo "======================================"
  echo "${VERSION} 테스트 시작"
  echo "VUS=${VUS}, PRODUCT_ID=${PRODUCT_ID}"
  echo "======================================"

  docker compose run --rm --no-deps \
    -e VUS="${VUS}" \
    -e BASE_URL="${BASE_URL}" \
    -e VERSION="${VERSION}" \
    -e PRODUCT_ID="${PRODUCT_ID}" \
    -e START_USER_ID=1 \
    k6 run /scripts/event-order-version-test.js

  local EXIT_CODE=$?

  if [ ${EXIT_CODE} -ne 0 ]; then
    echo "⚠️ ${VERSION} 테스트 실패 또는 threshold 초과. 다음 버전 계속 실행."
  fi

  return 0
}

for VERSION in "${VERSIONS[@]}"
do
  reset_db
  run_k6 "${VERSION}"
done

echo ""
echo "전체 버전 테스트 완료"