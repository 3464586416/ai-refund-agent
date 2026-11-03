-- 务必以 UTF-8 方式导入，否则中文会变乱码
SET NAMES utf8mb4;

-- 创建数据库
CREATE DATABASE IF NOT EXISTS ticketdb DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE ticketdb;

-- 老库重跑时避免外键阻止 DROP
SET FOREIGN_KEY_CHECKS = 0;

-- 顾客信息表
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

INSERT INTO `users` VALUES (1, '张三', 'password1', '13800138001');
INSERT INTO `users` VALUES (2, '李四', 'password2', '13900139001');

-- 旅程信息表
DROP TABLE IF EXISTS `trips`;
CREATE TABLE `trips` (
  `trip_id` int NOT NULL AUTO_INCREMENT,
  `departure` varchar(100) NOT NULL,
  `destination` varchar(100) NOT NULL,
  `departure_time` datetime NOT NULL,
  `arrival_time` datetime NOT NULL,
  PRIMARY KEY (`trip_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

INSERT INTO `trips` VALUES (1, '北京', '上海', '2025-05-01 08:00:00', '2025-05-01 18:00:00');
INSERT INTO `trips` VALUES (2, '上海', '广州', '2025-05-02 09:00:00', '2025-05-02 19:00:00');

-- 票面基本信息表
DROP TABLE IF EXISTS `tickets`;
CREATE TABLE `tickets` (
  `ticket_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `trip_id` int NOT NULL,
  `seat_number` varchar(10) NOT NULL,
  `price` decimal(8,2) NOT NULL,
  `purchase_time` datetime NOT NULL,
  `is_refunded` tinyint(1) DEFAULT 0,
  PRIMARY KEY (`ticket_id`),
  KEY `user_id` (`user_id`),
  KEY `trip_id` (`trip_id`),
  CONSTRAINT `tickets_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
  CONSTRAINT `tickets_ibfk_2` FOREIGN KEY (`trip_id`) REFERENCES `trips` (`trip_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

INSERT INTO `tickets` VALUES (1, 1, 1, 'A1', 500.00, '2025-04-20 10:00:00', 0);
INSERT INTO `tickets` VALUES (2, 2, 2, 'B2', 600.00, '2025-04-21 11:00:00', 0);

-- 退款记录表
DROP TABLE IF EXISTS `refund_records`;
CREATE TABLE `refund_records` (
  `record_id` int NOT NULL AUTO_INCREMENT,
  `ticket_id` int NOT NULL,
  `refund_time` datetime NOT NULL,
  `refund_amount` decimal(8,2) NOT NULL,
  PRIMARY KEY (`record_id`),
  KEY `ticket_id` (`ticket_id`),
  CONSTRAINT `refund_records_ibfk_1` FOREIGN KEY (`ticket_id`) REFERENCES `tickets` (`ticket_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

INSERT INTO `refund_records` VALUES (1, 1, '2025-04-22 14:00:00', 450.00);
INSERT INTO `refund_records` VALUES (2, 2, '2025-05-12 19:51:56', 600.00);

SET FOREIGN_KEY_CHECKS = 1;
