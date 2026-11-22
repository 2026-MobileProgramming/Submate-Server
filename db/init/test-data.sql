SET NAMES utf8mb4;

-- 개발·테스트용 데이터 (결제 내역 조회, 리포트, 캘린더 확인용)
-- 테스트 계정: test@submate.com / Test1234!  (메일 인증 없이 바로 로그인 가능)
-- 결제 내역은 2026-11-22 기준으로 만든 값

INSERT INTO users (id, email, password, nickname) VALUES
    (1, 'test@submate.com', '$2b$10$sX/b3M3sDrI2yL9c1608L.nU.AGRynVQB9oQc9rW4zYMzRd6WtdN.', '테스트');

INSERT INTO subscriptions (id, user_id, category_id, service_id, service_name, status, plan_type,
                           currency, amount, billing_cycle, billing_day, start_date, trial_end_date, memo, package_name) VALUES
    (1, 1, 1, 1, 'Netflix', 'ACTIVE', 'PAID', 'KRW', 17000.00, 'MONTHLY', 5, '2026-10-05', NULL, '가족 계정', 'com.netflix.mediaclient'),
    (2, 1, 2, 11, 'Spotify', 'ACTIVE', 'PAID', 'KRW', 11990.00, 'MONTHLY', 20, '2026-10-20', NULL, NULL, 'com.spotify.music'),
    (3, 1, 5, 23, 'ChatGPT', 'ACTIVE', 'PAID', 'USD', 20.00, 'MONTHLY', 12, '2026-11-12', NULL, NULL, 'com.openai.chatgpt'),
    (4, 1, 4, 19, '쿠팡 와우', 'ACTIVE', 'PAID', 'KRW', 7890.00, 'MONTHLY', 31, '2026-06-30', NULL, '31일 결제 (말일 처리 확인용)', NULL),
    (5, 1, 1, 3, 'TVING', 'CANCELED', 'PAID', 'KRW', 13900.00, 'MONTHLY', 15, '2026-07-15', NULL, '9월 결제 후 해지', NULL),
    (6, 1, 7, 32, 'Google One', 'ACTIVE', 'PAID', 'KRW', 24000.00, 'YEARLY', 10, '2026-09-10', NULL, '연간 결제', NULL),
    (7, 1, 1, 6, '쿠팡플레이', 'ACTIVE', 'TRIAL', NULL, NULL, NULL, NULL, NULL, '2026-12-15', '무료체험', NULL);

INSERT INTO payments (subscription_id, user_id, service_name, paid_date, billing_cycle,
                      currency, amount, exchange_rate, rate_date, amount_krw) VALUES
    (1, 1, 'Netflix', '2026-10-05', 'MONTHLY', 'KRW', 17000.00, 1.000000, NULL, 17000),
    (1, 1, 'Netflix', '2026-11-05', 'MONTHLY', 'KRW', 17000.00, 1.000000, NULL, 17000),
    (2, 1, 'Spotify', '2026-10-20', 'MONTHLY', 'KRW', 11990.00, 1.000000, NULL, 11990),
    (2, 1, 'Spotify', '2026-11-20', 'MONTHLY', 'KRW', 11990.00, 1.000000, NULL, 11990),
    (3, 1, 'ChatGPT', '2026-11-12', 'MONTHLY', 'USD', 20.00, 1385.400000, '2026-11-12', 27700),
    (4, 1, '쿠팡 와우', '2026-06-30', 'MONTHLY', 'KRW', 7890.00, 1.000000, NULL, 7890),
    (4, 1, '쿠팡 와우', '2026-07-31', 'MONTHLY', 'KRW', 7890.00, 1.000000, NULL, 7890),
    (4, 1, '쿠팡 와우', '2026-08-31', 'MONTHLY', 'KRW', 7890.00, 1.000000, NULL, 7890),
    (4, 1, '쿠팡 와우', '2026-09-30', 'MONTHLY', 'KRW', 7890.00, 1.000000, NULL, 7890),
    (4, 1, '쿠팡 와우', '2026-10-31', 'MONTHLY', 'KRW', 7890.00, 1.000000, NULL, 7890),
    (5, 1, 'TVING', '2026-07-15', 'MONTHLY', 'KRW', 13900.00, 1.000000, NULL, 13900),
    (5, 1, 'TVING', '2026-08-15', 'MONTHLY', 'KRW', 13900.00, 1.000000, NULL, 13900),
    (5, 1, 'TVING', '2026-09-15', 'MONTHLY', 'KRW', 13900.00, 1.000000, NULL, 13900),
    (6, 1, 'Google One', '2026-09-10', 'YEARLY', 'KRW', 24000.00, 1.000000, NULL, 24000);

-- 최신 환율 (스케줄러가 저장하기 전 기본값)
INSERT INTO exchange_rates (rate_date, currency, rate_to_krw) VALUES
    ('2026-11-20', 'USD', 1385.400000),
    ('2026-11-20', 'JPY', 9.120000);
