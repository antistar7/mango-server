ALTER TABLE `content`
  ADD COLUMN `store_latitude` decimal(10,7) DEFAULT NULL AFTER `place_id`,
  ADD COLUMN `store_longitude` decimal(10,7) DEFAULT NULL AFTER `store_latitude`,
  ADD COLUMN `store_address` varchar(255) DEFAULT NULL AFTER `store_longitude`;
