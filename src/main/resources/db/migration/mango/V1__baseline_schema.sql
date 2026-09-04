-- mango DB 베이스라인
--
-- 이 스키마는 원래 수동 DDL과 과거 ddl-auto로 만들어져 버전 관리 밖에 있었다.
-- 운영 DB의 현재 구조를 그대로 옮겨 담은 것이며, 앞으로의 변경은 V2부터 추가한다.
--
-- 이미 존재하는 DB(운영/로컬)는 flyway baseline-version=1 때문에 이 파일을
-- 건너뛴다. 빈 DB에서만 실행되어 전체 스키마를 만든다.
--
-- 주의: 컬럼 타입과 콜레이션은 운영 DB와 일치시켰다. Hibernate가
-- ddl-auto=validate로 부팅 시 스키마를 검증하므로 임의로 바꾸면 기동이 깨진다.

CREATE TABLE `categories` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `icon` varchar(20) DEFAULT NULL,
  `sort_order` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE `sub_categories` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `category_id` bigint(20) NOT NULL,
  `name` varchar(100) NOT NULL,
  `sort_order` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `fk_sub_category_category` (`category_id`),
  CONSTRAINT `fk_sub_category_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE `contents` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `source_language` varchar(10) NOT NULL,
  `target_language` varchar(10) NOT NULL,
  `source_text` text NOT NULL,
  `target_text` text NOT NULL,
  `sub_category_id` bigint(20) DEFAULT NULL,
  `description` varchar(500) DEFAULT NULL,
  `difficulty` int(11) NOT NULL DEFAULT 1,
  `sort_order` int(11) NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `updated_at` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `fk_content_sub_category` (`sub_category_id`),
  CONSTRAINT `fk_content_sub_category` FOREIGN KEY (`sub_category_id`) REFERENCES `sub_categories` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE `content_examples` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `source_text` text NOT NULL,
  `target_text` text NOT NULL,
  `content_id` bigint(20) NOT NULL,
  `speaker` varchar(20) NOT NULL,
  `sort_order` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `fk_content_examples_content` (`content_id`),
  CONSTRAINT `fk_content_examples_content` FOREIGN KEY (`content_id`) REFERENCES `contents` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `study_histories` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `content_id` bigint(20) NOT NULL,
  `result` varchar(20) NOT NULL,
  `studied_at` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `fk_study_histories_content` (`content_id`),
  CONSTRAINT `fk_study_histories_content` FOREIGN KEY (`content_id`) REFERENCES `contents` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
