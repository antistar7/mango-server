-- mango_fukuoka DB 베이스라인
--
-- 이 DB는 그동안 Flyway가 아예 걸려 있지 않아 모든 스키마 변경이
-- 수동 DDL로만 이루어졌다. 운영 DB의 현재 구조를 그대로 옮겨 담은 것이며,
-- 앞으로의 변경은 V2부터 추가한다.
--
-- 이미 존재하는 DB(운영/로컬)는 baseline-on-migrate로 baseline 기록만 남고
-- 이 파일을 건너뛴다. 빈 DB에서만 실행되어 전체 스키마를 만든다.
--
-- 주의: 컬럼 타입과 콜레이션은 운영 DB와 일치시켰다. Hibernate가
-- ddl-auto=validate로 부팅 시 스키마를 검증하므로 임의로 바꾸면 기동이 깨진다.

CREATE TABLE `city` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `slug` varchar(100) NOT NULL,
  `name` varchar(100) NOT NULL,
  `name_ja` varchar(100) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT 1,
  `sort_order` int(11) NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `updated_at` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_city_slug` (`slug`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE `place` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `city_id` bigint(20) DEFAULT NULL,
  `name` varchar(100) NOT NULL,
  `name_ja` varchar(100) DEFAULT NULL,
  `slug` varchar(100) NOT NULL,
  `description` text DEFAULT NULL,
  `latitude` decimal(10,7) DEFAULT NULL,
  `longitude` decimal(10,7) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `thumbnail_image` varchar(500) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `updated_at` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_place_slug` (`slug`),
  KEY `fk_place_city` (`city_id`),
  CONSTRAINT `fk_place_city` FOREIGN KEY (`city_id`) REFERENCES `city` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE `category` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `name_ja` varchar(100) DEFAULT NULL,
  `slug` varchar(100) NOT NULL,
  `icon` varchar(50) DEFAULT NULL,
  `sort_order` int(11) NOT NULL DEFAULT 0,
  `is_active` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_category_slug` (`slug`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE `content` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  `subtitle` varchar(255) DEFAULT NULL,
  `summary` text DEFAULT NULL,
  `body` longtext DEFAULT NULL,
  `place_id` bigint(20) DEFAULT NULL,
  `thumbnail_image` varchar(500) DEFAULT NULL,
  `hero_image` varchar(500) DEFAULT NULL,
  `status` varchar(30) NOT NULL DEFAULT 'DRAFT',
  `published_at` datetime DEFAULT NULL,
  `is_map_visible` tinyint(1) NOT NULL DEFAULT 0,
  `is_mango_pick` tinyint(1) NOT NULL DEFAULT 0,
  `mango_pick_order` int(11) NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `updated_at` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `idx_content_status` (`status`),
  KEY `idx_content_published_at` (`published_at`),
  KEY `idx_content_place_id` (`place_id`),
  CONSTRAINT `fk_content_place` FOREIGN KEY (`place_id`) REFERENCES `place` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE `content_category` (
  `content_id` bigint(20) NOT NULL,
  `category_id` bigint(20) NOT NULL,
  PRIMARY KEY (`content_id`,`category_id`),
  KEY `fk_cc_category` (`category_id`),
  CONSTRAINT `fk_cc_category` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_cc_content` FOREIGN KEY (`content_id`) REFERENCES `content` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE `content_images` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `content_id` bigint(20) NOT NULL,
  `image_url` varchar(1000) NOT NULL,
  `image_type` varchar(30) NOT NULL,
  `sort_order` int(11) NOT NULL DEFAULT 0,
  `caption` varchar(500) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `fk_content_images_content` (`content_id`),
  CONSTRAINT `fk_content_images_content` FOREIGN KEY (`content_id`) REFERENCES `content` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `japanese_expression` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `content_id` bigint(20) NOT NULL,
  `expression` varchar(500) NOT NULL,
  `translation` varchar(500) DEFAULT NULL,
  `reading` varchar(500) DEFAULT NULL,
  `audio_url` varchar(500) DEFAULT NULL,
  `note` text DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `updated_at` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `idx_expression_content_id` (`content_id`),
  CONSTRAINT `fk_expression_content` FOREIGN KEY (`content_id`) REFERENCES `content` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- 아래 두 테이블은 대응하는 JPA 엔티티가 아직 없지만
-- 운영 DB에 데이터와 함께 존재하므로 베이스라인에 포함한다.

CREATE TABLE `content_media` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `content_id` bigint(20) NOT NULL,
  `media_type` varchar(30) NOT NULL,
  `url` varchar(500) NOT NULL,
  `sort_order` int(11) NOT NULL DEFAULT 0,
  `caption` varchar(500) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `idx_media_content_id` (`content_id`),
  CONSTRAINT `fk_media_content` FOREIGN KEY (`content_id`) REFERENCES `content` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE `today_content` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `content_id` bigint(20) NOT NULL,
  `display_date` date NOT NULL,
  `sort_order` int(11) NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_today_content_date_sort` (`display_date`,`sort_order`),
  KEY `fk_today_content` (`content_id`),
  KEY `idx_today_display_date` (`display_date`),
  CONSTRAINT `fk_today_content` FOREIGN KEY (`content_id`) REFERENCES `content` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
