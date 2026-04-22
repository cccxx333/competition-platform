-- MySQL dump 10.13  Distrib 8.0.37, for Win64 (x86_64)
--
-- Host: localhost    Database: competition_platform
-- ------------------------------------------------------
-- Server version	8.0.37

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
-- Table structure for table `applications`
--

DROP TABLE IF EXISTS `applications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `applications` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `competition_id` bigint NOT NULL,
  `team_id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'PENDING',
  `is_active` bit(1) NOT NULL DEFAULT b'1',
  `applied_at` datetime DEFAULT NULL,
  `reviewed_at` datetime DEFAULT NULL,
  `reviewed_by` bigint DEFAULT NULL,
  `removed_at` datetime DEFAULT NULL,
  `removed_by` bigint DEFAULT NULL,
  `reason` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_applications_student_competition_active` (`student_id`,`competition_id`,`is_active`),
  KEY `fk_applications_competition` (`competition_id`),
  KEY `fk_applications_reviewer` (`reviewed_by`),
  KEY `fk_applications_remover` (`removed_by`),
  KEY `idx_applications_team_status` (`team_id`,`status`),
  CONSTRAINT `fk_applications_competition` FOREIGN KEY (`competition_id`) REFERENCES `competitions` (`id`),
  CONSTRAINT `fk_applications_remover` FOREIGN KEY (`removed_by`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_applications_reviewer` FOREIGN KEY (`reviewed_by`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_applications_student` FOREIGN KEY (`student_id`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_applications_team` FOREIGN KEY (`team_id`) REFERENCES `teams` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `applications`
--

LOCK TABLES `applications` WRITE;
/*!40000 ALTER TABLE `applications` DISABLE KEYS */;
INSERT INTO `applications` VALUES (1,9,2,1,'PENDING',_binary '','2026-01-15 15:46:40',NULL,NULL,NULL,NULL,NULL),(2,8,3,1,'REJECTED',_binary '\0','2026-01-15 18:23:33','2026-01-15 19:13:54',2,NULL,NULL,NULL),(3,7,4,1,'APPROVED',_binary '','2026-01-15 19:41:34','2026-01-15 19:43:26',2,NULL,NULL,'ok'),(4,10,5,1,'REJECTED',_binary '\0','2026-01-15 22:04:01','2026-01-20 15:07:08',2,NULL,NULL,'你不行'),(6,9,6,5,'REMOVED',_binary '\0','2026-01-16 14:06:33','2026-01-16 14:06:47',2,'2026-01-18 10:58:25',4,NULL),(7,7,4,5,'APPROVED',_binary '','2026-01-16 14:47:30','2026-01-16 14:48:32',2,NULL,NULL,'符合队伍要求'),(8,3,7,5,'APPROVED',_binary '','2026-01-18 11:18:00','2026-01-18 11:19:22',2,NULL,NULL,'符合队伍要求'),(9,12,11,5,'APPROVED',_binary '','2026-01-20 16:00:50','2026-01-20 16:01:00',2,NULL,NULL,'可以'),(10,16,15,5,'REJECTED',_binary '\0','2026-01-22 15:20:11','2026-01-22 15:24:21',2,NULL,NULL,'比赛关闭，不能审批通过'),(11,18,16,5,'REJECTED',_binary '\0','2026-01-22 15:47:06','2026-01-22 15:48:04',2,NULL,NULL,'1'),(12,19,17,5,'APPROVED',_binary '','2026-01-22 15:51:54','2026-01-23 14:04:19',2,NULL,NULL,'欢迎加入'),(13,20,18,5,'APPROVED',_binary '','2026-01-25 23:39:10','2026-01-25 23:39:40',2,NULL,NULL,NULL),(14,21,19,5,'APPROVED',_binary '','2026-01-25 23:39:23','2026-01-25 23:39:42',2,NULL,NULL,NULL),(15,22,20,5,'PENDING',_binary '','2026-01-26 12:07:43',NULL,NULL,NULL,NULL,NULL),(16,15,12,5,'APPROVED',_binary '','2026-02-26 22:29:31','2026-04-14 09:27:32',2,NULL,NULL,NULL),(17,14,13,5,'APPROVED',_binary '','2026-02-26 22:52:01','2026-04-14 09:27:29',2,NULL,NULL,NULL),(18,23,21,1,'PENDING',_binary '','2026-04-14 09:26:57',NULL,NULL,NULL,NULL,NULL),(19,23,21,17,'APPROVED',_binary '','2026-04-15 13:20:27','2026-04-15 13:20:27',2,NULL,NULL,'通过'),(20,23,21,18,'APPROVED',_binary '','2026-04-15 13:44:04','2026-04-15 13:44:05',2,NULL,NULL,'通过'),(21,15,12,1,'PENDING',_binary '','2026-04-22 10:29:47',NULL,NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `applications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `award_recipients`
--

DROP TABLE IF EXISTS `award_recipients`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `award_recipients` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `team_award_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `recorded_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_award_recipients_award_user` (`team_award_id`,`user_id`),
  KEY `idx_award_recipients_user` (`user_id`),
  CONSTRAINT `fk_award_recipients_team_award` FOREIGN KEY (`team_award_id`) REFERENCES `team_awards` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_award_recipients_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `award_recipients`
--

LOCK TABLES `award_recipients` WRITE;
/*!40000 ALTER TABLE `award_recipients` DISABLE KEYS */;
INSERT INTO `award_recipients` VALUES (1,1,5,'2026-01-18 10:06:56'),(2,2,5,'2026-01-21 13:12:22');
/*!40000 ALTER TABLE `award_recipients` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `competition_skills`
--

DROP TABLE IF EXISTS `competition_skills`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `competition_skills` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `competition_id` bigint NOT NULL,
  `skill_id` bigint NOT NULL,
  `importance` int DEFAULT '1',
  PRIMARY KEY (`id`),
  KEY `fk_competition_skills_competition` (`competition_id`),
  KEY `fk_competition_skills_skill` (`skill_id`),
  CONSTRAINT `fk_competition_skills_competition` FOREIGN KEY (`competition_id`) REFERENCES `competitions` (`id`),
  CONSTRAINT `fk_competition_skills_skill` FOREIGN KEY (`skill_id`) REFERENCES `skills` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=63 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `competition_skills`
--

LOCK TABLES `competition_skills` WRITE;
/*!40000 ALTER TABLE `competition_skills` DISABLE KEYS */;
INSERT INTO `competition_skills` VALUES (21,1,1,3),(22,1,2,2),(23,1,3,2),(24,1,4,1),(25,2,1,2),(26,2,3,3),(27,2,4,2),(28,2,2,1),(29,3,2,3),(30,3,3,2),(31,3,4,2),(32,3,1,1),(33,4,1,2),(34,4,2,2),(35,4,3,1),(36,4,4,1),(37,5,2,2),(38,5,3,2),(39,5,5,2),(40,5,4,1),(41,6,5,3),(42,6,6,2),(43,6,7,2),(44,6,8,1),(45,7,5,2),(46,7,6,3),(47,7,8,2),(48,7,7,1),(49,8,6,2),(50,8,7,3),(51,8,8,2),(52,8,5,1),(53,9,7,2),(54,9,8,2),(55,9,1,2),(56,9,6,1),(57,10,5,2),(58,10,6,2),(59,10,7,1),(60,10,8,1),(61,24,7,5),(62,24,16,3);
/*!40000 ALTER TABLE `competition_skills` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `competitions`
--

DROP TABLE IF EXISTS `competitions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `competitions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(128) NOT NULL,
  `description` text,
  `organizer` varchar(128) DEFAULT NULL,
  `start_date` date DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `registration_deadline` date DEFAULT NULL,
  `min_team_size` int DEFAULT '1',
  `max_team_size` int NOT NULL,
  `category` varchar(64) DEFAULT NULL,
  `level` varchar(64) DEFAULT NULL,
  `status` varchar(20) DEFAULT 'UPCOMING',
  `created_by` bigint DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_competitions_created_by` (`created_by`),
  KEY `idx_competitions_status_deadline` (`status`,`registration_deadline`),
  CONSTRAINT `fk_competitions_created_by` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `competitions`
--

LOCK TABLES `competitions` WRITE;
/*!40000 ALTER TABLE `competitions` DISABLE KEYS */;
INSERT INTO `competitions` VALUES (1,'ACM国际大学生程序设计竞赛','全球最具影响力的大学生程序设计竞赛\n参赛要求：\n1. 团队协作能力强\n2. 具备相关技术基础\n3. 有创新思维和解决问题的能力\n4. 能够承受高强度的比赛压力\n\n奖项设置：\n- 一等奖：奖金10000元 + 证书\n- 二等奖：奖金5000元 + 证书\n- 三等奖：奖金2000元 + 证书\n- 优秀奖：证书\n\n联系方式：competition@example.com','ACM','2024-05-01','2024-05-03','2024-04-15',1,3,'编程','国际级','FINISHED',NULL,NULL,'2026-01-18 09:57:13'),(2,'全国大学生数学建模竞赛','培养学生创新意识及运用数学方法解决实际问题的能力','教育部','2024-09-08','2024-09-11','2024-08-20',1,3,'数学建模','国家级','FINISHED',NULL,NULL,'2026-01-18 09:57:13'),(3,'中国互联网+大学生创新创业大赛','激发大学生创新创业热情，培养创新创业人才','教育部','2026-01-30','2026-03-10','2026-01-25',1,5,'创新创业','国家级','FINISHED',NULL,NULL,'2026-04-13 20:19:46'),(4,'蓝桥杯全国软件和信息技术专业人才大赛','促进软件开发技术的发展，培养软件开发人才\n参赛要求：\n1. 团队协作能力强\n2. 具备相关技术基础\n3. 有创新思维和解决问题的能力\n4. 能够承受高强度的比赛压力\n\n奖项设置：\n- 一等奖：奖金10000元 + 证书\n- 二等奖：奖金5000元 + 证书\n- 三等奖：奖金2000元 + 证书\n- 优秀奖：证书\n\n联系方式：competition@example.com','工信部','2024-04-13','2024-04-13','2024-03-30',1,1,'编程','国家级','FINISHED',NULL,NULL,'2026-01-18 09:57:13'),(5,'全国大学生电子设计竞赛','促进电子信息类专业和课程的建设','教育部','2024-08-07','2024-08-10','2024-07-20',1,3,'电子设计','国家级','FINISHED',NULL,NULL,'2026-01-18 09:57:13'),(6,'测试竞赛A','用于M2接口验证','教务处','2026-01-01','2026-02-01','2025-12-25',2,5,'综合类','校级','FINISHED',NULL,'2026-01-14 20:24:47','2026-02-26 21:00:48'),(7,'测试竞赛A','用于M2验证','教务处','2026-01-01','2026-02-01','2026-02-14',2,5,'综合类','校级','FINISHED',NULL,'2026-01-14 20:28:57','2026-02-26 21:00:48'),(8,'校级算法竞赛-更新','面向大二','计算机学院','2026-03-01','2026-03-20','2026-02-20',1,3,'算法','校级','FINISHED',1,'2026-01-14 20:55:51','2026-04-13 20:19:46'),(9,'M4 Test Competition','competition for M4 application test','Test Organizer','2026-01-01','2026-01-10','2025-12-25',1,5,'TEST','SCHOOL','FINISHED',NULL,'2026-01-15 15:25:22','2026-01-18 10:01:17'),(10,'2026 校级算法竞赛','面向全校本科生的基础算法与数据结构竞赛','计算机学院','2026-11-01','2026-11-15','2026-12-31',1,3,'算法','校级','UPCOMING',NULL,'2026-01-15 22:00:35','2026-01-15 22:00:35'),(11,'前端分页测试','1','2','2026-11-01','2026-11-15','2026-12-31',1,3,'算法','校级','UPCOMING',NULL,'2026-01-19 21:21:20','2026-01-19 21:21:20'),(12,'前端分页测试2','1','2','2026-11-01','2026-11-15','2026-12-31',1,3,'算法','校级','UPCOMING',NULL,'2026-01-19 23:07:50','2026-01-19 23:07:50'),(13,'前端测试3','1','2','2026-11-01','2026-11-15','2026-12-31',1,3,'算法','校级','UPCOMING',NULL,'2026-01-20 12:25:18','2026-01-20 12:25:18'),(14,'后端教师申请技能测试','1','2','2026-11-01','2026-12-15','2026-10-31',1,3,'算法','校级','UPCOMING',NULL,'2026-01-21 19:48:21','2026-01-22 15:19:06'),(15,'后端教师申请技能测试2','1','2','2026-11-01','2026-12-15','2026-10-31',1,3,'算法','校级','UPCOMING',NULL,'2026-01-21 20:03:36','2026-01-21 20:03:36'),(16,'后端教师申请技能测试3','1','2','2026-11-01','2026-12-15','2026-12-09',1,3,'算法','校级','FINISHED',NULL,'2026-01-21 23:09:00','2026-01-22 15:21:03'),(17,'后端测试5',NULL,'教育部','2026-03-06','2026-07-17','2025-12-11',1,2,NULL,'校级','FINISHED',NULL,'2026-01-22 15:42:46','2026-01-22 15:43:44'),(18,'后端测试5',NULL,NULL,'2026-03-21','2026-05-16','2026-02-20',1,1,NULL,NULL,'FINISHED',NULL,'2026-01-22 15:44:10','2026-01-22 15:47:17'),(19,'1',NULL,NULL,'2027-01-15','2028-01-01','2026-02-21',1,1,NULL,NULL,'FINISHED',NULL,'2026-01-22 15:49:33','2026-01-24 01:05:56'),(20,'页面优化测试1',NULL,'1','2026-02-28','2026-03-14','2026-01-31',1,3,NULL,'1','FINISHED',NULL,'2026-01-25 23:37:39','2026-04-13 20:19:46'),(21,'页面优化测试2',NULL,'1','2026-02-21','2026-03-07','2026-01-31',1,3,NULL,'2','FINISHED',NULL,'2026-01-25 23:38:08','2026-04-13 20:19:46'),(22,'教师页面测试1',NULL,'1','2026-01-31','2026-02-21','2026-01-29',1,1,NULL,'1','FINISHED',NULL,'2026-01-26 12:07:00','2026-02-26 21:00:48'),(23,'全国大学生数学建模竞赛',NULL,'信工部','2026-04-18','2026-04-30','2026-04-16',3,5,NULL,'校级','ONGOING',NULL,'2026-04-13 22:41:27','2026-04-20 14:45:35'),(24,'2',NULL,NULL,'2026-05-01','2026-05-30','2026-04-30',1,3,NULL,NULL,'UPCOMING',NULL,'2026-04-21 20:06:43','2026-04-21 20:06:43');
/*!40000 ALTER TABLE `competitions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `skills`
--

DROP TABLE IF EXISTS `skills`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `skills` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `category` varchar(64) DEFAULT NULL,
  `description` text,
  `is_active` bit(1) DEFAULT b'1',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_skills_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `skills`
--

LOCK TABLES `skills` WRITE;
/*!40000 ALTER TABLE `skills` DISABLE KEYS */;
INSERT INTO `skills` VALUES (1,'Java','编程语言','Java编程语言',_binary '',NULL,NULL),(2,'Python','编程语言','Python编程语言',_binary '',NULL,NULL),(3,'JavaScript','编程语言','JavaScript编程语言',_binary '',NULL,NULL),(4,'C++','编程语言','C++编程语言',_binary '',NULL,NULL),(5,'算法设计','算法','数据结构与算法设计',_binary '',NULL,NULL),(6,'数据库设计','数据','关系型数据库设计与优化',_binary '',NULL,NULL),(7,'前端开发','Web开发','HTML/CSS/JavaScript前端开发',_binary '',NULL,NULL),(8,'后端开发','Web开发','服务器端应用开发',_binary '',NULL,NULL),(9,'机器学习','人工智能','机器学习算法与应用',_binary '',NULL,NULL),(10,'深度学习','人工智能','深度神经网络',_binary '',NULL,NULL),(11,'数据分析','数据科学','数据分析与可视化',_binary '',NULL,NULL),(12,'数学建模','数学','数学建模与仿真',_binary '',NULL,NULL),(13,'项目管理','管理','项目管理与团队协作',_binary '',NULL,NULL),(14,'UI设计','设计','用户界面设计',_binary '',NULL,NULL),(15,'产品设计','设计','产品设计与用户体验',_binary '',NULL,NULL),(16,'Android','移动','Android应用开发',_binary '',NULL,'2026-01-22 14:18:50'),(17,'iOS开发','移动开发','iOS应用开发',_binary '',NULL,NULL),(18,'Flutter','移动开发','跨平台移动应用开发',_binary '',NULL,NULL),(19,'React Native','移动开发','React Native跨平台开发',_binary '',NULL,NULL),(20,'Unity3D','游戏开发','Unity3D游戏引擎开发',_binary '',NULL,NULL),(21,'Unreal Engine','游戏开发','Unreal Engine游戏开发',_binary '',NULL,NULL),(22,'Cocos2d','游戏开发','Cocos2d游戏开发',_binary '',NULL,NULL),(23,'嵌入式C','嵌入式','嵌入式系统C语言开发',_binary '',NULL,NULL),(24,'Arduino','嵌入式','Arduino开发平台',_binary '',NULL,NULL),(25,'树莓派','嵌入式','树莓派项目开发',_binary '',NULL,NULL),(26,'软件测试','测试','软件质量保证与测试',_binary '',NULL,NULL),(27,'自动化测试','测试','测试自动化工具与框架',_binary '',NULL,NULL),(28,'性能测试','测试','系统性能测试与优化',_binary '',NULL,NULL),(29,'Linux运维','DevOps','Linux系统运维',_binary '',NULL,NULL),(30,'CI/CD','DevOps','持续集成与持续部署',_binary '',NULL,NULL),(31,'监控运维','DevOps','系统监控与运维',_binary '',NULL,NULL),(32,'Go','backend','test create',_binary '','2026-01-14 15:17:19','2026-01-14 15:17:19');
/*!40000 ALTER TABLE `skills` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `teacher_application_skills`
--

DROP TABLE IF EXISTS `teacher_application_skills`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `teacher_application_skills` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `teacher_application_id` bigint NOT NULL,
  `skill_id` bigint NOT NULL,
  `weight` int DEFAULT '1',
  PRIMARY KEY (`id`),
  KEY `fk_teacher_application_skills_application` (`teacher_application_id`),
  KEY `fk_teacher_application_skills_skill` (`skill_id`),
  CONSTRAINT `fk_teacher_application_skills_application` FOREIGN KEY (`teacher_application_id`) REFERENCES `teacher_applications` (`id`),
  CONSTRAINT `fk_teacher_application_skills_skill` FOREIGN KEY (`skill_id`) REFERENCES `skills` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `teacher_application_skills`
--

LOCK TABLES `teacher_application_skills` WRITE;
/*!40000 ALTER TABLE `teacher_application_skills` DISABLE KEYS */;
INSERT INTO `teacher_application_skills` VALUES (1,1,1,2),(3,17,10,1),(4,16,10,1),(5,16,11,2),(6,18,8,1),(7,18,3,2),(8,19,4,1),(9,19,22,3),(10,19,24,2),(11,19,17,1),(12,19,18,4),(13,20,24,1),(14,20,5,4),(15,20,4,5),(16,20,7,3),(17,20,8,1),(18,21,5,3),(19,21,6,4),(20,21,7,2),(21,22,4,1),(22,22,18,1),(23,25,16,1),(24,25,4,3),(25,25,2,1);
/*!40000 ALTER TABLE `teacher_application_skills` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `teacher_applications`
--

DROP TABLE IF EXISTS `teacher_applications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `teacher_applications` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `competition_id` bigint NOT NULL,
  `teacher_id` bigint NOT NULL,
  `status` varchar(20) DEFAULT 'PENDING',
  `applied_at` datetime DEFAULT NULL,
  `reviewed_at` datetime DEFAULT NULL,
  `reviewed_by` bigint DEFAULT NULL,
  `review_comment` varchar(255) DEFAULT NULL,
  `generated_team_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_teacher_applications_competition_teacher` (`competition_id`,`teacher_id`),
  KEY `fk_teacher_applications_teacher` (`teacher_id`),
  KEY `fk_teacher_applications_reviewer` (`reviewed_by`),
  KEY `fk_teacher_applications_generated_team` (`generated_team_id`),
  CONSTRAINT `fk_teacher_applications_competition` FOREIGN KEY (`competition_id`) REFERENCES `competitions` (`id`),
  CONSTRAINT `fk_teacher_applications_generated_team` FOREIGN KEY (`generated_team_id`) REFERENCES `teams` (`id`),
  CONSTRAINT `fk_teacher_applications_reviewer` FOREIGN KEY (`reviewed_by`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_teacher_applications_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `teacher_applications`
--

LOCK TABLES `teacher_applications` WRITE;
/*!40000 ALTER TABLE `teacher_applications` DISABLE KEYS */;
INSERT INTO `teacher_applications` VALUES (1,1,1,'APPROVED','2026-01-15 12:28:06','2026-01-15 12:46:15',1,'ok',NULL),(2,2,1,'REJECTED','2026-01-15 12:34:07','2026-01-15 12:47:16',1,'资料不完整',NULL),(3,3,1,'APPROVED','2026-01-15 13:04:08','2026-01-15 13:05:01',1,NULL,1),(4,9,1,'APPROVED','2026-01-15 15:26:35','2026-01-15 15:39:03',1,'approved for M4 test',2),(5,8,2,'APPROVED','2026-01-15 17:27:54','2026-01-15 17:28:58',4,'m3 rollback verification',3),(6,7,2,'APPROVED','2026-01-15 19:35:54','2026-01-15 19:37:44',4,'yyy',4),(7,10,2,'APPROVED','2026-01-15 22:01:12','2026-01-15 22:02:17',4,'符合竞赛指导要求',5),(8,9,2,'APPROVED','2026-01-16 09:06:41','2026-01-16 09:07:20',4,'符合竞赛指导要求',6),(9,3,2,'APPROVED','2026-01-18 11:07:56','2026-01-18 11:10:06',4,'符合竞赛指导要求',7),(10,11,2,'APPROVED','2026-01-19 22:26:17','2026-01-19 22:47:39',4,NULL,8),(11,6,2,'APPROVED','2026-01-19 22:54:03','2026-01-19 22:55:00',4,NULL,9),(12,12,2,'APPROVED','2026-01-20 11:44:04','2026-01-20 11:44:26',4,NULL,11),(13,2,2,'REJECTED','2026-01-20 00:41:03','2026-01-20 11:11:46',4,'123',NULL),(14,5,2,'APPROVED','2026-01-20 11:12:50','2026-01-20 11:13:19',4,NULL,10),(15,13,2,'REJECTED','2026-01-20 12:25:28','2026-01-20 12:25:59',4,'你不符合要求',NULL),(16,14,2,'APPROVED','2026-01-21 20:15:28','2026-01-21 20:15:54',4,'符合竞赛指导要求',13),(17,15,2,'APPROVED','2026-01-21 20:08:42','2026-01-21 20:14:52',4,'符合竞赛指导要求',12),(18,14,7,'APPROVED','2026-01-21 20:18:13','2026-01-21 20:19:13',4,NULL,14),(19,16,2,'APPROVED','2026-01-21 23:09:32','2026-01-21 23:12:01',4,NULL,15),(20,18,2,'APPROVED','2026-01-22 15:45:25','2026-01-22 15:45:41',4,NULL,16),(21,19,2,'APPROVED','2026-01-22 15:50:35','2026-01-22 15:50:43',4,NULL,17),(22,21,2,'APPROVED','2026-01-25 23:38:32','2026-01-25 23:38:53',4,NULL,19),(23,20,2,'APPROVED','2026-01-25 23:38:40','2026-01-25 23:38:51',4,NULL,18),(24,22,2,'APPROVED','2026-01-26 12:07:16','2026-01-26 12:07:25',4,NULL,20),(25,23,2,'APPROVED','2026-04-13 22:42:21','2026-04-13 22:42:53',4,NULL,21),(26,24,2,'PENDING','2026-04-22 10:35:13',NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `teacher_applications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `team_awards`
--

DROP TABLE IF EXISTS `team_awards`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `team_awards` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `competition_id` bigint NOT NULL,
  `team_id` bigint NOT NULL,
  `award_name` varchar(64) NOT NULL,
  `published_by` bigint NOT NULL,
  `published_at` datetime NOT NULL,
  `is_active` tinyint NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_team_awards_competition_team_award` (`competition_id`,`team_id`,`award_name`),
  KEY `fk_team_awards_published_by` (`published_by`),
  KEY `idx_team_awards_team` (`team_id`),
  KEY `idx_team_awards_comp` (`competition_id`),
  CONSTRAINT `fk_team_awards_competition` FOREIGN KEY (`competition_id`) REFERENCES `competitions` (`id`),
  CONSTRAINT `fk_team_awards_published_by` FOREIGN KEY (`published_by`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_team_awards_team` FOREIGN KEY (`team_id`) REFERENCES `teams` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `team_awards`
--

LOCK TABLES `team_awards` WRITE;
/*!40000 ALTER TABLE `team_awards` DISABLE KEYS */;
INSERT INTO `team_awards` VALUES (1,9,6,'一等奖',4,'2026-01-18 10:06:56',1),(2,3,7,'一等奖',4,'2026-01-21 13:12:22',1);
/*!40000 ALTER TABLE `team_awards` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `team_discussion_posts`
--

DROP TABLE IF EXISTS `team_discussion_posts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `team_discussion_posts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `team_id` bigint NOT NULL,
  `author_id` bigint NOT NULL,
  `parent_post_id` bigint DEFAULT NULL,
  `content` text NOT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `deleted_at` datetime DEFAULT NULL,
  `deleted_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_team_discussion_posts_team` (`team_id`),
  KEY `fk_team_discussion_posts_author` (`author_id`),
  KEY `fk_team_discussion_posts_parent` (`parent_post_id`),
  KEY `fk_team_discussion_posts_deleted_by` (`deleted_by`),
  CONSTRAINT `fk_team_discussion_posts_author` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_team_discussion_posts_deleted_by` FOREIGN KEY (`deleted_by`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_team_discussion_posts_parent` FOREIGN KEY (`parent_post_id`) REFERENCES `team_discussion_posts` (`id`),
  CONSTRAINT `fk_team_discussion_posts_team` FOREIGN KEY (`team_id`) REFERENCES `teams` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `team_discussion_posts`
--

LOCK TABLES `team_discussion_posts` WRITE;
/*!40000 ALTER TABLE `team_discussion_posts` DISABLE KEYS */;
INSERT INTO `team_discussion_posts` VALUES (1,6,5,NULL,'first post','2026-01-16 14:12:47',NULL,NULL,NULL),(2,4,5,NULL,'root','2026-01-16 14:49:28','2026-01-16 15:33:24','2026-01-16 15:33:24',2),(3,4,1,NULL,'reply','2026-01-16 14:51:25','2026-01-16 15:34:53','2026-01-16 15:34:53',4),(4,4,1,NULL,'root2','2026-01-16 15:02:11','2026-01-16 15:31:45','2026-01-16 15:31:45',1),(5,4,5,4,'reply2','2026-01-16 15:03:33',NULL,NULL,NULL),(6,6,5,NULL,'root3','2026-01-16 15:36:46','2026-01-16 15:40:56','2026-01-16 15:40:56',5),(7,6,5,NULL,'root4','2026-01-16 15:48:27',NULL,NULL,NULL),(8,4,1,NULL,'root5','2026-01-16 15:50:00','2026-01-16 15:53:00','2026-01-16 15:53:00',1),(9,4,5,8,'root5-re1','2026-01-16 15:50:40','2026-01-16 15:53:00','2026-01-16 15:53:00',1),(10,4,5,8,'root5-re2','2026-01-16 15:50:49','2026-01-16 15:53:00','2026-01-16 15:53:00',1),(11,11,5,NULL,'你好','2026-01-20 18:40:36','2026-01-20 19:45:54','2026-01-20 19:45:54',5),(12,11,5,11,'我很好 你呢','2026-01-20 18:43:32','2026-01-20 19:45:46','2026-01-20 19:45:46',5),(13,11,2,11,'大家好啊','2026-01-20 18:49:44','2026-01-20 19:45:54','2026-01-20 19:45:54',5),(14,9,2,NULL,'你好','2026-01-20 18:54:45',NULL,NULL,NULL),(15,11,5,NULL,'你好','2026-01-21 16:28:54',NULL,NULL,NULL),(16,4,1,NULL,'现在大家的任务都完成得怎么样了','2026-04-20 14:50:05',NULL,NULL,NULL),(17,4,5,NULL,'我的那一部分已经可以提交了','2026-04-20 14:52:02','2026-04-20 15:29:21','2026-04-20 15:29:21',5),(18,4,1,16,'我的那一部分已经差不多了','2026-04-20 15:03:27','2026-04-20 15:28:45','2026-04-20 15:28:45',1),(19,4,5,16,'我的那一部分已经完成得差不多了','2026-04-20 15:29:39',NULL,NULL,NULL);
/*!40000 ALTER TABLE `team_discussion_posts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `team_members`
--

DROP TABLE IF EXISTS `team_members`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `team_members` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `team_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `role` varchar(20) DEFAULT 'MEMBER',
  `joined_at` datetime DEFAULT NULL,
  `left_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_team_members_user` (`user_id`),
  KEY `idx_team_members_team_left_at` (`team_id`,`left_at`),
  CONSTRAINT `fk_team_members_team` FOREIGN KEY (`team_id`) REFERENCES `teams` (`id`),
  CONSTRAINT `fk_team_members_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `team_members`
--

LOCK TABLES `team_members` WRITE;
/*!40000 ALTER TABLE `team_members` DISABLE KEYS */;
INSERT INTO `team_members` VALUES (1,4,1,'MEMBER','2026-01-15 19:43:26',NULL),(2,6,5,'MEMBER','2026-01-16 09:37:07','2026-01-16 09:38:50'),(3,6,5,'MEMBER','2026-01-16 09:41:20','2026-01-16 10:51:57'),(4,6,5,'MEMBER','2026-01-16 11:24:20','2026-01-16 11:31:31'),(5,6,5,'MEMBER','2026-01-16 14:06:47','2026-01-18 10:58:25'),(6,4,5,'MEMBER','2026-01-16 14:48:32',NULL),(7,7,5,'MEMBER','2026-01-18 11:19:22',NULL),(8,11,5,'MEMBER','2026-01-20 13:39:26','2026-01-20 13:52:28'),(9,11,5,'MEMBER','2026-01-20 13:54:51','2026-01-20 15:56:21'),(10,11,5,'MEMBER','2026-01-20 16:01:00',NULL),(11,16,5,'MEMBER','2026-01-22 15:46:16','2026-01-22 15:46:47'),(12,17,5,'MEMBER','2026-01-23 14:04:19',NULL),(13,18,5,'MEMBER','2026-01-25 23:39:40',NULL),(14,19,5,'MEMBER','2026-01-25 23:39:42',NULL),(15,13,5,'MEMBER','2026-04-14 09:27:29',NULL),(16,12,5,'MEMBER','2026-04-14 09:27:32',NULL),(17,21,17,'MEMBER','2026-04-15 13:20:27',NULL),(18,21,18,'MEMBER','2026-04-15 13:44:05',NULL);
/*!40000 ALTER TABLE `team_members` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `team_skills`
--

DROP TABLE IF EXISTS `team_skills`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `team_skills` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `team_id` bigint NOT NULL,
  `skill_id` bigint NOT NULL,
  `weight` int DEFAULT '1',
  PRIMARY KEY (`id`),
  KEY `fk_team_skills_team` (`team_id`),
  KEY `fk_team_skills_skill` (`skill_id`),
  CONSTRAINT `fk_team_skills_skill` FOREIGN KEY (`skill_id`) REFERENCES `skills` (`id`),
  CONSTRAINT `fk_team_skills_team` FOREIGN KEY (`team_id`) REFERENCES `teams` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `team_skills`
--

LOCK TABLES `team_skills` WRITE;
/*!40000 ALTER TABLE `team_skills` DISABLE KEYS */;
INSERT INTO `team_skills` VALUES (1,12,10,1),(2,13,11,2),(3,13,10,1),(4,14,8,1),(5,14,3,2),(6,15,4,1),(7,15,22,3),(8,15,24,2),(9,15,17,1),(10,15,18,4),(11,16,24,1),(12,16,5,4),(13,16,4,5),(14,16,7,3),(15,16,8,1),(16,17,5,3),(17,17,6,4),(18,17,7,2),(19,19,4,1),(20,19,18,1),(21,21,16,1),(22,21,4,3),(23,21,2,1);
/*!40000 ALTER TABLE `team_skills` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `team_submissions`
--

DROP TABLE IF EXISTS `team_submissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `team_submissions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `team_id` bigint NOT NULL,
  `competition_id` bigint NOT NULL,
  `submitted_by` bigint NOT NULL,
  `file_name` varchar(255) NOT NULL,
  `file_url` varchar(512) NOT NULL,
  `remark` varchar(255) DEFAULT NULL,
  `submitted_at` datetime DEFAULT NULL,
  `is_current` bit(1) DEFAULT b'1',
  PRIMARY KEY (`id`),
  KEY `fk_team_submissions_team` (`team_id`),
  KEY `fk_team_submissions_competition` (`competition_id`),
  KEY `fk_team_submissions_submitter` (`submitted_by`),
  CONSTRAINT `fk_team_submissions_competition` FOREIGN KEY (`competition_id`) REFERENCES `competitions` (`id`),
  CONSTRAINT `fk_team_submissions_submitter` FOREIGN KEY (`submitted_by`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_team_submissions_team` FOREIGN KEY (`team_id`) REFERENCES `teams` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `team_submissions`
--

LOCK TABLES `team_submissions` WRITE;
/*!40000 ALTER TABLE `team_submissions` DISABLE KEYS */;
INSERT INTO `team_submissions` VALUES (1,6,9,5,'sp+vue.md','/files/teams/6/1768570134686_adaeab25-f14f-43bb-b9df-1bb0342e8f77.md','first upload','2026-01-16 21:28:55',_binary '\0'),(2,6,9,5,'sp+vue.md','/files/teams/6/1768570852706_5c30cbd5-04fe-480f-981a-8f45183adb80.md','first upload','2026-01-16 21:40:53',_binary '\0'),(3,6,9,5,'sp+vue.md','/files/teams/6/1768571031068_24cd158b-1ca4-4635-bb64-66e3424af8e8.md','first upload','2026-01-16 21:43:51',_binary '\0'),(4,6,9,5,'sp+vue.md','/files/teams/6/1768571036343_b6178ea2-54a6-496c-b119-2fc2b4cd7d1a.md','first upload','2026-01-16 21:43:56',_binary '\0'),(5,6,9,5,'todolist.md','/files/teams/6/1768603437092_0127a116-af8d-4ee4-9ede-b304e95c3138.md','Mt-step2 test','2026-01-17 06:43:57',_binary '\0'),(6,6,9,5,'todolist.md','/files/teams/6/1768603708021_aca858b5-abcf-4087-bb9a-b102b8e1efff.md','Mt-step2 test','2026-01-17 06:48:28',_binary '\0'),(7,6,9,5,'todolist.md','/files/teams/6/1768603903643_391babbe-8f19-4db7-aac5-52fe649f0d1b.md','Mt-step2 test','2026-01-17 06:51:44',_binary '\0'),(8,6,9,5,'todolist.md','/files/teams/6/1768604500539_0c9806ad-a947-42a6-8026-cc649246ae76.md','Mt-step2 test','2026-01-17 07:01:41',_binary '\0'),(9,6,9,5,'todolist.md','/files/teams/6/1768604515350_c1d890aa-bcc4-4419-bb2b-91a94ffea304.md','Mt-step2 test','2026-01-17 07:01:55',_binary '\0'),(10,6,9,5,'todolist.md','/files/teams/6/1768604520587_d5116288-1bda-409e-a417-b94bd8c78ad3.md','Mt-step2 test','2026-01-17 07:02:01',_binary '\0'),(11,6,9,5,'todolist.md','/files/teams/6/1768605130461_49925625-be7e-443e-b7f9-58c524e7497b.md','Mt-step2 test','2026-01-17 07:12:10',_binary ''),(12,11,12,5,'1.md','/files/teams/11/1768913566835_02664ee2-ac05-41a7-be03-2cb206b5ad9a.md','第一次项目文件','2026-01-20 20:52:47',_binary '\0'),(13,11,12,5,'新建 文本文档 (2).txt','/files/teams/11/1768914915252_929d3c4b-7e43-40b3-87af-babe5ada2160.txt','第二次','2026-01-20 21:15:15',_binary ''),(14,4,7,1,'logo.png','/files/teams/4/1776671523020_6dbd4621-a521-4699-8d65-eb2548eca6e1.png',NULL,'2026-04-20 15:52:03',_binary '\0'),(15,4,7,1,'motto.png','/files/teams/4/1776672012272_e31923fa-92a3-434c-8f8f-dc044d43bdcb.png','修改后的图片','2026-04-20 16:00:12',_binary '');
/*!40000 ALTER TABLE `team_submissions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `teams`
--

DROP TABLE IF EXISTS `teams`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `teams` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `competition_id` bigint NOT NULL,
  `leader_id` bigint NOT NULL,
  `name` varchar(128) NOT NULL,
  `description` text,
  `status` varchar(20) DEFAULT 'RECRUITING',
  `closed_at` datetime DEFAULT NULL,
  `closed_by` bigint DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_teams_competition_leader` (`competition_id`,`leader_id`),
  KEY `fk_teams_leader` (`leader_id`),
  KEY `fk_teams_closed_by` (`closed_by`),
  CONSTRAINT `fk_teams_closed_by` FOREIGN KEY (`closed_by`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_teams_competition` FOREIGN KEY (`competition_id`) REFERENCES `competitions` (`id`),
  CONSTRAINT `fk_teams_leader` FOREIGN KEY (`leader_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `teams`
--

LOCK TABLES `teams` WRITE;
/*!40000 ALTER TABLE `teams` DISABLE KEYS */;
INSERT INTO `teams` VALUES (1,3,1,'Team-3-1',NULL,'DISBANDED','2026-01-22 14:26:20',4,'2026-01-15 13:05:01','2026-01-22 14:26:24'),(2,9,1,'Team-9-1',NULL,'CLOSED','2026-01-21 20:53:19',4,'2026-01-15 15:39:03','2026-01-21 20:53:19'),(3,8,2,'Team-8-2',NULL,'CLOSED','2026-01-15 21:44:07',2,'2026-01-15 17:28:58','2026-01-15 21:44:07'),(4,7,2,'Team-7-2',NULL,'RECRUITING',NULL,NULL,'2026-01-15 19:37:44','2026-01-15 19:37:44'),(5,10,2,'Team-10-2',NULL,'CLOSED','2026-01-15 22:05:17',2,'2026-01-15 22:02:17','2026-01-15 22:05:17'),(6,9,2,'Team-9-2',NULL,'DISBANDED','2026-01-18 10:06:47',4,'2026-01-16 09:07:20','2026-01-18 10:58:25'),(7,3,2,'Team-3-2',NULL,'CLOSED','2026-01-18 11:20:08',4,'2026-01-18 11:10:06','2026-01-18 11:20:08'),(8,11,2,'Team-11-2',NULL,'RECRUITING',NULL,NULL,'2026-01-19 22:47:39','2026-01-19 22:47:39'),(9,6,2,'Team-6-2',NULL,'CLOSED','2026-01-20 16:03:07',4,'2026-01-19 22:55:00','2026-01-20 16:03:07'),(10,5,2,'Team-5-2',NULL,'CLOSED','2026-01-20 16:01:42',2,'2026-01-20 11:13:19','2026-01-20 16:01:42'),(11,12,2,'Team-12-2',NULL,'RECRUITING',NULL,NULL,'2026-01-20 11:44:26','2026-01-20 11:44:26'),(12,15,2,'Team-15-2',NULL,'RECRUITING',NULL,NULL,'2026-01-21 20:14:52','2026-01-21 20:14:52'),(13,14,2,'Team-14-2',NULL,'RECRUITING',NULL,NULL,'2026-01-21 20:15:54','2026-01-21 20:15:54'),(14,14,7,'Team-14-7',NULL,'RECRUITING',NULL,NULL,'2026-01-21 20:19:13','2026-01-21 20:19:13'),(15,16,2,'Team-16-2',NULL,'RECRUITING',NULL,NULL,'2026-01-21 23:12:01','2026-01-21 23:12:01'),(16,18,2,'Team-18-2',NULL,'RECRUITING',NULL,NULL,'2026-01-22 15:45:41','2026-01-22 15:45:41'),(17,19,2,'Team-19-2',NULL,'RECRUITING',NULL,NULL,'2026-01-22 15:50:43','2026-01-22 15:50:43'),(18,20,2,'Team-20-2',NULL,'RECRUITING',NULL,NULL,'2026-01-25 23:38:51','2026-01-25 23:38:51'),(19,21,2,'Team-21-2',NULL,'RECRUITING',NULL,NULL,'2026-01-25 23:38:53','2026-01-25 23:38:53'),(20,22,2,'Team-22-2',NULL,'RECRUITING',NULL,NULL,'2026-01-26 12:07:25','2026-01-26 12:07:25'),(21,23,2,'Team-23-2',NULL,'RECRUITING',NULL,NULL,'2026-04-13 22:42:53','2026-04-13 22:42:53');
/*!40000 ALTER TABLE `teams` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_behaviors`
--

DROP TABLE IF EXISTS `user_behaviors`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_behaviors` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `behavior_type` varchar(20) NOT NULL,
  `target_type` varchar(20) NOT NULL,
  `target_id` bigint NOT NULL,
  `weight` int DEFAULT '1',
  `created_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user_behaviors_user_created_at` (`user_id`,`created_at`),
  CONSTRAINT `fk_user_behaviors_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_behaviors`
--

LOCK TABLES `user_behaviors` WRITE;
/*!40000 ALTER TABLE `user_behaviors` DISABLE KEYS */;
INSERT INTO `user_behaviors` VALUES (1,5,'VIEW','COMPETITION',5,1,'2026-02-26 22:48:44'),(2,5,'VIEW','COMPETITION',10,1,'2026-02-26 22:48:54'),(3,5,'APPLY','COMPETITION',14,5,'2026-02-26 22:52:01'),(4,5,'VIEW','COMPETITION',21,3,'2026-02-26 22:52:49'),(5,6,'VIEW','COMPETITION',21,3,'2026-02-26 23:00:07'),(6,6,'VIEW','COMPETITION',5,1,'2026-02-26 23:00:17'),(7,6,'VIEW','COMPETITION',19,1,'2026-02-26 23:02:54'),(8,1,'APPLY','COMPETITION',23,5,'2026-04-14 09:26:57'),(9,17,'APPLY','COMPETITION',23,5,'2026-04-15 13:20:27'),(10,18,'APPLY','COMPETITION',23,5,'2026-04-15 13:44:04'),(11,1,'VIEW','COMPETITION',2,1,'2026-04-20 14:48:40'),(12,1,'VIEW','COMPETITION',20,1,'2026-04-20 15:06:55'),(13,1,'VIEW','COMPETITION',16,1,'2026-04-20 15:06:59'),(14,1,'VIEW','COMPETITION',18,1,'2026-04-20 15:07:06'),(15,1,'VIEW','COMPETITION',1,1,'2026-04-21 20:07:45'),(16,1,'VIEW','COMPETITION',24,1,'2026-04-22 09:59:45'),(17,1,'VIEW','COMPETITION',7,1,'2026-04-22 10:15:07'),(18,1,'APPLY','COMPETITION',15,5,'2026-04-22 10:29:47');
/*!40000 ALTER TABLE `user_behaviors` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_skills`
--

DROP TABLE IF EXISTS `user_skills`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_skills` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `skill_id` bigint NOT NULL,
  `level` int DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_user_skills_user` (`user_id`),
  KEY `fk_user_skills_skill` (`skill_id`),
  CONSTRAINT `fk_user_skills_skill` FOREIGN KEY (`skill_id`) REFERENCES `skills` (`id`),
  CONSTRAINT `fk_user_skills_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_skills`
--

LOCK TABLES `user_skills` WRITE;
/*!40000 ALTER TABLE `user_skills` DISABLE KEYS */;
INSERT INTO `user_skills` VALUES (14,5,5,2,'2026-01-17 09:29:44'),(15,5,6,4,'2026-01-17 09:29:44'),(16,5,7,1,'2026-01-17 09:29:44'),(23,6,18,1,'2026-01-19 14:32:57'),(24,6,17,1,'2026-01-19 14:33:05'),(26,5,22,3,'2026-01-25 20:27:27'),(28,8,4,1,'2026-04-14 18:59:48'),(29,8,5,3,'2026-04-14 19:00:05'),(30,8,8,3,'2026-04-14 19:00:16'),(31,13,16,3,'2026-04-15 10:29:28'),(32,17,16,3,'2026-04-15 13:18:22'),(33,18,16,3,'2026-04-15 13:43:45'),(34,1,22,3,'2026-04-20 15:07:25'),(35,1,16,2,'2026-04-20 15:07:31'),(36,1,7,1,'2026-04-20 15:07:36');
/*!40000 ALTER TABLE `user_skills` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `account_no` varchar(32) NOT NULL,
  `role` varchar(20) NOT NULL,
  `password` varchar(255) NOT NULL,
  `real_name` varchar(64) DEFAULT NULL,
  `username` varchar(64) DEFAULT NULL,
  `email` varchar(128) DEFAULT NULL,
  `phone` varchar(32) DEFAULT NULL,
  `avatar_url` varchar(255) DEFAULT NULL,
  `school` varchar(128) DEFAULT NULL,
  `major` varchar(128) DEFAULT NULL,
  `grade` varchar(32) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_users_account_no` (`account_no`),
  UNIQUE KEY `uk_users_email` (`email`),
  UNIQUE KEY `uk_users_phone` (`phone`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'cqs','STUDENT','$2a$10$CcC4MbrEIQJSBEzNkH12SOyXmVbQPAxfLJ2Au/aqedmGMwuo3utfG','Alice','cqs','cqs@test.com','13800000000','/a.png','CS','SE','2021','2026-01-14 15:42:47','2026-01-14 15:57:04'),(2,'teacher01','TEACHER','$2a$10$CcC4MbrEIQJSBEzNkH12SOyXmVbQPAxfLJ2Au/aqedmGMwuo3utfG','Bob','Bob','teacher01@test.com','13900000001','/avatar/teacher02.png',NULL,NULL,NULL,'2026-01-15 16:11:32','2026-01-15 16:11:32'),(4,'admin01','ADMIN','$2a$10$CcC4MbrEIQJSBEzNkH12SOyXmVbQPAxfLJ2Au/aqedmGMwuo3utfG','Bob','Tom','admin01@test.com','13900000003','/avatar/teacher02.png',NULL,NULL,NULL,'2026-01-15 16:13:05','2026-01-15 16:13:05'),(5,'s02','STUDENT','$2a$10$npwptl1PGoYtd0ztM/jtse7Qr9HP8sZMsVoWjAz2FFpCshsI86woS','s02','s02','stu02@test.com',NULL,NULL,NULL,NULL,NULL,'2026-01-16 09:32:16','2026-01-16 09:32:16'),(6,'s03','STUDENT','$2a$10$E/./68n1Tq1vAHlSmIzpjO70X1jkplJzQKnmSbFLsd9XiDHhtrL8m','s03','s03','stu03@test.com',NULL,NULL,NULL,NULL,NULL,'2026-01-19 14:11:15','2026-01-19 14:11:15'),(7,'t02','TEACHER','$2a$10$ln2YW11AdssV74wjt/yVT.IFZ./2wcCW9alclD4z2T9vXexCESoea','t02','t02','TEA02@test.com',NULL,NULL,NULL,NULL,NULL,'2026-01-20 16:22:10','2026-01-20 16:22:10'),(8,'s04','STUDENT','$2a$10$0ZShx7tzh4DA2ZebNlLy0OPt8WWXpVC1KZzRroqYZJ3vezZvhCzuK',NULL,'s04','12345678@qq.com',NULL,NULL,NULL,NULL,NULL,'2026-04-14 13:23:16','2026-04-14 13:23:16'),(9,'diag5672','STUDENT','$2a$10$NA8lW8H6zA1er5XaMUQXhOpamTuQc7S2cszELGcIXDznfx4kILT8C',NULL,'diag5672','diag5672@example.com',NULL,NULL,NULL,NULL,NULL,'2026-04-14 13:37:09','2026-04-14 13:37:09'),(10,'diag1776173848','STUDENT','$2a$10$Mh1OoHlBTrmEipjoT8SZ4.ulswNz0kZlSBvRUCtrKW3bdW4KCXjRy',NULL,'diag1776173848','diag1776173848@example.com',NULL,NULL,NULL,NULL,NULL,'2026-04-14 13:37:28','2026-04-14 13:37:28'),(11,'s05','STUDENT','$2a$10$/bBhu/kmka5FcE3WBVpbJeueiPRXI95jNyb1De5IH5qLzgZ31Iug6','s05','s05','STU05@test.com',NULL,NULL,NULL,NULL,NULL,'2026-04-14 19:53:07','2026-04-14 19:53:07'),(12,'diagfb1776197142','STUDENT','$2a$10$rTWumXLzkNdh8Vpg20wtlONq3oo5lmxQx7QIWOeArIdA5ocySV3Pu',NULL,'diagfb1776197142','diagfb1776197142@example.com',NULL,NULL,NULL,NULL,NULL,'2026-04-14 20:05:42','2026-04-14 20:05:42'),(13,'s512513','STUDENT','$2a$10$pNrdUPG63c9bY25SZqkxIeX0dRXqKRd/YLJicIXcAq8ipurw.HwN6',NULL,'s512513','s512513@test.com',NULL,NULL,NULL,NULL,NULL,'2026-04-15 10:18:33','2026-04-15 10:18:33'),(14,'s04_179416','STUDENT','$2a$10$0w.OxknTc3z9CV0tSJFbw.dL5z8Wq3ipolAA8g/1v.fCMGp/DTINC',NULL,'s04_179416','s04_179416@test.com',NULL,NULL,NULL,NULL,NULL,'2026-04-15 12:59:53','2026-04-15 12:59:53'),(15,'s05_192572','STUDENT','$2a$10$nG4EEfofUNbJ1A91kavpW.k8jzRCH2Y.04fudKFO2mNufDMGLnJn2',NULL,'s05_192572','s05_192572@test.com',NULL,NULL,NULL,NULL,NULL,'2026-04-15 13:00:23','2026-04-15 13:00:23'),(16,'s06_222697','STUDENT','$2a$10$k22tvozkDBM3UqHAy.Q.MetKGAO/5xBmiAEh4KRSgFlSgUsUAbDDC',NULL,'s06_222697','s06_222697@test.com',NULL,NULL,NULL,NULL,NULL,'2026-04-15 13:03:55','2026-04-15 13:03:55'),(17,'s04_954845','STUDENT','$2a$10$ukNy7O6uVInd1g5RFP.YPeQWn.vrYmsFN140z0XsRJ.ozQcxJAp0G',NULL,'s04_954845','s04_954845@test.com',NULL,NULL,NULL,NULL,NULL,'2026-04-15 13:12:35','2026-04-15 13:12:35'),(18,'s05_797950','STUDENT','$2a$10$bEI1vBYJ/H7PFAp5rxucceZ9PKCZUT3RhYLw7Y5unh9E7icQur5CO',NULL,'s05_797950','s05_797950@test.com',NULL,NULL,NULL,NULL,NULL,'2026-04-15 13:43:18','2026-04-15 13:43:18'),(19,'ex_4250266','STUDENT','$2a$10$AU2Xzh.WDO40IibIpBc3X.4y.vlWsBnRxbzGMp16HRgXHRgDKurxm',NULL,'ex_4250266','ex_4250266@test.com',NULL,NULL,NULL,NULL,NULL,'2026-04-15 14:24:10','2026-04-15 14:24:10'),(20,'ex_4623687','STUDENT','$2a$10$9P7V7A69WRn/HO0z0JQr3e6pYzv4rlrMaNPEZHjvc6cP.OvG1Vk4.',NULL,'ex_4623687','ex_4623687@test.com',NULL,NULL,NULL,NULL,NULL,'2026-04-15 14:30:24','2026-04-15 14:30:24'),(21,'ex_4951751','STUDENT','$2a$10$Ej.6N7d7.ouD/9zuyP/Wf.2jJXT7RDgFQsn7vAfeoWnJTEIi3ZEz.',NULL,'ex_4951751','ex_4951751@test.com',NULL,NULL,NULL,NULL,NULL,'2026-04-15 14:35:52','2026-04-15 14:35:52'),(22,'ex_5450240','STUDENT','$2a$10$jfblLlfdJ7Ui008fzbCxNO15UjqQgNOJYAsUhHE57Dn.TPWsFQ.Tm',NULL,'ex_5450240','ex_5450240@test.com',NULL,NULL,NULL,NULL,NULL,'2026-04-15 14:44:10','2026-04-15 14:44:10'),(23,'ex_5789381','STUDENT','$2a$10$.ub2eCGlLIDgYjJ8d2RWhOJhTnsm5VpIYxR/eV/d1NlBQSck0n5Cm',NULL,'ex_5789381','ex_5789381@test.com',NULL,NULL,NULL,NULL,NULL,'2026-04-15 14:49:50','2026-04-15 14:49:50');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-22 14:24:25
