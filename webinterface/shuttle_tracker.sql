-- phpMyAdmin SQL Dump
-- version 3.4.7.1
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Mar 23, 2012 at 12:37 PM
-- Server version: 5.5.17
-- PHP Version: 5.3.8

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `shuttle_tracker`
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
-- Table structure for table `route_coord_distances`
--

CREATE TABLE IF NOT EXISTS `route_coord_distances` (
  `route_id` int(11) NOT NULL,
  `seq_1` int(11) NOT NULL,
  `seq_2` int(11) NOT NULL,
  `distance` float NOT NULL,
  PRIMARY KEY (`route_id`,`seq_1`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `shuttles`
--

CREATE TABLE IF NOT EXISTS `shuttles` (
  `shuttle_id` int(11) NOT NULL,
  `name` varchar(32) NOT NULL,
  UNIQUE KEY `shuttle_id` (`shuttle_id`)
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
  `update_time` datetime NOT NULL,
  `route_id` int(11) DEFAULT '1'
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `shuttle_eta`
--

CREATE TABLE IF NOT EXISTS `shuttle_eta` (
  `shuttle_id` int(11) NOT NULL,
  `stop_id` varchar(24) COLLATE utf8_unicode_ci NOT NULL DEFAULT '',
  `eta_id` int(11) NOT NULL,
  `eta` int(11) NOT NULL,
  `absolute_eta` int(40) NOT NULL,
  `route` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`shuttle_id`,`stop_id`,`eta_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `stats`
--

CREATE TABLE IF NOT EXISTS `stats` (
  `access_id` int(11) NOT NULL AUTO_INCREMENT,
  `ip` varchar(15) COLLATE utf8_unicode_ci NOT NULL,
  `access_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `device` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `comment` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`access_id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci AUTO_INCREMENT=49 ;

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
  `route_id` int(11) NOT NULL,
  UNIQUE KEY `route_id` (`route_id`,`stop_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `test_shuttle_coords`
--

CREATE TABLE IF NOT EXISTS `test_shuttle_coords` (
  `shuttle_id` int(11) NOT NULL,
  `heading` varchar(16) NOT NULL,
  `location` point NOT NULL,
  `speed` varchar(16) NOT NULL,
  `public_status_msg` varchar(255) NOT NULL,
  `cardinal_point` varchar(16) NOT NULL,
  `update_time` datetime NOT NULL,
  `route_id` int(11) NOT NULL DEFAULT '1',
  UNIQUE KEY `update_time` (`update_time`,`shuttle_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `weekend_eta`
--

CREATE TABLE IF NOT EXISTS `weekend_eta` (
  `stop_id` varchar(12) COLLATE utf8_unicode_ci NOT NULL,
  `eta_id` int(11) NOT NULL,
  `eta` time NOT NULL,
  `route` int(11) NOT NULL,
  `arrival_or_departure` varchar(255) COLLATE utf8_unicode_ci NOT NULL DEFAULT 'Departure',
  `shuttle_id` int(11) NOT NULL DEFAULT '1',
  PRIMARY KEY (`stop_id`,`eta_id`,`arrival_or_departure`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
