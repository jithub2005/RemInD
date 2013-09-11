/* das ist ein Kommentar */
/* das ist ein zweiter Kommentar*/
-- Das ist ein dritter Kommentar
/* Das ist ein Kommentar mit einem blank end */ * /
CREATE table IF NOT EXISTS testtable2 ( id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, testinput VARCHAR(255) );
insert into testtable2 (testinput) values ('Das ist ein Insert-Test.');
insert into testtable2 (testinput) values ('Das ist ein weiterer Insert-Test.');
/* DIE FOLGENDEN 3 ZEILEN SIND EIN ATOMIC STATEMENT
TEST TEST TEST
 */
insert into testtable2 (testinput) values ('Das ist ein dritter Insert-test.');
ALTER TABLE testtable2 ADD COLUMN newinput VARCHAR(255);

/*
INSERT INTO TEST VALUES(1,2,3);
INSERT INTO TEST VALUES(1,2,3);
INSERT INTO TEST VALUES(1,2,3);
INSERT INTO TEST VALUES(1,2,3);
*/
