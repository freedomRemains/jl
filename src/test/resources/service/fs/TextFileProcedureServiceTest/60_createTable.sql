USE JLDB;

CREATE TABLE MGNRGRP(
	MGNRGRP_ID INT AUTO_INCREMENT,
	GNR_GRP_NAME VARCHAR(256),
	VERSION INT,
	DEL_FLG INT,
	CREATE_USER VARCHAR(128),
	CREATE_DATE DATETIME,
	UPDATE_USER VARCHAR(128),
	UPDATE_DATE TIMESTAMP,
	PRIMARY KEY(MGNRGRP_ID)
);

CREATE TABLE MGNRKEYVAL(
	MGNRKEYVAL_ID INT AUTO_INCREMENT,
	GNR_KEY VARCHAR(256),
	GNR_VAL VARCHAR(256),
	MGNRGRP_ID INT,
	ORD_IN_GRP INT,
	VERSION INT,
	DEL_FLG INT,
	CREATE_USER VARCHAR(128),
	CREATE_DATE DATETIME,
	UPDATE_USER VARCHAR(128),
	UPDATE_DATE TIMESTAMP,
	PRIMARY KEY(MGNRKEYVAL_ID)
);

CREATE TABLE TACCOUNT(
	TACCOUNT_ID INT AUTO_INCREMENT,
	ACCOUNT_NAME VARCHAR(256),
	PASSWORD VARCHAR(256),
	VERSION INT,
	DEL_FLG INT,
	CREATE_USER VARCHAR(128),
	CREATE_DATE DATETIME,
	UPDATE_USER VARCHAR(128),
	UPDATE_DATE TIMESTAMP,
	PRIMARY KEY(TACCOUNT_ID)
);

CREATE TABLE IF NOT EXISTS TMAIL(
	TMAIL_ID INT NOT NULL AUTO_INCREMENT,
	MAIL_FROM VARCHAR(256),
	MAIL_SUBJECT VARCHAR(256),
	SENT_DATE DATETIME,
	HONBUN VARCHAR(8192),
	VERSION INT,
	DEL_FLG INT,
	CREATE_USER VARCHAR(128),
	CREATE_DATE DATETIME,
	UPDATE_USER VARCHAR(128),
	UPDATE_DATE TIMESTAMP,
	PRIMARY KEY(TMAIL_ID)
);

CREATE TABLE TSCR(
	TSCR_ID INT AUTO_INCREMENT,
	SCR_NAME VARCHAR(256),
	VERSION INT,
	DEL_FLG INT,
	CREATE_USER VARCHAR(128),
	CREATE_DATE DATETIME,
	UPDATE_USER VARCHAR(128),
	UPDATE_DATE TIMESTAMP,
	PRIMARY KEY(TSCR_ID)
);

CREATE TABLE TSCRELM(
	TSCRELM_ID INT AUTO_INCREMENT,
	SERVICE_NAME VARCHAR(256),
	ADAPTER VARCHAR(256),
	PREPARE_INPUT VARCHAR(256),
	TSCR_ID INT,
	ORD_IN_GRP INT,
	VERSION INT,
	DEL_FLG INT,
	CREATE_USER VARCHAR(128),
	CREATE_DATE DATETIME,
	UPDATE_USER VARCHAR(128),
	UPDATE_DATE TIMESTAMP,
	PRIMARY KEY(TSCRELM_ID)
);

SHOW TABLES;
