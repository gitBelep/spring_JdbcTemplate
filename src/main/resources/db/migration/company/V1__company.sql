CREATE TABLE `emp`(
 `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
 `emp_name` VARCHAR(255) NOT NULL UNIQUE COLLATE 'utf8_hungarian_ci',
 `age` INT NULL DEFAULT NULL,
PRIMARY KEY (`id`) USING BTREE)
COLLATE='utf8_hungarian_ci' ENGINE=InnoDB;

CREATE TABLE images (
 id bigint NOT NULL,
 filename varchar(255) COLLATE 'utf8_hungarian_ci',
 content BLOB NULL DEFAULT NULL,
primary key (id));
