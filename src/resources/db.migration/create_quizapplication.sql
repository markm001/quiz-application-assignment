-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema quizapplication
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `quizapplication` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci ;
USE `quizapplication` ;

-- -----------------------------------------------------
-- Table `quizapplication`.`topic`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `quizapplication`.`topic` ;

CREATE TABLE IF NOT EXISTS `quizapplication`.`topic` (
    `id` INT(11) NOT NULL AUTO_INCREMENT,
    `topicName` VARCHAR(20) NULL DEFAULT NULL,
    PRIMARY KEY (`id`))
    ENGINE = InnoDB
    AUTO_INCREMENT = 4
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `quizapplication`.`question`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `quizapplication`.`question` ;

CREATE TABLE IF NOT EXISTS `quizapplication`.`question` (
    `id` INT(11) NOT NULL AUTO_INCREMENT,
    `difficultyRankNumber` INT(11) NOT NULL,
    `content` VARCHAR(255) NOT NULL,
    `topic_id` INT(11) NULL DEFAULT NULL,
    PRIMARY KEY (`id`),
    INDEX `topic_id` (`topic_id` ASC) VISIBLE,
    CONSTRAINT `question_ibfk_1`
    FOREIGN KEY (`topic_id`)
    REFERENCES `quizapplication`.`topic` (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `quizapplication`.`response`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `quizapplication`.`response` ;

CREATE TABLE IF NOT EXISTS `quizapplication`.`response` (
    `id` INT(11) NOT NULL AUTO_INCREMENT,
    `text` VARCHAR(255) NOT NULL,
    `correct` TINYINT(1) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `text` (`text` ASC, `correct` ASC) VISIBLE)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `quizapplication`.`question_response`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `quizapplication`.`question_response` ;

CREATE TABLE IF NOT EXISTS `quizapplication`.`question_response` (
    `question_id` INT(11) NOT NULL,
    `response_id` INT(11) NOT NULL,
    INDEX `question_id` (`question_id` ASC) VISIBLE,
    INDEX `response_id` (`response_id` ASC) VISIBLE,
    CONSTRAINT `question_response_ibfk_1`
    FOREIGN KEY (`question_id`)
    REFERENCES `quizapplication`.`question` (`id`),
    CONSTRAINT `question_response_ibfk_2`
    FOREIGN KEY (`response_id`)
    REFERENCES `quizapplication`.`response` (`id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;