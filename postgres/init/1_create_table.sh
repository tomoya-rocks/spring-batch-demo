#!/bin/bash

psql -U testuser testdb1 -c "
	CREATE TABLE member ( \
		id VARCHAR(20) NOT NULL PRIMARY KEY, \
		first_name VARCHAR(20) NOT NULL, \
		last_name VARCHAR(20) NOT NULL, \
		full_name VARCHAR(20) NOT NULL \
	);
"
psql -U testuser testdb2 -c "
	CREATE TABLE member ( \
		id VARCHAR(20) NOT NULL PRIMARY KEY, \
		first_name VARCHAR(20) NOT NULL, \
		last_name VARCHAR(20) NOT NULL, \
		full_name VARCHAR(20) NOT NULL \
	);
"
psql -U testuser testdb3 -c "
	CREATE TABLE member ( \
		id VARCHAR(20) NOT NULL PRIMARY KEY, \
		first_name VARCHAR(20) NOT NULL, \
		last_name VARCHAR(20) NOT NULL, \
		full_name VARCHAR(20) NOT NULL \
	);
"