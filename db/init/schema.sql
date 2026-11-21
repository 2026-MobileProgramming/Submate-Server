SET NAMES utf8mb4;

-- 1. USERS
CREATE TABLE users (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    email       VARCHAR(255) NOT NULL,
    password    VARCHAR(255) NOT NULL,
    nickname    VARCHAR(8)   NOT NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email)
);

-- 2. EMAIL_VERIFICATIONS
-- code, fail_count 는 EmailVerification 엔티티(String, int)와 맞추려고 VARCHAR(6), INT 사용
CREATE TABLE email_verifications (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    email        VARCHAR(255) NOT NULL,
    purpose      VARCHAR(20)  NOT NULL,
    code         VARCHAR(6)   NOT NULL,
    status       VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    fail_count   INT          NOT NULL DEFAULT 0,
    expires_at   DATETIME     NOT NULL,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    verified_at  DATETIME     NULL,
    PRIMARY KEY (id),
    INDEX idx_email_verifications (email, purpose, created_at),
    CONSTRAINT chk_ev_purpose CHECK (purpose IN ('SIGNUP', 'RESET_PW')),
    CONSTRAINT chk_ev_status  CHECK (status IN ('PENDING', 'VERIFIED', 'USED', 'INVALIDATED'))
);

-- 3. CATEGORIES
CREATE TABLE categories (
    id    INT         NOT NULL AUTO_INCREMENT,
    name  VARCHAR(20) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_categories_name (name)
);

-- 4. SERVICES
CREATE TABLE services (
    id           INT         NOT NULL AUTO_INCREMENT,
    category_id  INT         NOT NULL,
    name         VARCHAR(50) NOT NULL,
    sort_order   INT         NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_services_category_name (category_id, name),
    CONSTRAINT fk_services_category FOREIGN KEY (category_id) REFERENCES categories (id)
);

-- 6. SUBSCRIPTIONS
CREATE TABLE subscriptions (
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    user_id         BIGINT        NOT NULL,
    category_id     INT           NOT NULL,
    service_id      INT           NULL,
    service_name    VARCHAR(50)   NOT NULL,
    status          VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',
    plan_type       VARCHAR(10)   NOT NULL,
    currency        CHAR(3)       NULL,
    amount          DECIMAL(12,2) NULL,
    billing_cycle   VARCHAR(10)   NULL,
    billing_day     INT           NULL,
    start_date      DATE          NULL,
    trial_end_date  DATE          NULL,
    cancel_date     DATE          NULL,
    memo            VARCHAR(500)  NULL,
    package_name    VARCHAR(150)  NULL,
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_subscriptions_user_status (user_id, status),
    CONSTRAINT fk_subscriptions_user     FOREIGN KEY (user_id)     REFERENCES users (id)      ON DELETE CASCADE,
    CONSTRAINT fk_subscriptions_category FOREIGN KEY (category_id) REFERENCES categories (id),
    CONSTRAINT fk_subscriptions_service  FOREIGN KEY (service_id)  REFERENCES services (id)   ON DELETE SET NULL,
    CONSTRAINT chk_sub_status        CHECK (status IN ('ACTIVE', 'CANCEL_SCHEDULED', 'CANCELED')),
    CONSTRAINT chk_sub_plan_type     CHECK (plan_type IN ('PAID', 'TRIAL')),
    CONSTRAINT chk_sub_currency      CHECK (currency IN ('KRW', 'JPY', 'USD')),
    CONSTRAINT chk_sub_amount        CHECK (amount >= 0),
    CONSTRAINT chk_sub_billing_cycle CHECK (billing_cycle IN ('MONTHLY', 'YEARLY')),
    CONSTRAINT chk_sub_billing_day   CHECK (billing_day BETWEEN 1 AND 31),
    CONSTRAINT chk_sub_plan_rule CHECK (
        (plan_type = 'PAID'
            AND currency IS NOT NULL AND amount IS NOT NULL
            AND billing_cycle IS NOT NULL AND billing_day IS NOT NULL
            AND start_date IS NOT NULL AND trial_end_date IS NULL)
        OR
        (plan_type = 'TRIAL'
            AND trial_end_date IS NOT NULL
            AND currency IS NULL AND amount IS NULL
            AND billing_cycle IS NULL AND billing_day IS NULL)
    )
);

-- 7. PAYMENTS
CREATE TABLE payments (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    subscription_id  BIGINT        NOT NULL,
    user_id          BIGINT        NOT NULL,
    service_name     VARCHAR(50)   NOT NULL,
    paid_date        DATE          NOT NULL,
    billing_cycle    VARCHAR(10)   NOT NULL,
    currency         CHAR(3)       NOT NULL,
    amount           DECIMAL(12,2) NOT NULL,
    exchange_rate    DECIMAL(18,6) NOT NULL DEFAULT 1,
    rate_date        DATE          NULL,
    amount_krw       DECIMAL(14,0) NOT NULL,
    created_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_payments_subscription_date (subscription_id, paid_date),
    INDEX idx_payments_user_date (user_id, paid_date),
    CONSTRAINT fk_payments_subscription FOREIGN KEY (subscription_id) REFERENCES subscriptions (id) ON DELETE CASCADE,
    CONSTRAINT fk_payments_user         FOREIGN KEY (user_id)         REFERENCES users (id)         ON DELETE CASCADE,
    CONSTRAINT chk_pay_billing_cycle CHECK (billing_cycle IN ('MONTHLY', 'YEARLY')),
    CONSTRAINT chk_pay_currency      CHECK (currency IN ('KRW', 'JPY', 'USD'))
);

-- 8. EXCHANGE_RATES
CREATE TABLE exchange_rates (
    rate_date    DATE          NOT NULL,
    currency     CHAR(3)       NOT NULL,
    rate_to_krw  DECIMAL(18,6) NOT NULL,
    fetched_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (rate_date, currency),
    CONSTRAINT chk_er_currency CHECK (currency IN ('USD', 'JPY'))
);
