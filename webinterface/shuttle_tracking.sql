-- phpMyAdmin SQL Dump
-- version 3.2.4
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Feb 06, 2011 at 12:22 AM
-- Server version: 5.1.41
-- PHP Version: 5.3.1

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `shuttle_tracking`
--

-- --------------------------------------------------------

--
-- Table structure for table `routes`
--

CREATE TABLE IF NOT EXISTS `routes` (
  `route_id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `color` varchar(16) NOT NULL,
  `width` int(11) NOT NULL,
  UNIQUE KEY `route_id` (`route_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `route_coords`
--

CREATE TABLE IF NOT EXISTS `route_coords` (
  `route_id` int(11) NOT NULL,
  `seq` int(11) NOT NULL,
  `location` point NOT NULL,
  PRIMARY KEY (`route_id`,`seq`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `sessions`
--

CREATE TABLE IF NOT EXISTS `sessions` (
  `session_id` varchar(32) NOT NULL DEFAULT '',
  `session_time` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `session_start` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `session_value` longtext NOT NULL,
  `ip_address` varchar(16) NOT NULL DEFAULT '',
  `user_agent` varchar(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `shuttles`
--

CREATE TABLE IF NOT EXISTS `shuttles` (
  `shuttle_id` int(11) NOT NULL,
  `name` varchar(32) NOT NULL,
  `icon` int(11) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `shuttle_coords`
--

CREATE TABLE IF NOT EXISTS `shuttle_coords` (
  `shuttle_id` int(11) NOT NULL,
  `heading` varchar(16) NOT NULL,
  `location` point NOT NULL,
  `speed` varchar(16) NOT NULL,
  `public_status_msg` varchar(255) NOT NULL,
  `cardinal_point` varchar(16) NOT NULL,
  `update_time` datetime NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `stops`
--

CREATE TABLE IF NOT EXISTS `stops` (
  `stop_id` varchar(32) NOT NULL,
  `location` point NOT NULL,
  `name` varchar(255) NOT NULL,
  UNIQUE KEY `stop_id` (`stop_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `stop_routes`
--

CREATE TABLE IF NOT EXISTS `stop_routes` (
  `stop_id` varchar(32) NOT NULL,
  `route_id` int(11) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
