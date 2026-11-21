SET NAMES utf8mb4;

-- 카테고리 7개
INSERT INTO categories (id, name) VALUES
    (1, 'OTT'),
    (2, '음악'),
    (3, '외식'),
    (4, '쇼핑'),
    (5, 'AI'),
    (6, '디지털 콘텐츠'),
    (7, '기타');

-- 카테고리별 서비스 (sort_order 는 10 단위로 띄워서 나중에 사이에 끼워넣을 수 있게 함)
INSERT INTO services (id, category_id, name, sort_order) VALUES
    (1,  1, '넷플릭스',         10),
    (2,  1, '티빙',             20),
    (3,  1, '웨이브',           30),
    (4,  1, '쿠팡플레이',       40),
    (5,  1, '디즈니+',          50),
    (6,  1, '왓챠',             60),
    (7,  2, '유튜브 프리미엄',  10),
    (8,  2, '멜론',             20),
    (9,  2, '스포티파이',       30),
    (10, 2, '지니뮤직',         40),
    (11, 2, '바이브',           50),
    (12, 2, '플로',             60),
    (13, 3, '배민클럽',         10),
    (14, 3, '요기패스X',        20),
    (15, 3, '쿠팡이츠',         30),
    (16, 4, '쿠팡 와우',        10),
    (17, 4, '네이버플러스 멤버십', 20),
    (18, 5, 'ChatGPT Plus',     10),
    (19, 5, 'Claude Pro',       20),
    (20, 5, 'Gemini',           30),
    (21, 6, '리디',             10),
    (22, 6, '밀리의 서재',      20),
    (23, 6, '네이버 웹툰',      30);

-- 서비스 ↔ 앱 패키지명 (서비스 하나에 여러 개 가능)
INSERT INTO service_packages (service_id, package_name) VALUES
    (1,  'com.netflix.mediaclient'),
    (2,  'net.cj.cjhv.gs.tving'),
    (3,  'kr.co.captv.pooqV2'),
    (4,  'com.coupang.mobile.play'),
    (5,  'com.disney.disneyplus'),
    (6,  'com.frograms.wplay'),
    (7,  'com.google.android.youtube'),
    (7,  'com.google.android.apps.youtube.music'),
    (8,  'com.iloen.melon'),
    (9,  'com.spotify.music'),
    (10, 'com.ktmusic.geniemusic'),
    (11, 'com.naver.vibe'),
    (12, 'skplanet.musicmate'),
    (13, 'com.sampleapp'),
    (14, 'com.fineapp.yogiyo'),
    (15, 'com.coupang.mobile.eats'),
    (16, 'com.coupang.mobile'),
    (17, 'com.nhn.android.search'),
    (18, 'com.openai.chatgpt'),
    (19, 'com.anthropic.claude'),
    (20, 'com.google.android.apps.bard'),
    (21, 'com.initialcoms.ridi'),
    (22, 'kr.co.millie.millieshelf'),
    (23, 'com.nhn.android.webtoon');
