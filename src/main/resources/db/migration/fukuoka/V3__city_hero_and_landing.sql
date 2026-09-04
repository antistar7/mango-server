ALTER TABLE `city`
  ADD COLUMN `hero_image` varchar(500) DEFAULT NULL AFTER `description`;

CREATE TABLE `site_setting` (
  `setting_key` varchar(100) NOT NULL,
  `setting_value` varchar(1000) DEFAULT NULL,
  PRIMARY KEY (`setting_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
