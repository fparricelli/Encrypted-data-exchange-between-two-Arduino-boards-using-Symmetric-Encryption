-- MySQL dump 10.13  Distrib 5.7.17, for Win64 (x86_64)
--
-- Host: localhost    Database: secure_messaging
-- ------------------------------------------------------
-- Server version	5.7.19-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `account_lockdown`
--

DROP TABLE IF EXISTS `account_lockdown`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `account_lockdown` (
  `lockdown_username` varchar(100) NOT NULL,
  `ip` varchar(45) NOT NULL,
  `starting` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`lockdown_username`,`ip`),
  CONSTRAINT `lockdown_username` FOREIGN KEY (`lockdown_username`) REFERENCES `users` (`username`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `account_lockdown`
--

LOCK TABLES `account_lockdown` WRITE;
/*!40000 ALTER TABLE `account_lockdown` DISABLE KEYS */;
INSERT INTO `account_lockdown` VALUES ('wewe','140.0.0.1','2017-12-14 15:02:48'),('wewe3','127.0.0.1','2017-12-14 15:47:05'),('wewe4','127.0.01','2017-12-14 15:47:05');
/*!40000 ALTER TABLE `account_lockdown` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `failed_logins`
--

DROP TABLE IF EXISTS `failed_logins`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `failed_logins` (
  `username_failed` varchar(100) NOT NULL,
  `ip` varchar(45) NOT NULL,
  `attempts` int(11) NOT NULL,
  `first_attempt` datetime NOT NULL,
  PRIMARY KEY (`username_failed`,`ip`),
  CONSTRAINT `username` FOREIGN KEY (`username_failed`) REFERENCES `users` (`username`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `failed_logins`
--

LOCK TABLES `failed_logins` WRITE;
/*!40000 ALTER TABLE `failed_logins` DISABLE KEYS */;
INSERT INTO `failed_logins` VALUES ('wewe','127.0.0.1',1,'2017-12-14 18:02:11'),('wewe2','127.0.0.1',1,'2017-12-14 17:10:58'),('wewe3','127.0.0.1',5,'2017-12-14 16:46:50'),('wewe4','127.0.0.1',1,'2017-12-14 18:01:21');
/*!40000 ALTER TABLE `failed_logins` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lockdown_history`
--

DROP TABLE IF EXISTS `lockdown_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lockdown_history` (
  `username` varchar(100) NOT NULL,
  `ip` varchar(45) NOT NULL,
  PRIMARY KEY (`username`,`ip`),
  CONSTRAINT `username_history` FOREIGN KEY (`username`) REFERENCES `users` (`username`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lockdown_history`
--

LOCK TABLES `lockdown_history` WRITE;
/*!40000 ALTER TABLE `lockdown_history` DISABLE KEYS */;
INSERT INTO `lockdown_history` VALUES ('wewe','140.0.0.1'),('wewe4','127.0.01');
/*!40000 ALTER TABLE `lockdown_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mail_codes`
--

DROP TABLE IF EXISTS `mail_codes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `mail_codes` (
  `username` varchar(100) NOT NULL,
  `IP` varchar(45) NOT NULL,
  `issued` datetime NOT NULL,
  `value` varchar(64) NOT NULL,
  PRIMARY KEY (`username`,`IP`),
  CONSTRAINT `username_mail` FOREIGN KEY (`username`) REFERENCES `users` (`username`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mail_codes`
--

LOCK TABLES `mail_codes` WRITE;
/*!40000 ALTER TABLE `mail_codes` DISABLE KEYS */;
INSERT INTO `mail_codes` VALUES ('Luca','127.0.0.1','2017-12-14 19:34:19','f754a450bb39ebb19d07a286aac3968cd1f4e2a5d445230e7d1d213c6a5a8980');
/*!40000 ALTER TABLE `mail_codes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Temporary view structure for view `malicious_ip`
--

DROP TABLE IF EXISTS `malicious_ip`;
/*!50001 DROP VIEW IF EXISTS `malicious_ip`*/;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
/*!50001 CREATE VIEW `malicious_ip` AS SELECT 
 1 AS `IP`,
 1 AS `DIFFERENT USERS FAILED`*/;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `roles`
--

DROP TABLE IF EXISTS `roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `roles` (
  `name` varchar(20) NOT NULL,
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `roles`
--

LOCK TABLES `roles` WRITE;
/*!40000 ALTER TABLE `roles` DISABLE KEYS */;
INSERT INTO `roles` VALUES ('admin'),('tecnico'),('utente');
/*!40000 ALTER TABLE `roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `trusted_devices`
--

DROP TABLE IF EXISTS `trusted_devices`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `trusted_devices` (
  `username` varchar(100) NOT NULL,
  `ip` varchar(45) NOT NULL,
  PRIMARY KEY (`username`,`ip`),
  CONSTRAINT `users_device` FOREIGN KEY (`username`) REFERENCES `users` (`username`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trusted_devices`
--

LOCK TABLES `trusted_devices` WRITE;
/*!40000 ALTER TABLE `trusted_devices` DISABLE KEYS */;
/*!40000 ALTER TABLE `trusted_devices` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `users` (
  `username` varchar(100) NOT NULL,
  `password` varchar(60) NOT NULL,
  `name` varchar(45) DEFAULT NULL,
  `surname` varchar(45) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `telephone` int(11) DEFAULT NULL,
  `role` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`username`),
  UNIQUE KEY `email_UNIQUE` (`email`),
  UNIQUE KEY `telephone_UNIQUE` (`telephone`),
  KEY `user_role_idx` (`role`),
  CONSTRAINT `user_role` FOREIGN KEY (`role`) REFERENCES `roles` (`name`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES ('aldo','$2a$12$tpfplMs0JaMdJ3naIdAQWuNnzbaORIC/AuhV8GyV2I.u3Zx2lipri','Aldo','Strofaldi',NULL,200,'admin'),('bob','$2a$12$tpfplMs0JaMdJ3naIdAQWuNnzbaORIC/AuhV8GyV2I.u3Zx2lipri','Bob','Agigustatutto',NULL,16002,'tecnico'),('linux','$2a$12$tpfplMs0JaMdJ3naIdAQWuNnzbaORIC/AuhV8GyV2I.u3Zx2lipri','Linus','Torvalds',NULL,207,'tecnico'),('Luca','$2a$12$ApIUXvhm0dgk758J5hmi7ubSMRovxifwNqYeNh6I/pUVslNsw6LrC','Luca','Pirozzi','luca.pirozzi2@gmail.com',NULL,NULL),('Nome utente','$2a$12$JtiY7dI2Rs/5P9i4dSYAdOXG4/pQAKINkdnYgKdnkbAHleBvR4yxS',NULL,NULL,NULL,NULL,NULL),('nwoegnewnginn','$2a$12$mLZThHSUQL8.V9HEy1a6Su1s1llgmjf1mFW3.NUBbqpRxq8.k2msW',NULL,NULL,NULL,NULL,NULL),('paperino','$2a$12$tpfplMs0JaMdJ3naIdAQWuNnzbaORIC/AuhV8GyV2I.u3Zx2lipri','Donald ','Duck',NULL,16000,'utente'),('pepp','$2a$12$tpfplMs0JaMdJ3naIdAQWuNnzbaORIC/AuhV8GyV2I.u3Zx2lipri','Peppe','Barra',NULL,204,'admin'),('Questo','$2a$12$N/htUPF0bVNfT.0TNVEjR.9L18MOg0LP3OL999/1YSPuheL.bd7Y.',NULL,NULL,NULL,NULL,NULL),('tizio','hash',NULL,NULL,NULL,NULL,NULL),('topolino','$2a$12$tpfplMs0JaMdJ3naIdAQWuNnzbaORIC/AuhV8GyV2I.u3Zx2lipri','Mickey','Mouse',NULL,211,'utente'),('Untizio','$2a$12$v8THmPyy7fQu71MrNw4zDeAaH1bJSaprLoX7dQuCgko2yrJZGBcNS',NULL,NULL,NULL,NULL,NULL),('username','$2a$12$tpfplMs0JaMdJ3naIdAQWuNnzbaORIC/AuhV8GyV2I.u3Zx2lipri',NULL,NULL,NULL,NULL,NULL),('wewe','$2a$12$WQmASg/O7zN6bFTg84zDJOjoY5J61m/4EPDp0r2v1yI/Kucsmn3y2',NULL,NULL,NULL,NULL,NULL),('wewe2','$2a$12$oxTZWwTNbGjgODm9LBTsT.BsnwR.cot4p7f0wzVO6d1vId6g6nLYC',NULL,NULL,NULL,NULL,NULL),('wewe3','$2a$12$VyX9gZDpNrvAJi3AT8.V0OX05skTWbKuf64j7t9e4pRDakqY6IfR2',NULL,NULL,NULL,NULL,NULL),('wewe4','$2a$12$KSCqyoEA6hGVnFhV.oRgOOJNTOn63wTAsMpqsXYdc4nze9Qyd4wsa',NULL,NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Final view structure for view `malicious_ip`
--

/*!50001 DROP VIEW IF EXISTS `malicious_ip`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8 */;
/*!50001 SET character_set_results     = utf8 */;
/*!50001 SET collation_connection      = utf8_general_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `malicious_ip` AS select `lockdown_history`.`ip` AS `IP`,count(0) AS `DIFFERENT USERS FAILED` from `lockdown_history` group by `lockdown_history`.`ip` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2017-12-14 22:44:26
