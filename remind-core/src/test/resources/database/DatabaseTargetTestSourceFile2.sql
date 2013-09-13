CREATE table IF NOT EXISTS testtable2 ( id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, testinput VARCHAR(255) );
insert into testtable2 (testinput) values ('Das ist ein Insert-Test.');
nsert into testtable2 (testinput) values ('Das ist ein weiterer Insert-Test.');
insert into testtable2 (testinput) values ('Das ist ein dritter Insert-test.');
ALTER TABLE testtable2 ADD COLUMN newinput VARCHAR(255);