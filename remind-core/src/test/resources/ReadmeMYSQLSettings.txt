CREATE TABLE remind_table ( id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, sysdate TIMESTAMP, data CHAR(1) );
insert into remind_table (data) values ('*');

DB: remindtesting
user: remindtest
pwd: stilgero
table: remind_table