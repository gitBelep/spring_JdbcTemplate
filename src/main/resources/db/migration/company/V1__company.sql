CREATE TABLE `emp`(
 `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
 `emp_name` VARCHAR(255) NOT NULL COLLATE 'utf8_hungarian_ci',
 `age` INT NULL DEFAULT NULL,
 `content` BLOB NULL DEFAULT NULL,
PRIMARY KEY (`id`) USING BTREE)
COLLATE='utf8_hungarian_ci' ENGINE=InnoDB;
