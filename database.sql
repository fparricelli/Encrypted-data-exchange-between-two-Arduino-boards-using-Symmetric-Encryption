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
  CONSTRAINT `lockdown_username` FOREIGN KEY (`lockdown_username`) REFERENCES `users` (`username`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `account_lockdown`
--

LOCK TABLES `account_lockdown` WRITE;
/*!40000 ALTER TABLE `account_lockdown` DISABLE KEYS */;
INSERT INTO `account_lockdown` VALUES ('wewe','127.0.0.1','2017-12-14 15:26:01'),('wewe','140.0.0.1','2017-12-14 15:02:48');
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
  `first_attempt` date NOT NULL,
  PRIMARY KEY (`username_failed`,`ip`),
  CONSTRAINT `username` FOREIGN KEY (`username_failed`) REFERENCES `users` (`username`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `failed_logins`
--

LOCK TABLES `failed_logins` WRITE;
/*!40000 ALTER TABLE `failed_logins` DISABLE KEYS */;
INSERT INTO `failed_logins` VALUES ('wewe','127.0.0.1',4,'2017-12-14');
/*!40000 ALTER TABLE `failed_logins` ENABLE KEYS */;
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
  PRIMARY KEY (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES ('Nome utente','$2a$12$JtiY7dI2Rs/5P9i4dSYAdOXG4/pQAKINkdnYgKdnkbAHleBvR4yxS'),('nwoegnewnginn','$2a$12$mLZThHSUQL8.V9HEy1a6Su1s1llgmjf1mFW3.NUBbqpRxq8.k2msW'),('Questo','$2a$12$N/htUPF0bVNfT.0TNVEjR.9L18MOg0LP3OL999/1YSPuheL.bd7Y.'),('tizio','hash'),('Untizio','$2a$12$v8THmPyy7fQu71MrNw4zDeAaH1bJSaprLoX7dQuCgko2yrJZGBcNS'),('username','$2a$12$tpfplMs0JaMdJ3naIdAQWuNnzbaORIC/AuhV8GyV2I.u3Zx2lipri'),('wewe','$2a$12$WQmASg/O7zN6bFTg84zDJOjoY5J61m/4EPDp0r2v1yI/Kucsmn3y2'),('wewe2','$2a$12$oxTZWwTNbGjgODm9LBTsT.BsnwR.cot4p7f0wzVO6d1vId6g6nLYC'),('wewe3','$2a$12$VyX9gZDpNrvAJi3AT8.V0OX05skTWbKuf64j7t9e4pRDakqY6IfR2'),('wewe4','$2a$12$KSCqyoEA6hGVnFhV.oRgOOJNTOn63wTAsMpqsXYdc4nze9Qyd4wsa');
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

-- Dump completed on 2017-12-14 16:34:13
