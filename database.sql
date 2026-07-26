-- MySQL dump 10.13  Distrib 9.0.1, for Win64 (x86_64)
--
-- Host: localhost    Database: company_talk_system
-- ------------------------------------------------------
-- Server version	9.0.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `company_talk_system`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `company_talk_system` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `company_talk_system`;

--
-- Table structure for table `activity_participants`
--

DROP TABLE IF EXISTS `activity_participants`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `activity_participants` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `record_id` bigint NOT NULL,
  `user_job_no` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `activity_participants`
--

LOCK TABLES `activity_participants` WRITE;
/*!40000 ALTER TABLE `activity_participants` DISABLE KEYS */;
INSERT INTO `activity_participants` (`id`, `record_id`, `user_job_no`, `user_name`) VALUES (1,1,'000002','政工室科长'),(2,1,'000003','政工室副科长1'),(3,1,'000004','政工室组长1'),(4,1,'000005','政工室组员1'),(5,1,'000006','政工室组员2'),(6,1,'000007','政工室组员3'),(7,1,'000008','政工室组长2'),(8,1,'000009','政工室组员1'),(9,1,'000010','政工室组员2');
/*!40000 ALTER TABLE `activity_participants` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `activity_records`
--

DROP TABLE IF EXISTS `activity_records`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `activity_records` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `dept_id` int NOT NULL,
  `photos` text COLLATE utf8mb4_unicode_ci,
  `remark` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `submit_time` datetime(6) NOT NULL,
  `submitted_by` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `submitted_by_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `task_id` bigint NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `activity_records`
--

LOCK TABLES `activity_records` WRITE;
/*!40000 ALTER TABLE `activity_records` DISABLE KEYS */;
INSERT INTO `activity_records` (`id`, `content`, `dept_id`, `photos`, `remark`, `submit_time`, `submitted_by`, `submitted_by_name`, `task_id`) VALUES (1,'111111111111',2,'/uploads/e127f9cd-9b65-4442-8e10-439fb9503e87_IMG_0001.JPG','','2026-06-02 04:46:20.569513','000010','政工室组员2',1);
/*!40000 ALTER TABLE `activity_records` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `activity_tasks`
--

DROP TABLE IF EXISTS `activity_tasks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `activity_tasks` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `create_time` datetime(6) NOT NULL,
  `created_by` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_by_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `deadline` datetime(6) NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `status` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `task_type` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `activity_tasks`
--

LOCK TABLES `activity_tasks` WRITE;
/*!40000 ALTER TABLE `activity_tasks` DISABLE KEYS */;
INSERT INTO `activity_tasks` (`id`, `create_time`, `created_by`, `created_by_name`, `deadline`, `description`, `status`, `task_type`, `title`) VALUES (1,'2026-06-02 04:45:23.926537','admin','系统管理员','2026-06-18 04:45:20.000000','1','ACTIVE','LEARNING','夜学');
/*!40000 ALTER TABLE `activity_tasks` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `audit_logs`
--

DROP TABLE IF EXISTS `audit_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `audit_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `action` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `create_time` datetime(6) NOT NULL,
  `detail` text COLLATE utf8mb4_unicode_ci,
  `ip_address` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `operator_job_no` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `operator_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `target_id` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `target_type` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `audit_logs`
--

LOCK TABLES `audit_logs` WRITE;
/*!40000 ALTER TABLE `audit_logs` DISABLE KEYS */;
INSERT INTO `audit_logs` (`id`, `action`, `create_time`, `detail`, `ip_address`, `operator_job_no`, `operator_name`, `target_id`, `target_type`) VALUES (1,'LOGIN','2026-06-02 03:27:13.287203','用户登录：局长（000001）','127.0.0.1','SYSTEM','系统','000001','USER'),(2,'LOGIN','2026-06-02 03:28:16.033703','用户登录：系统管理员（admin）','127.0.0.1','SYSTEM','系统','admin','USER'),(3,'CREATE_ACTIVITY_TASK','2026-06-02 04:45:24.178412','创建活动任务：夜学，下发20个单位','127.0.0.1','admin','系统管理员','1','ACTIVITY_TASK'),(4,'UPDATE_USER','2026-06-02 04:45:39.101487','更新用户信息：政工室组员2（000010），字段：[name, position, role, riskLevel, isKeyPersonnel, operatorJobNo]','127.0.0.1','admin','系统管理员','000010','USER'),(5,'LOGIN','2026-06-02 04:45:52.974150','用户登录：政工室组员2（000010）','127.0.0.1','SYSTEM','系统','000010','USER'),(6,'SUBMIT_ACTIVITY_RECORD','2026-06-02 04:46:20.681145','提交活动记录：夜学，参与人9人','127.0.0.1','000010','政工室组员2','1','ACTIVITY_RECORD'),(7,'LOGIN','2026-06-02 04:50:02.625502','用户登录：局长（000001）','127.0.0.1','SYSTEM','系统','000001','USER'),(8,'LOGIN','2026-06-02 04:50:19.665923','用户登录：系统管理员（admin）','127.0.0.1','SYSTEM','系统','admin','USER');
/*!40000 ALTER TABLE `audit_logs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `depts`
--

DROP TABLE IF EXISTS `depts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `depts` (
  `id` int NOT NULL AUTO_INCREMENT,
  `dept_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `depts`
--

LOCK TABLES `depts` WRITE;
/*!40000 ALTER TABLE `depts` DISABLE KEYS */;
INSERT INTO `depts` (`id`, `dept_name`) VALUES (1,'办公室'),(2,'政工室'),(3,'情报指挥中心'),(4,'政治安全保卫大队'),(5,'治安管理大队'),(6,'刑事侦查大队'),(7,'网络安全保卫大队'),(8,'交通管理大队'),(9,'法治大队'),(10,'督察审计大队'),(11,'经济犯罪侦查大队'),(12,'资源环境和食品药品犯罪侦查大队'),(13,'巡特警大队'),(14,'县看守所'),(15,'城北派出所'),(16,'城南派出所'),(17,'梅源派出所'),(18,'石塘派出所'),(19,'紧水滩派出所'),(20,'云和湖派出所');
/*!40000 ALTER TABLE `depts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hierarchy_history`
--

DROP TABLE IF EXISTS `hierarchy_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hierarchy_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `end_date` datetime(6) DEFAULT NULL,
  `manager_job_no` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `manager_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `start_date` datetime(6) NOT NULL,
  `target_job_no` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `unit_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hierarchy_history`
--

LOCK TABLES `hierarchy_history` WRITE;
/*!40000 ALTER TABLE `hierarchy_history` DISABLE KEYS */;
INSERT INTO `hierarchy_history` (`id`, `end_date`, `manager_job_no`, `manager_name`, `start_date`, `target_job_no`, `unit_name`) VALUES (1,'2026-05-28 01:23:02.169855','000008','政工室组长2','2026-05-28 01:23:02.197780','000011','政工室'),(2,NULL,'000001','局长','2026-05-28 01:23:02.299707','000011','办公室');
/*!40000 ALTER TABLE `hierarchy_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `home_visits`
--

DROP TABLE IF EXISTS `home_visits`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `home_visits` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `location` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `operator_job_no` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `photo` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `target_job_no` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `visit_time` datetime(6) NOT NULL,
  `visit_type` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `home_visits`
--

LOCK TABLES `home_visits` WRITE;
/*!40000 ALTER TABLE `home_visits` DISABLE KEYS */;
INSERT INTO `home_visits` (`id`, `content`, `created_at`, `location`, `operator_job_no`, `photo`, `target_job_no`, `visit_time`, `visit_type`) VALUES (1,'1','2026-06-03 16:40:28.629637','','000001','/uploads/221022f9-18f2-4e91-ba21-b2fa05d53c46_论文一篇.png','000182','2026-06-03 16:40:11.000000','例行家访'),(2,'000001创建','2026-06-07 12:54:31.077603','测试','000001',NULL,'000005','2026-06-07 06:00:00.000000','例行家访'),(3,'000002创建','2026-06-07 12:54:31.279113','测试','000002',NULL,'000005','2026-06-07 06:00:00.000000','例行家访');
/*!40000 ALTER TABLE `home_visits` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notifications`
--

DROP TABLE IF EXISTS `notifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notifications` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `create_time` datetime(6) NOT NULL,
  `is_read` bit(1) NOT NULL,
  `related_id` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_job_no` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notifications`
--

LOCK TABLES `notifications` WRITE;
/*!40000 ALTER TABLE `notifications` DISABLE KEYS */;
INSERT INTO `notifications` (`id`, `content`, `create_time`, `is_read`, `related_id`, `title`, `type`, `user_job_no`) VALUES (1,'上级下发了活动任务「自动化测试活动」，请在2026-12-31 23:59:59前完成填报。','2026-06-07 12:59:46.201916',_binary '\0','3','新活动任务','ACTIVITY_TASK','000010');
/*!40000 ALTER TABLE `notifications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `permissions`
--

DROP TABLE IF EXISTS `permissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `permissions` (
  `id` int NOT NULL AUTO_INCREMENT,
  `category` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_7lcb6glmvwlro3p2w2cewxtvd` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `permissions`
--

LOCK TABLES `permissions` WRITE;
/*!40000 ALTER TABLE `permissions` DISABLE KEYS */;
INSERT INTO `permissions` (`id`, `category`, `code`, `description`, `name`) VALUES (1,'DASHBOARD','GLOBAL_DASHBOARD','查看全局管理看板','全局看板'),(2,'MANAGEMENT','PERSONNEL_MANAGE','新增/编辑/删除人员，分配角色和上级','人事管理'),(3,'MANAGEMENT','ACTIVITY_PUBLISH','创建和管理活动任务','活动发布'),(6,'REPORTS','STAT_REPORTS','查看统计报表和导出','统计报表'),(7,'MANAGEMENT','HIERARCHY_MANAGE','配置本部门人事层级和上下级归属','层级管理');
/*!40000 ALTER TABLE `permissions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `talk_records`
--

DROP TABLE IF EXISTS `talk_records`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `talk_records` (
  `id` int NOT NULL AUTO_INCREMENT,
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `create_time` datetime(6) DEFAULT NULL,
  `location` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `photo` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `talk_time` datetime(6) NOT NULL,
  `talk_type` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `talker_job_no` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `target_job_no` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `talk_records`
--

LOCK TABLES `talk_records` WRITE;
/*!40000 ALTER TABLE `talk_records` DISABLE KEYS */;
INSERT INTO `talk_records` (`id`, `content`, `create_time`, `location`, `photo`, `talk_time`, `talk_type`, `talker_job_no`, `target_job_no`) VALUES (1,'1','2026-06-03 16:40:08.743557','办公室','/uploads/bd9b1e1c-ea5f-44be-b68e-e70ebb4c872e_论文一篇.png','2026-06-03 16:39:50.000000','日常沟通','000001','000182'),(2,'000001创建','2026-06-07 12:54:30.999599','办公室',NULL,'2026-06-07 02:00:00.000000','日常沟通','000001','000005');
/*!40000 ALTER TABLE `talk_records` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `task_targets`
--

DROP TABLE IF EXISTS `task_targets`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `task_targets` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `dept_id` int NOT NULL,
  `dept_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `submit_time` datetime(6) DEFAULT NULL,
  `task_id` bigint NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `task_targets`
--

LOCK TABLES `task_targets` WRITE;
/*!40000 ALTER TABLE `task_targets` DISABLE KEYS */;
INSERT INTO `task_targets` (`id`, `dept_id`, `dept_name`, `status`, `submit_time`, `task_id`) VALUES (1,1,'办公室','PENDING',NULL,1),(2,2,'政工室','SUBMITTED','2026-06-02 04:46:20.673034',1),(3,3,'情报指挥中心','PENDING',NULL,1),(4,4,'政治安全保卫大队','PENDING',NULL,1),(5,5,'治安管理大队','PENDING',NULL,1),(6,6,'刑事侦查大队','PENDING',NULL,1),(7,7,'网络安全保卫大队','PENDING',NULL,1),(8,8,'交通管理大队','PENDING',NULL,1),(9,9,'法治大队','PENDING',NULL,1),(10,10,'督察审计大队','PENDING',NULL,1),(11,11,'经济犯罪侦查大队','PENDING',NULL,1),(12,12,'资源环境和食品药品犯罪侦查大队','PENDING',NULL,1),(13,13,'巡特警大队','PENDING',NULL,1),(14,14,'县看守所','PENDING',NULL,1),(15,15,'城北派出所','PENDING',NULL,1),(16,16,'城南派出所','PENDING',NULL,1),(17,17,'梅源派出所','PENDING',NULL,1),(18,18,'石塘派出所','PENDING',NULL,1),(19,19,'紧水滩派出所','PENDING',NULL,1),(20,20,'云和湖派出所','PENDING',NULL,1);
/*!40000 ALTER TABLE `task_targets` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_permissions`
--

DROP TABLE IF EXISTS `user_permissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_permissions` (
  `user_id` int NOT NULL,
  `permission_id` int NOT NULL,
  PRIMARY KEY (`user_id`,`permission_id`),
  KEY `FKq4qlrabt4s0etm9tfkoqfuib1` (`permission_id`),
  CONSTRAINT `FKkowxl8b2bngrxd1gafh13005u` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKq4qlrabt4s0etm9tfkoqfuib1` FOREIGN KEY (`permission_id`) REFERENCES `permissions` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_permissions`
--

LOCK TABLES `user_permissions` WRITE;
/*!40000 ALTER TABLE `user_permissions` DISABLE KEYS */;
INSERT INTO `user_permissions` (`user_id`, `permission_id`) VALUES (1,1),(2,1),(1,2),(1,3),(10,3),(1,6),(2,6),(10,6),(11,6),(11,7);
/*!40000 ALTER TABLE `user_permissions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `avatar` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `create_time` datetime(6) DEFAULT NULL,
  `is_key_personnel` bit(1) NOT NULL,
  `job_no` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `position` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `risk_level` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `role` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `superior_job_no` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `dept_id` int DEFAULT NULL,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_stxwshf2346ucwycm0f4ltr2f` (`job_no`),
  KEY `FK1ihhim4vm8utursyc6418py99` (`dept_id`),
  CONSTRAINT `FK1ihhim4vm8utursyc6418py99` FOREIGN KEY (`dept_id`) REFERENCES `depts` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=193 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` (`id`, `avatar`, `create_time`, `is_key_personnel`, `job_no`, `name`, `password`, `position`, `risk_level`, `role`, `superior_job_no`, `dept_id`, `phone`) VALUES (1,NULL,NULL,_binary '\0','admin','系统管理员','$2a$10$/VaX2Ls6tVPLX6K2c.eRxetXFxmw9CrrCWA0H7Liw..uZ5JM03r1G','系统管理员','NORMAL','ADMIN_GLOBAL',NULL,1,NULL),(2,NULL,NULL,_binary '\0','000001','局长','$2a$10$xNEjFzdZ/aenvGT5XwUgU.E8nQX7dgOjOI6GV6r5NIbT8f/IpZqOC','局长','NORMAL','USER',NULL,1,NULL),(3,NULL,NULL,_binary '\0','000002','政工室科长','$2a$10$TibdjmMF39uZ5fyCVA.AhOshT60Pez/0PhaIdDoeP6yzQpKlT9YWS','科长','NORMAL','USER','000001',2,NULL),(4,NULL,NULL,_binary '\0','000003','政工室副科长1','$2a$10$xK7YUTUMz3wpfGHFyVg8A.8vIf2yth5nMs6/zWJMRQ1HJ1r2VsvVW','副科长','NORMAL','USER','000002',2,NULL),(5,NULL,NULL,_binary '\0','000004','政工室组长1','$2a$10$9FbfxyZ2NrVSncc2FvXHLu7q6LCqkkJhrIsAMjIlybwEKThzWwVTq','组长','NORMAL','USER','000003',2,NULL),(6,NULL,NULL,_binary '\0','000005','政工室组员1','$2a$10$OTuGzy2E81m.K9/31BbeLOxrJvyIBYIGWTRcwqnRa8BiQBN37rtn.','组员','NORMAL','USER','000004',2,NULL),(7,NULL,NULL,_binary '\0','000006','政工室组员2','$2a$10$InIC7hCR6r2p62ONAAGvY.oJ5nPPA/3W1NmQunLTSWvlXkcTgaJtG','组员','NORMAL','USER','000004',2,NULL),(8,NULL,NULL,_binary '\0','000007','政工室组员3','$2a$10$sFS1dfrHD3Mki9/yfdLZieVDbWbjPOWoS2JgAntwJBRN5PpR24nJK','组员','NORMAL','USER','000004',2,NULL),(9,NULL,NULL,_binary '\0','000008','政工室组长2','$2a$10$e.s9MBcAY5q4PK7Oj6.EluyEj4NzUV5OU0/.vzFXppi.FLWurj3la','组长','NORMAL','USER','000003',2,NULL),(10,NULL,NULL,_binary '\0','000009','政工室组员1','$2a$10$C.GFIUX703laBc7K48p9PehjSpwpW49tWWg14wlcm0x6sLBXqvSeC','组员','NORMAL','USER','000008',2,NULL),(11,NULL,NULL,_binary '\0','000010','政工室组员2','$2a$10$PhcTOKWMatl6aVAWh7b3aOjSXAW2Bgqq9YgNAk8tsUnjdAZ1OqRqi','组员','NORMAL','ADMIN_UNIT','000008',2,NULL),(12,NULL,'2026-05-28 01:23:02.299707',_binary '\0','000011','政工室组员3','$2a$10$aU01X9V5PkI4CbU7dPnVOu7.qlFeeOvqJiYnGVyQtTCFhoxZSciwq','组员','ATTENTION','USER','000001',1,NULL),(13,NULL,NULL,_binary '\0','000012','情报指挥中心负责人','$2a$10$7RtiYAaSqm6NMWimqFuuFuhQUSlECf67GTt7gXB7wbC3CaMLaRRNe','负责人','NORMAL','USER','000001',3,NULL),(14,NULL,NULL,_binary '\0','000013','情报指挥中心副职1','$2a$10$NpDyU3GIy/Eku/vrZbJ9tud4kwVCzvF.KXId1zERnov24Q0Mam4nm','副职','RISK','USER','000012',3,NULL),(15,NULL,NULL,_binary '\0','000014','情报指挥中心组长1','$2a$10$Ja9uBQmJu9myI6RHtAWzW.gu7INCVRY6TLAQ9JRcq332GK.PNQV2W','组长','NORMAL','USER','000013',3,NULL),(16,NULL,NULL,_binary '\0','000015','情报指挥中心组员1','$2a$10$LkStydydiGKhhRCGcG7yZONnjXYpC2lDn1mUA/xyyfG/Z8PbB6mfa','组员','NORMAL','USER','000014',3,NULL),(17,NULL,NULL,_binary '\0','000016','情报指挥中心组员2','$2a$10$xrkUBt7lZhnszIQrsGkFhuft6aSv.R01IoYiOc06.qkkuqSyKFJS6','组员','NORMAL','USER','000014',3,NULL),(18,NULL,NULL,_binary '','000017','情报指挥中心组员3','$2a$10$TS8Svd6Q1Vhk8Roc8QrbcuOfsF7EuJaTChoEeOrc16/sy6cd/bOg6','组员','KEY','USER','000014',3,NULL),(19,NULL,NULL,_binary '\0','000018','情报指挥中心组长2','$2a$10$URKyxn4B8PBOEnkREhYGRuNvQzIai8iz5DWm.mgDtmygcYHvD.7N6','组长','NORMAL','USER','000013',3,NULL),(20,NULL,NULL,_binary '\0','000019','情报指挥中心组员1','$2a$10$IA.EqXekoOondhUdnmil7uJcdxCVTCOnkEsymorxruZ6HPDEQ6BOK','组员','NORMAL','USER','000018',3,NULL),(21,NULL,NULL,_binary '\0','000020','情报指挥中心组员2','$2a$10$IIfG6ghJ3wC2jQ/hY/uM1.getyXwstyOHiYO65lHCB2E0kl3cnaa6','组员','NORMAL','USER','000018',3,NULL),(22,NULL,NULL,_binary '\0','000021','情报指挥中心组员3','$2a$10$E3Oi/mtFTHp8AvZTS.44W.ohCSV8AmDlNViSBrwJSpUW/ZM0YfuZC','组员','NORMAL','USER','000018',3,NULL),(23,NULL,NULL,_binary '\0','000022','政治安全保卫大队队长','$2a$10$GdsQS5uno0EUaOZ76c60Ce1nHdbNOWl4irHKzc0jPF15cwBcFORxa','队长','ATTENTION','USER','000001',4,NULL),(24,NULL,NULL,_binary '\0','000023','政治安全保卫大队副队长1','$2a$10$EceaSjIFm8yIpotyp1yUReyTfH5fWdqBzHIdL6wCsVYksfZDTRnVi','副队长','NORMAL','USER','000022',4,NULL),(25,NULL,NULL,_binary '\0','000024','政治安全保卫大队组长1','$2a$10$NRm9itouzO7Bql0crDmcbuyKS2UtOeype0Vs.x6vl7J09v4tBoGFu','组长','NORMAL','USER','000023',4,NULL),(26,NULL,NULL,_binary '\0','000025','政治安全保卫大队组员1','$2a$10$JE/KQ3XIbPwNDzumIVK8lO2h8Kn4HqL0r.U3Sg1WiNpyjZdAwU7T6','组员','NORMAL','USER','000024',4,NULL),(27,NULL,NULL,_binary '\0','000026','政治安全保卫大队组员2','$2a$10$eTYuJ.vtnSudcwwyIjjUhuWeasxXZohZGo7ZUddFGkPeSj8PWhW5G','组员','RISK','USER','000024',4,NULL),(28,NULL,NULL,_binary '\0','000027','政治安全保卫大队组员3','$2a$10$qb1seeXQjLX9fC027tzsZ.nu3FnSNlJMfPatptEMrk5VCQ6BKvhsi','组员','NORMAL','USER','000024',4,NULL),(29,NULL,NULL,_binary '\0','000028','政治安全保卫大队组长2','$2a$10$i6gJtaY1aan9bebgTxMVHeWIKmEy2NQTE.Ypqc93XG0dJneAjemae','组长','NORMAL','USER','000023',4,NULL),(30,NULL,NULL,_binary '\0','000029','政治安全保卫大队组员1','$2a$10$XO6qEob8FjO1.9uu5bSLBejQomWnwWA1jT.m0IHLcyazrVvHdskIy','组员','NORMAL','USER','000028',4,NULL),(31,NULL,NULL,_binary '\0','000030','政治安全保卫大队组员2','$2a$10$OQyidoACDTHhHzcU5/MjzeIX4F0aLWjgzgDAkQ3ZJi2J0gkF1Lnlu','组员','NORMAL','USER','000028',4,NULL),(32,NULL,NULL,_binary '\0','000031','政治安全保卫大队组员3','$2a$10$bI09IHo2kW7ZEXqg1bPI3eECm2A49rfepWA2MEb3CFPy6WvuajQCa','组员','NORMAL','USER','000028',4,NULL),(33,NULL,NULL,_binary '\0','000032','治安管理大队队长','$2a$10$MRxW7H.RbfQHXoadptmYQ.t9KErb/fXmDNxMwLSgdtfr.FbPbrJVa','队长','NORMAL','USER','000001',5,NULL),(34,NULL,NULL,_binary '\0','000033','治安管理大队副队长1','$2a$10$E9/odrr/MQuKc28QXHEfVO4lj4IzETepl7we.6jtwhwsrcWyOU.8m','副队长','ATTENTION','USER','000032',5,NULL),(35,NULL,NULL,_binary '','000034','治安管理大队组长1','$2a$10$gFo1/LMZzlMiWEbYEA/tUuQSl3MyL5.ekDmvZjILNxyGBstaapXT6','组长','KEY','USER','000033',5,NULL),(36,NULL,NULL,_binary '\0','000035','治安管理大队组员1','$2a$10$EmsUEWiodCiciPw0xRnzCeYvRGoe5/XvhygoktuHXwCKLJwwzhkAq','组员','NORMAL','USER','000034',5,NULL),(37,NULL,NULL,_binary '\0','000036','治安管理大队组员2','$2a$10$VsOqc0ohn.Ibo1L0herqjesLMmEw.ee8aWjJ1p4T9xomV04fHR4YO','组员','NORMAL','USER','000034',5,NULL),(38,NULL,NULL,_binary '\0','000037','治安管理大队组员3','$2a$10$gjL5Xt8R960qfD1pSDHYO.rIBE42/YZS8JZB76Km2ForB1PeTArK.','组员','NORMAL','USER','000034',5,NULL),(39,NULL,NULL,_binary '\0','000038','治安管理大队组长2','$2a$10$fqtx8Sx9bVddlbBUcVIPNuKWb0r5Ilvix.JcOZ1.zHfX5p1g4/Z8W','组长','NORMAL','USER','000033',5,NULL),(40,NULL,NULL,_binary '\0','000039','治安管理大队组员1','$2a$10$aR.GV8wPVDGfBT43Zto34OzE4f70aR08D7Vi9d0gacG4bClj9J0XS','组员','RISK','USER','000038',5,NULL),(41,NULL,NULL,_binary '\0','000040','治安管理大队组员2','$2a$10$R3dQKvOe1EsZUCK4qmD8L.oXpuYkrKYq5ltlPCzgLS2oLTGnY5x2m','组员','NORMAL','USER','000038',5,NULL),(42,NULL,NULL,_binary '\0','000041','治安管理大队组员3','$2a$10$/HDrQY426mxfQ0k305MGEetPuvYrNRHK0h93FH17sSp4Zuz6IMJfG','组员','NORMAL','USER','000038',5,NULL),(43,NULL,NULL,_binary '\0','000042','刑事侦查大队队长','$2a$10$syILTh8bmpgbAYxKCp7E1.Dgu3wldqs5iYvFYkXsTVJXYASIPsBci','队长','NORMAL','USER','000001',6,NULL),(44,NULL,NULL,_binary '\0','000043','刑事侦查大队副队长1','$2a$10$M2ew0d.SkTg62zHSZMGfRu.HSHMt1T5zHeRFUWqF2UgH/x84bdNJm','副队长','NORMAL','USER','000042',6,NULL),(45,NULL,NULL,_binary '\0','000044','刑事侦查大队组长1','$2a$10$F/aUHRWMbwQY1Ywybz0exOZzaWPCw48OvMWuC2sMdL3e3rrJWVu/i','组长','ATTENTION','USER','000043',6,NULL),(46,NULL,NULL,_binary '\0','000045','刑事侦查大队组员1','$2a$10$zKJX4e6wBhpr2LiJ1vAz8eXbWkhKZlay8d4oue4Gbjqozyt8n6/Oy','组员','NORMAL','USER','000044',6,NULL),(47,NULL,NULL,_binary '\0','000046','刑事侦查大队组员2','$2a$10$zX3ILgGoAWN7vtg5GMqaw.y.SXSstL6mcsy61GSMYWQZF5Zq.1kgq','组员','NORMAL','USER','000044',6,NULL),(48,NULL,NULL,_binary '\0','000047','刑事侦查大队组员3','$2a$10$NPtuV6YtX5RjlxcgSFq0hO3qCdsXNEwwVL8rQOilxpAG7xRsL5Xoe','组员','NORMAL','USER','000044',6,NULL),(49,NULL,NULL,_binary '\0','000048','刑事侦查大队组长2','$2a$10$atFXSmvxec2Q7O.QLAjJS.t/pj94eQUZrX8gX3YhlXNpDVVVZV2wy','组长','NORMAL','USER','000043',6,NULL),(50,NULL,NULL,_binary '\0','000049','刑事侦查大队组员1','$2a$10$LtUQAPdP2ODTCzbn6N4LheGmNEjhtXoTWlUx0XZiNA.O.2z/AhVwK','组员','NORMAL','USER','000048',6,NULL),(51,NULL,NULL,_binary '\0','000050','刑事侦查大队组员2','$2a$10$gbYho/e1SaPzm9z5eO9LxeLNyz/8iSTI5Tp8km6Nr4vqB2qJSdD52','组员','NORMAL','USER','000048',6,NULL),(52,NULL,NULL,_binary '','000051','刑事侦查大队组员3','$2a$10$dgNjF.iDuQtZVqZOD7uT5.YG.86XXeJRZnGUOnA0xFsF6L94TexVq','组员','KEY','USER','000048',6,NULL),(53,NULL,NULL,_binary '\0','000052','网络安全保卫大队队长','$2a$10$Li7LND74fpWC6cJZO4pY5uOZkDAO5PAd37p3LhBL2RSckrMWXE38e','队长','RISK','USER','000001',7,NULL),(54,NULL,NULL,_binary '\0','000053','网络安全保卫大队副队长1','$2a$10$tIWnLKCrAVQN9VcbS4lspeBmdyzECFTBiWjqlZXCbDUO6TZkCciaq','副队长','NORMAL','USER','000052',7,NULL),(55,NULL,NULL,_binary '\0','000054','网络安全保卫大队组长1','$2a$10$IEOAdV.UR7BtwyKBarcvCeksktvT2DV6uhomYIOr2dYEWMG4iOzS6','组长','NORMAL','USER','000053',7,NULL),(56,NULL,NULL,_binary '\0','000055','网络安全保卫大队组员1','$2a$10$2G3BlLkmPvIxfzqzTyr1PuFSSDLiWM3wPgY/GshURrDJHXIeklg3S','组员','ATTENTION','USER','000054',7,NULL),(57,NULL,NULL,_binary '\0','000056','网络安全保卫大队组员2','$2a$10$plmaqPDi/c6jmak7dPwNM.mAQTrhylET0bxboIYcp1liI2uEyJsVe','组员','NORMAL','USER','000054',7,NULL),(58,NULL,NULL,_binary '\0','000057','网络安全保卫大队组员3','$2a$10$ksBX9zqz7o.IjwtnaWAyvub8OU/5nvBjpur1Ys2jDk/7k.E8SAKjK','组员','NORMAL','USER','000054',7,NULL),(59,NULL,NULL,_binary '\0','000058','网络安全保卫大队组长2','$2a$10$lOnuvh6FAKhs6xj1tekxBu.iw4vHQESSlLr1UAceZQekVCXHuM7O2','组长','NORMAL','USER','000053',7,NULL),(60,NULL,NULL,_binary '\0','000059','网络安全保卫大队组员1','$2a$10$T.ncaDgRj6/lUSjEvbgzuO9pXjpHiVaHpZHFfIYCGlNBn2UERW/py','组员','NORMAL','USER','000058',7,NULL),(61,NULL,NULL,_binary '\0','000060','网络安全保卫大队组员2','$2a$10$uwAMK3B5pow3Ymnqkury7.STl0HM6N0ZRCzDu/qV1IfCN.5Jp27Ta','组员','NORMAL','USER','000058',7,NULL),(62,NULL,NULL,_binary '\0','000061','网络安全保卫大队组员3','$2a$10$PVCsXuM4eZa7BcuFYAji8.sGvx6LrBf.JJReCdiHSckUu0bfXi70.','组员','NORMAL','USER','000058',7,NULL),(63,NULL,NULL,_binary '\0','000062','交通管理大队队长','$2a$10$Bp4yLr2NX9aT1k67/Q1/Ke024HVkD2TexVE5uA.KIIQXDSmabAZgW','队长','NORMAL','USER','000001',8,NULL),(64,NULL,NULL,_binary '\0','000063','交通管理大队副队长1','$2a$10$nDE20pnzhRzt4NNNaqQsOOIRQyxAOJ9fXR24Dta3BKF/PaX/46I1i','副队长','NORMAL','USER','000062',8,NULL),(65,NULL,NULL,_binary '\0','000064','交通管理大队组长1','$2a$10$9gI0JVS5/dK8dCK22d3xteZ5OxYe9N44OesEnXmFzkG5Ed6FmGVnK','组长','NORMAL','USER','000063',8,NULL),(66,NULL,NULL,_binary '\0','000065','交通管理大队组员1','$2a$10$gCch/3CReCCK6kapcduf5u4MMTR8MFR3xH2bEdMTV4Yz1JfQliNP6','组员','RISK','USER','000064',8,NULL),(67,NULL,NULL,_binary '\0','000066','交通管理大队组员2','$2a$10$neuDBvRiXTjfUMk7N9i1MObyohkNfH7aMaQh7DoHaqklyul62Yx4C','组员','ATTENTION','USER','000064',8,NULL),(68,NULL,NULL,_binary '\0','000067','交通管理大队组员3','$2a$10$kqhGUuCH9egG5b4Dsdg3OOp05mQ2CjFE5nfrNSAh94b1AFzb7UZRa','组员','NORMAL','USER','000064',8,NULL),(69,NULL,NULL,_binary '','000068','交通管理大队组长2','$2a$10$BG.HBMatHSPqOY.zhnqPFuALNKi.vTCv0KDj464AOlWdsQ8k5.IJm','组长','KEY','USER','000063',8,NULL),(70,NULL,NULL,_binary '\0','000069','交通管理大队组员1','$2a$10$q8xFwmF7evSxnJuyf3njHOr5I2hH.WPyY3/joymoAxEexZHOA8j7e','组员','NORMAL','USER','000068',8,NULL),(71,NULL,NULL,_binary '\0','000070','交通管理大队组员2','$2a$10$jkrbN1UZuZ03oX1DHymxH.cjSmLvZzD5tw72TJ2VLxAvgsTb2z6t2','组员','NORMAL','USER','000068',8,NULL),(72,NULL,NULL,_binary '\0','000071','交通管理大队组员3','$2a$10$CZJLi7xVAATvHSUawwNL2eSJ6gA4rLWt2uZ0Eu7mt2b5qV.n20XsS','组员','NORMAL','USER','000068',8,NULL),(73,NULL,NULL,_binary '\0','000072','法治大队队长','$2a$10$TpsyI4oPwb996ZpnsC1jWeobFBrKBFn7YTzyHpDH1q5.h4fxCumCa','队长','NORMAL','USER','000001',9,NULL),(74,NULL,NULL,_binary '\0','000073','法治大队副队长1','$2a$10$vD6Ft2R/LN4YfOM7XD5lqOBgpdTOIe8QcMDsVRI/qTes7xOnd4ISS','副队长','NORMAL','USER','000072',9,NULL),(75,NULL,NULL,_binary '\0','000074','法治大队组长1','$2a$10$BkmHwZ7f6U72El..hb5Ll.QIYUagt5j83Td5D9pZJhaBfPH7403Uu','组长','NORMAL','USER','000073',9,NULL),(76,NULL,NULL,_binary '\0','000075','法治大队组员1','$2a$10$Q7rMKwGabdQ/OMl1AuHmq.ihCKXIi9Fx55GQCp/SnP77RF.KUeM6C','组员','NORMAL','USER','000074',9,NULL),(77,NULL,NULL,_binary '\0','000076','法治大队组员2','$2a$10$c3FoAZpbTacle500UYB.cOw0S3f3yvyXrZzCvXqCD/YNxNSJgyHVG','组员','NORMAL','USER','000074',9,NULL),(78,NULL,NULL,_binary '\0','000077','法治大队组员3','$2a$10$cNgen9UqOFXJ25E59lMfH.uXbmEosMEUoI6oB.9vtwarbDp2jXyyq','组员','ATTENTION','USER','000074',9,NULL),(79,NULL,NULL,_binary '\0','000078','法治大队组长2','$2a$10$aJUR9CIcRfnoL5ZA8M6Idu7vUr1C/BhxRHEu0S9Ds1Nf7rQ3xUOEq','组长','RISK','USER','000073',9,NULL),(80,NULL,NULL,_binary '\0','000079','法治大队组员1','$2a$10$YnCom.BcGAFbt3cpNO0yruJL5aoRgNbnstuGm7rV025RRmF2D4Kwq','组员','NORMAL','USER','000078',9,NULL),(81,NULL,NULL,_binary '\0','000080','法治大队组员2','$2a$10$TDzlXIt6ViOzScb8GQr8EeF5JABYUWb.dZ.QS2ABIVNFkOzFekybi','组员','NORMAL','USER','000078',9,NULL),(82,NULL,NULL,_binary '\0','000081','法治大队组员3','$2a$10$o5NIVydNHPAwjJvFRi8zwu4tWJpOvfHDgjscTVq1tub.A956M4Tgu','组员','NORMAL','USER','000078',9,NULL),(83,NULL,NULL,_binary '\0','000082','督察审计大队队长','$2a$10$wn2S13lSU9yVO1hiU3O68uj6wZPkcAhQHAeP1mAR1L3EI6BeQn5jO','队长','NORMAL','USER','000001',10,NULL),(84,NULL,NULL,_binary '\0','000083','督察审计大队副队长1','$2a$10$w8P.l3oKy5xLdmfLS4xSLeK3V7BkMNwk9eHrwJZxdvJdWYlnFyYFq','副队长','NORMAL','USER','000082',10,NULL),(85,NULL,NULL,_binary '\0','000084','督察审计大队组长1','$2a$10$Z7cB7vhpmNY1yu18.HZIuOMgt0WCqW7yZQtADmirMTqTWRvt7jBq2','组长','NORMAL','USER','000083',10,NULL),(86,NULL,NULL,_binary '','000085','督察审计大队组员1','$2a$10$4OPgjNwV1SbqWiuQ1Es/4OuP9F0dFASVyp.VxB7y2E856KduZJLPi','组员','KEY','USER','000084',10,NULL),(87,NULL,NULL,_binary '\0','000086','督察审计大队组员2','$2a$10$hF9A3Gz9XCnIxmGAN2nSQO4SXdd5wsDbW8OBxmX5BUVZ.PTd3QkWm','组员','NORMAL','USER','000084',10,NULL),(88,NULL,NULL,_binary '\0','000087','督察审计大队组员3','$2a$10$BIJNziXcoDAT.mJ3AmQs1earoADTHek4NPPvb.LB2wTY0OMANCOrG','组员','NORMAL','USER','000084',10,NULL),(89,NULL,NULL,_binary '\0','000088','督察审计大队组长2','$2a$10$o4gE1ywEuarXh2gmu3ACfuvr7ptwLcSJMET54.VX.QQbK.AhOnoE.','组长','ATTENTION','USER','000083',10,NULL),(90,NULL,NULL,_binary '\0','000089','督察审计大队组员1','$2a$10$S.OfUJCBcJ1D7Ej4QBkUmuxmWqWR.aI.DRZymP5kGFbLZ5wXq8FAO','组员','NORMAL','USER','000088',10,NULL),(91,NULL,NULL,_binary '\0','000090','督察审计大队组员2','$2a$10$v3NO/ChUMcuJ1YJMB4bFHu0nHnDxYz88HJ4OogLm8d7EYUKeXMNXS','组员','NORMAL','USER','000088',10,NULL),(92,NULL,NULL,_binary '\0','000091','督察审计大队组员3','$2a$10$s41ecqRoN.aCZEGtkw6/o.IX/7TuvASF0UhWRQsjYE48z7Wtbpg5K','组员','RISK','USER','000088',10,NULL),(93,NULL,NULL,_binary '\0','000092','经济犯罪侦查大队队长','$2a$10$0GuQRioQSaBpZH1g2XxgT.AGGyo1RMIHnMpjsVxZhaAz6nXbIhA/.','队长','NORMAL','USER','000001',11,NULL),(94,NULL,NULL,_binary '\0','000093','经济犯罪侦查大队副队长1','$2a$10$i1o.Dxx/lw/xaC1HdHK.AeIEVyxWhaiGOavjfOBBI4cauYkL/sCqC','副队长','NORMAL','USER','000092',11,NULL),(95,NULL,NULL,_binary '\0','000094','经济犯罪侦查大队组长1','$2a$10$V.kauogyMJrW8GV69L.sduc8W9x/26.KRGqKH1fDGwCCBr44aWRme','组长','NORMAL','USER','000093',11,NULL),(96,NULL,NULL,_binary '\0','000095','经济犯罪侦查大队组员1','$2a$10$1G2durJU/aagSgXQW81AuOXBZYv2rz0IumVvUmTGQaLNaRyzD3/Xm','组员','NORMAL','USER','000094',11,NULL),(97,NULL,NULL,_binary '\0','000096','经济犯罪侦查大队组员2','$2a$10$OgoK3neSVVl6tGSRfbTjsegbTUxSKkkPFWPS68NEyaDcTKH18FsZq','组员','NORMAL','USER','000094',11,NULL),(98,NULL,NULL,_binary '\0','000097','经济犯罪侦查大队组员3','$2a$10$YdL74qEjhbxIJhpEVD5owOiFn5uWHgh7YQxYTQtV02Hm3T4o4gTJW','组员','NORMAL','USER','000094',11,NULL),(99,NULL,NULL,_binary '\0','000098','经济犯罪侦查大队组长2','$2a$10$4314z3Lvhi1Ag7w.mHB3EOV7BE.9EXQfQ8cqVdA4Rxdy1ckUduK3a','组长','NORMAL','USER','000093',11,NULL),(100,NULL,NULL,_binary '\0','000099','经济犯罪侦查大队组员1','$2a$10$v91xMHEkbik2C5fS.WF9jO9qC3PLKRfzhQFOh1mav51trfupl/i6q','组员','ATTENTION','USER','000098',11,NULL),(101,NULL,NULL,_binary '\0','000100','经济犯罪侦查大队组员2','$2a$10$2nbYmqDSOteJSeda2XOsKuZpCzBEYTxcPrXPgyNZlgKfnNOqRfYHW','组员','NORMAL','USER','000098',11,NULL),(102,NULL,NULL,_binary '\0','000101','经济犯罪侦查大队组员3','$2a$10$hcYYkMgjEjHqtKnFEvlkHOVLCFjYcoPLdBNxGjSf1Iz1o3McrR1R2','组员','NORMAL','USER','000098',11,NULL),(103,NULL,NULL,_binary '','000102','资源环境和食品药品犯罪侦查大队队长','$2a$10$7WlOFjD4V3/syJDbMRnQpeVv3sy/O1bIw4nGeUxfdiTl27x1ytx2O','队长','KEY','USER','000001',12,NULL),(104,NULL,NULL,_binary '\0','000103','资源环境和食品药品犯罪侦查大队副队长1','$2a$10$E1Q2izyplwNuuZOyygMweOGzIn5eiHjwxjUR3gNkVOcmICIV172L2','副队长','NORMAL','USER','000102',12,NULL),(105,NULL,NULL,_binary '\0','000104','资源环境和食品药品犯罪侦查大队组长1','$2a$10$CkVTfJ2kbGJsvoOofoF87uTXsxE9LZrVMV.aNPbcPQKtzcHhsyTKS','组长','RISK','USER','000103',12,NULL),(106,NULL,NULL,_binary '\0','000105','资源环境和食品药品犯罪侦查大队组员1','$2a$10$tyBJa4NnbuNMXo003/ASCuoAS.mMK7JQZ7r3jI9yHelgtM25eqNO2','组员','NORMAL','USER','000104',12,NULL),(107,NULL,NULL,_binary '\0','000106','资源环境和食品药品犯罪侦查大队组员2','$2a$10$5pC04iWOdwMXVszT9iiXhe.ImWE0wF9wygHK8jPIzUat.RfIzLivi','组员','NORMAL','USER','000104',12,NULL),(108,NULL,NULL,_binary '\0','000107','资源环境和食品药品犯罪侦查大队组员3','$2a$10$T8OwOSX0NSyCvNIO2QnO8uBrZZsr/Z0WRCFq7QIqH2hUVKtpFxktm','组员','NORMAL','USER','000104',12,NULL),(109,NULL,NULL,_binary '\0','000108','资源环境和食品药品犯罪侦查大队组长2','$2a$10$LCBtV.mnEkXiaK9I4fD5GeIEw.a9mEoyowS3XKKK7mVAOu5ibEr.G','组长','NORMAL','USER','000103',12,NULL),(110,NULL,NULL,_binary '\0','000109','资源环境和食品药品犯罪侦查大队组员1','$2a$10$IjbDY2Ds9sKrzQ1IUfMqxeGMOZTZ1S9ibAeXBlS7KOt3Nv8AWUoJ6','组员','NORMAL','USER','000108',12,NULL),(111,NULL,NULL,_binary '\0','000110','资源环境和食品药品犯罪侦查大队组员2','$2a$10$n7JEDufglskpedCQFupSz.HZWVqMqDEEzdl5Kl/TzA6T1999NaANm','组员','ATTENTION','USER','000108',12,NULL),(112,NULL,NULL,_binary '\0','000111','资源环境和食品药品犯罪侦查大队组员3','$2a$10$5qA33UVb7dz/lrFhLCMmIugfOHHtmJ8WQIpnU2I9ow2ve3zKUEXxq','组员','NORMAL','USER','000108',12,NULL),(113,NULL,NULL,_binary '\0','000112','巡特警大队队长','$2a$10$1kTc8CM0yvx5MmSLE3pzzOOs6iW/AFMNLM7ASnG.KgxkUSOEntkr2','队长','NORMAL','USER','000001',13,NULL),(114,NULL,NULL,_binary '\0','000113','巡特警大队副队长1','$2a$10$EvIc9dsiEnYndN2yNqirguiSLyjqPnqnZ2cLsJw3S0OMHVNKK0442','副队长','NORMAL','USER','000112',13,NULL),(115,NULL,NULL,_binary '\0','000114','巡特警大队组长1','$2a$10$i2/Nh29FWFTbK9uNVffVXuFS3U2ARMhySbT9Cl3bTfePKgwMSR2Tu','组长','NORMAL','USER','000113',13,NULL),(116,NULL,NULL,_binary '\0','000115','巡特警大队组员1','$2a$10$2/aN8YDAhdKfjN2zSWxgC.4.ut3oLxhpp/sHmHHQ.rbLpWUf8yLte','组员','NORMAL','USER','000114',13,NULL),(117,NULL,NULL,_binary '\0','000116','巡特警大队组员2','$2a$10$QBsDt6sgImQv1K41glfGPuqtUe9SU4s6O99XO87TEwIZoe8/heGBu','组员','NORMAL','USER','000114',13,NULL),(118,NULL,NULL,_binary '\0','000117','巡特警大队组员3','$2a$10$v7LYRcdHwBYrfSH9NNvmQOFK8eIhdLgtaOr4yb98LvyDZ7ne5o.Cy','组员','RISK','USER','000114',13,NULL),(119,NULL,NULL,_binary '\0','000118','巡特警大队组长2','$2a$10$aHWEWunWmDbsrbKZRGCnXORfJPax8BSbLM5CViizpEp6arIRvcQiu','组长','NORMAL','USER','000113',13,NULL),(120,NULL,NULL,_binary '','000119','巡特警大队组员1','$2a$10$Pxz7D1j26L5hgeURw.J7HO8wcY31wDnZnF0DLn2qlSFhBqtJnqbnK','组员','KEY','USER','000118',13,NULL),(121,NULL,NULL,_binary '\0','000120','巡特警大队组员2','$2a$10$lzreE6ZpCoNMCsl2HMP.t.KyBDBXeSOa/G8skFS1WOEJYg37vzS96','组员','NORMAL','USER','000118',13,NULL),(122,NULL,NULL,_binary '\0','000121','巡特警大队组员3','$2a$10$8KkgeJYA9n6PqHeTpJDv9uTbRSEJnqWBiUNGeXWtrfFLEKbicLuYS','组员','ATTENTION','USER','000118',13,NULL),(123,NULL,NULL,_binary '\0','000122','县看守所负责人','$2a$10$7WRETz/4Xdd9RpmpH3FkGeXtQs6B5elREHyWd.5yj2WLd45xWX8n.','负责人','NORMAL','USER','000001',14,NULL),(124,NULL,NULL,_binary '\0','000123','县看守所副职1','$2a$10$s4jV79rp7J9409teuyYYzOs/iAvCeICEg.Gl86.LYQF3VnlkYYjuG','副职','NORMAL','USER','000122',14,NULL),(125,NULL,NULL,_binary '\0','000124','县看守所组长1','$2a$10$Up1WdD7TeTn2gSG7.VAxyeMY4IIhhWJcj1Onlc9Cc8Q8GgbrEtUEG','组长','NORMAL','USER','000123',14,NULL),(126,NULL,NULL,_binary '\0','000125','县看守所组员1','$2a$10$HRUTUkiqeKimGkA2fr3Ui.eRb1tlrSQX5ZbmfrkHVUd2YgvGE0hQi','组员','NORMAL','USER','000124',14,NULL),(127,NULL,NULL,_binary '\0','000126','县看守所组员2','$2a$10$Te/PwoEcdq1.F5.a62fnAujSfjAu8vf/QuDWm.4goW3pXR/Oe/XIG','组员','NORMAL','USER','000124',14,NULL),(128,NULL,NULL,_binary '\0','000127','县看守所组员3','$2a$10$Y8yHfOPwIKZH2r9nVsqOx.6J5vjki7WxLZ.Ner93ErYASVqxcRz0q','组员','NORMAL','USER','000124',14,NULL),(129,NULL,NULL,_binary '\0','000128','县看守所组长2','$2a$10$juEuHtthvJnh3mgLNjU9ReE/ZlPwSu5T1NFCoCs8stFLwYuh5XHva','组长','NORMAL','USER','000123',14,NULL),(130,NULL,NULL,_binary '\0','000129','县看守所组员1','$2a$10$6dEI0ac/cvZ/vBCchmYm7OqKPYVnsPx2On5w4BgVOGf3H4t3YyIH.','组员','NORMAL','USER','000128',14,NULL),(131,NULL,NULL,_binary '\0','000130','县看守所组员2','$2a$10$a401wyPQT5h37qBycnSVXOLEy1CGzOBbuO5YbgHmiOL3eHNV9MwXK','组员','RISK','USER','000128',14,NULL),(132,NULL,NULL,_binary '\0','000131','县看守所组员3','$2a$10$d40XgZUFwUj5o7k7Y9C/FeQdGYcAWWrBX6x7x6F0vr4bYvqgkd1nG','组员','NORMAL','USER','000128',14,NULL),(133,NULL,NULL,_binary '\0','000132','城北派出所所长','$2a$10$SUMfRsqYTD9muoOXXUVbDuZQN13Vu5ADDuPmBsO7qsYaAN07DmZua','所长','ATTENTION','USER','000001',15,NULL),(134,NULL,NULL,_binary '\0','000133','城北派出所副所长1','$2a$10$GI56cNDjZjGOqwWfZmrVxuCHkssFnixx5Iw3aSBQiKc2lR1jYEmY6','副所长','NORMAL','USER','000132',15,NULL),(135,NULL,NULL,_binary '\0','000134','城北派出所组长1','$2a$10$jj9AepU7hmPlzanc7IGxhO/NzvbfWd3BSTDd4X7HWUfpP7lMjHBH6','组长','NORMAL','USER','000133',15,NULL),(136,NULL,NULL,_binary '\0','000135','城北派出所组员1','$2a$10$QGnalVLkhHlITKbxjthkvuypbfQaNUMrGtKLQZx8enqP52ozKgyq6','组员','NORMAL','USER','000134',15,NULL),(137,NULL,NULL,_binary '','000136','城北派出所组员2','$2a$10$lWZtfZBVH84WUKr9KJx8a.7A0EzeAF.GXI9KMqurj2OT4DiKkQKVm','组员','KEY','USER','000134',15,NULL),(138,NULL,NULL,_binary '\0','000137','城北派出所组员3','$2a$10$yAQsXWFH4DB.lccpSrF37.AnOTCX43kqxaX6Jjm1Utc20uQIzdXKC','组员','NORMAL','USER','000134',15,NULL),(139,NULL,NULL,_binary '\0','000138','城北派出所组长2','$2a$10$8DzLP8OfrltE5XTSa1nKweuRBmYBrqH0RBQs.b8.Bm00PUgNYRrRy','组长','NORMAL','USER','000133',15,NULL),(140,NULL,NULL,_binary '\0','000139','城北派出所组员1','$2a$10$ccbzr9u6AQYnI1Tkwz4i1eLc5tOWJ7Rt5fEg5657qDWTRDAUe6EEO','组员','NORMAL','USER','000138',15,NULL),(141,NULL,NULL,_binary '\0','000140','城北派出所组员2','$2a$10$MjpzYBeTXkpr6kdoInKB.OH.tqkSe1dEdQ8mU/pHIfkShiAz3QQVa','组员','NORMAL','USER','000138',15,NULL),(142,NULL,NULL,_binary '\0','000141','城北派出所组员3','$2a$10$zjEdV8/gFa46qhE86amW/eZqBkB5JrBsyWCbJs6nribKk9L0fekwK','组员','NORMAL','USER','000138',15,NULL),(143,NULL,NULL,_binary '\0','000142','城南派出所所长','$2a$10$EmrCefEJ2Cf.wO2QSTwCXednDK05sln4UF4WqYf8uKAa184WUs/2O','所长','NORMAL','USER','000001',16,NULL),(144,NULL,NULL,_binary '\0','000143','城南派出所副所长1','$2a$10$N.iIbzxiRceVa61/Saffp.7FT02rOeyfKbtAFZMyC2mHiE7/QiXhi','副所长','RISK','USER','000142',16,NULL),(145,NULL,NULL,_binary '\0','000144','城南派出所组长1','$2a$10$B9wTZ63HPp4vpobXvI2YQe1c8EUbPK50wX9toHBVf5IkRsYaatKMG','组长','NORMAL','USER','000143',16,NULL),(146,NULL,NULL,_binary '\0','000145','城南派出所组员1','$2a$10$tBLIbp6ydbvQE/n8.g2ti.zQn3kJ9b9XeUsuaz7SqrpveYBFBe/Ke','组员','NORMAL','USER','000144',16,NULL),(147,NULL,NULL,_binary '\0','000146','城南派出所组员2','$2a$10$4NPxKwwG8.xjij.QjrYBQuStVzm8Q0Ev/mbw1wjwGexVK19FwxYIC','组员','NORMAL','USER','000144',16,NULL),(148,NULL,NULL,_binary '\0','000147','城南派出所组员3','$2a$10$16sni9j0Chn1FPcPqsFJgeUPLNBz5qBOeATzpMTbJw82NZRjVwOYe','组员','NORMAL','USER','000144',16,NULL),(149,NULL,NULL,_binary '\0','000148','城南派出所组长2','$2a$10$dE0e3Xu1Rr5hLS5FVxYh4O8d9CzLLkZ56ONu6lUmakVvArTZpMCS.','组长','NORMAL','USER','000143',16,NULL),(150,NULL,NULL,_binary '\0','000149','城南派出所组员1','$2a$10$pVMWhvsj8P1sGveQ/0tS.es3ZYJjypxwcVZC6ZYxMTpKXuG6w9WfW','组员','NORMAL','USER','000148',16,NULL),(151,NULL,NULL,_binary '\0','000150','城南派出所组员2','$2a$10$N5gt.jI4YawHwecAdVCAqeffh5vIpr9OK8XJbvGqh7fQ5MQBgdsGO','组员','NORMAL','USER','000148',16,NULL),(152,NULL,NULL,_binary '\0','000151','城南派出所组员3','$2a$10$FKLngIaSquoy.SYBJTTA3OVcEamgdhilmwsqxwXJyQN9KPzHC4mB6','组员','NORMAL','USER','000148',16,NULL),(153,NULL,NULL,_binary '\0','000152','梅源派出所所长','$2a$10$.il2IJpSDNtunExx43MYAOL5617IYTcrUmo/6T.2DTrJzZ.QSu0Mi','所长','NORMAL','USER','000001',17,NULL),(154,NULL,NULL,_binary '','000153','梅源派出所副所长1','$2a$10$B1yWQ/tkwvktOP.hX/SUX.wdiDIhASvmtS94oTXv6J1k3V1d5XfHm','副所长','KEY','USER','000152',17,NULL),(155,NULL,NULL,_binary '\0','000154','梅源派出所组长1','$2a$10$cZ494WTW1tr6lRCbjX0i1O15rws3DwSi2LsqwV1H5XOLbL4EweNWi','组长','ATTENTION','USER','000153',17,NULL),(156,NULL,NULL,_binary '\0','000155','梅源派出所组员1','$2a$10$3KOkd45NR4MUJsbBhzwnQO/Yye2i4LagacZJQy/qKfLn8LiEZd0sy','组员','NORMAL','USER','000154',17,NULL),(157,NULL,NULL,_binary '\0','000156','梅源派出所组员2','$2a$10$RqUuQzq.99WbG.qvY0ygOeqCBhpEDO.By.8ewLAUG3Ei834/BhRrS','组员','RISK','USER','000154',17,NULL),(158,NULL,NULL,_binary '\0','000157','梅源派出所组员3','$2a$10$2fEYSmnjxiyCb4/2mlJF5OneHFBoF7iK4iCASbELJhnZw4D1l8MMe','组员','NORMAL','USER','000154',17,NULL),(159,NULL,NULL,_binary '\0','000158','梅源派出所组长2','$2a$10$1pGONAIpaTM3zF5mV3qhO.P9y46/gJUL7fYwBvCWI9S9rsOx5W.5i','组长','NORMAL','USER','000153',17,NULL),(160,NULL,NULL,_binary '\0','000159','梅源派出所组员1','$2a$10$RlRw1T/1NQ4Lhl/Tpon0f.bvbMnhk9pDDWH3zjJaqhJK2v4vhiVtO','组员','NORMAL','USER','000158',17,NULL),(161,NULL,NULL,_binary '\0','000160','梅源派出所组员2','$2a$10$/jQhr22mqn3g7wF6FJfjjOkZZzxLSxwF1aGRSecsAVwEibjd.fNIe','组员','NORMAL','USER','000158',17,NULL),(162,NULL,NULL,_binary '\0','000161','梅源派出所组员3','$2a$10$70y225mS5Oe9guk0b0dn5uAm/eQulCvY4VLLG3WBRYALBrYMneXD2','组员','NORMAL','USER','000158',17,NULL),(163,NULL,NULL,_binary '\0','000162','石塘派出所所长','$2a$10$qlUzhp/3IPwMQ0HmvvyEqOEeCEmrE4rNT/l3M3kkQKcR52w9iQJF6','所长','NORMAL','USER','000001',18,NULL),(164,NULL,NULL,_binary '\0','000163','石塘派出所副所长1','$2a$10$Wh57pR5gDMdwSJhkECrFFOAFhaP9eyoLIAwmP4atjxSKVEjCrLN4q','副所长','NORMAL','USER','000162',18,NULL),(165,NULL,NULL,_binary '\0','000164','石塘派出所组长1','$2a$10$p8HLWJ0nnNAoAFRxDyvw0uhE9iojT1vUFeGeIC4QC9H1pBWsaxARm','组长','NORMAL','USER','000163',18,NULL),(166,NULL,NULL,_binary '\0','000165','石塘派出所组员1','$2a$10$M7qZ7nVzBZZPvpN/NcNqReSm/D9xcLY/UUXD3SCA.Cc89WXm64Jbi','组员','ATTENTION','USER','000164',18,NULL),(167,NULL,NULL,_binary '\0','000166','石塘派出所组员2','$2a$10$pQk83BdyzKDxsxAkNItTeOQ7SbIkQl41OQnXbDWXlN0Jp58TzQHIi','组员','NORMAL','USER','000164',18,NULL),(168,NULL,NULL,_binary '\0','000167','石塘派出所组员3','$2a$10$WphoDBOdZeqA2nHAAmIRT.q5GvbStyNFImTsNcOj3IULkgjOsRxwy','组员','NORMAL','USER','000164',18,NULL),(169,NULL,NULL,_binary '\0','000168','石塘派出所组长2','$2a$10$sEvsgk507TnD7x67wNJLWO38rrvfSCzvLb.jevJypc3ofv6o2sTqW','组长','NORMAL','USER','000163',18,NULL),(170,NULL,NULL,_binary '\0','000169','石塘派出所组员1','$2a$10$yDMi8gVRFti4.WhteH99L./uCbyORSErSk0azzU4UPstOL1pD/wqO','组员','RISK','USER','000168',18,NULL),(171,NULL,NULL,_binary '','000170','石塘派出所组员2','$2a$10$I/Z6Ehtlcr.xrYA0T8vRQe6eA0KAjdKrl6GNPzdeaf7LqLbIU304y','组员','KEY','USER','000168',18,NULL),(172,NULL,NULL,_binary '\0','000171','石塘派出所组员3','$2a$10$hHLapH3gKQqtWl8OT2PnCe8hrb28ssSf2J8Cv6XLwpoJWyCcP3cQW','组员','NORMAL','USER','000168',18,NULL),(173,NULL,NULL,_binary '\0','000172','紧水滩派出所所长','$2a$10$eel7860K6JX1QzvM17E/zOFXAj.DcI3CPY11uTAQZGCNQnxRq9FKK','所长','NORMAL','USER','000001',19,NULL),(174,NULL,NULL,_binary '\0','000173','紧水滩派出所副所长1','$2a$10$eeshiBOHdCFrYKI9dFyoAuSR3TKIb0iYYbaH/SVcAJwEOENRis/Ya','副所长','NORMAL','USER','000172',19,NULL),(175,NULL,NULL,_binary '\0','000174','紧水滩派出所组长1','$2a$10$0CcaqKdZCS1lXaeZbepcjeHXEptwEm8QW5R1vRZJKrEjDhqn77Ol.','组长','NORMAL','USER','000173',19,NULL),(176,NULL,NULL,_binary '\0','000175','紧水滩派出所组员1','$2a$10$58/ixZNH8CnfUr8uf5Z/5ulMZlL2Aup9Cu2EjKS7UCpXJGmVkE6sq','组员','NORMAL','USER','000174',19,NULL),(177,NULL,NULL,_binary '\0','000176','紧水滩派出所组员2','$2a$10$qb4W4OIXigceho/u6vH3E.wuGe8BY.jU4J/3mNQqPQovpDa6VgG16','组员','ATTENTION','USER','000174',19,NULL),(178,NULL,NULL,_binary '\0','000177','紧水滩派出所组员3','$2a$10$hAKM.jzzrYL4HxBm0G1FdOs8mmrLRmrRFFGVytVIcP2R4/xq2DdN6','组员','NORMAL','USER','000174',19,NULL),(179,NULL,NULL,_binary '\0','000178','紧水滩派出所组长2','$2a$10$xYVORSD3WWSVssTMXXLgQeJi0lZuwccA.BenNrFm3tZdSbT1a36MK','组长','NORMAL','USER','000173',19,NULL),(180,NULL,NULL,_binary '\0','000179','紧水滩派出所组员1','$2a$10$YuidUDTVzosSfp2RYHQhpuFLrQnzzd8ucB5Uzwpr88YlnxvVMal5C','组员','NORMAL','USER','000178',19,NULL),(181,NULL,NULL,_binary '\0','000180','紧水滩派出所组员2','$2a$10$Ciriv6.Fe4EmHHZGOwpnLObd/DJZgY34zcdmCCMw.gSJVpNFK6BxS','组员','NORMAL','USER','000178',19,NULL),(182,NULL,NULL,_binary '\0','000181','紧水滩派出所组员3','$2a$10$ovFllKHdVIPBrPrF/H5wM..tEixrDJSCewwyT5t0WT8JrouGcpEWO','组员','NORMAL','USER','000178',19,NULL),(183,NULL,NULL,_binary '\0','000182','云和湖派出所所长','$2a$10$nJZ/ib26344OGRX8VgcrSesgLsrvZGXmiZre7S4EpYarndD7qJx/q','所长','RISK','USER','000001',20,NULL),(184,NULL,NULL,_binary '\0','000183','云和湖派出所副所长1','$2a$10$SwEmozRbE0sDvQ4rcUNslO1AaxSl84T/uF.h6GANLeQ4eI8LbW3a.','副所长','NORMAL','USER','000182',20,NULL),(185,NULL,NULL,_binary '\0','000184','云和湖派出所组长1','$2a$10$CLh.2bwYjyX6ow7XDYzoW.T0WsFg2G8yFNhBeLHCu5UwEfJjbvzPW','组长','NORMAL','USER','000183',20,NULL),(186,NULL,NULL,_binary '\0','000185','云和湖派出所组员1','$2a$10$07QinLaI.dl.u6qg1OvSb.yyrs2xz9./gEGEVQaHy2F4m8hl0MN1O','组员','NORMAL','USER','000184',20,NULL),(187,NULL,NULL,_binary '\0','000186','云和湖派出所组员2','$2a$10$iUnAj7oWs7FzMV9tRATNM.LIXulVzw/q6EhFmgiYltshaDM.o90qi','组员','NORMAL','USER','000184',20,NULL),(188,NULL,NULL,_binary '','000187','云和湖派出所组员3','$2a$10$GEd8BZGYWcFSUvz/KX3xeOlDR6FPY5PM8bRgTHAcVD8R/.HMaUWvy','组员','KEY','USER','000184',20,NULL),(189,NULL,NULL,_binary '\0','000188','云和湖派出所组长2','$2a$10$RY9IOREFPC.dHUWkePTxXuk6oeVn.OyBjYtyHe..JUhcRsqhAgyTG','组长','NORMAL','USER','000183',20,NULL),(190,NULL,NULL,_binary '\0','000189','云和湖派出所组员1','$2a$10$XrwxvrU1eyBCRmzBtVMMDe2drGcbuQb0zpA3qbyGLoOtptbg1No8q','组员','NORMAL','USER','000188',20,NULL),(191,NULL,NULL,_binary '\0','000190','云和湖派出所组员2','$2a$10$dmjjSlJ15zLQT0zPu6qT9.QsbYwZqhThFZHkBxzTWI5ZO4URS8eIa','组员','NORMAL','USER','000188',20,NULL),(192,NULL,NULL,_binary '\0','000191','云和湖派出所组员3','$2a$10$5v.eGTnSe9FBQEseRH9VxerQuNSnX3/aUODWR6vWl00Dp81mgGjUa','组员','NORMAL','USER','000188',20,NULL);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `violation_records`
--

DROP TABLE IF EXISTS `violation_records`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `violation_records` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_by` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `punishment` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `reason` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `target_job_no` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `violation_time` datetime(6) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `violation_records`
--

LOCK TABLES `violation_records` WRITE;
/*!40000 ALTER TABLE `violation_records` DISABLE KEYS */;
/*!40000 ALTER TABLE `violation_records` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-17 20:54:38
