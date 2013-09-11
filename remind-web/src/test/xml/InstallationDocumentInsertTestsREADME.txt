1. install mysql db, create database remind

create test table use following command:

CREATE TABLE IF NOT EXISTS `testtable` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `test_date` date NOT NULL,
  `insert_timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_bin AUTO_INCREMENT=11 ;

2. in RemInD webapplication add following configuration

DatabaseTarget
environment: DEV
sid: REMINDMYSQL
jdbcUrl=jdbc:mysql://localhost/remind
jdbcDriver=com.mysql.jdbc.Driver
validationStatement=SELECT test_date FROM testtable

DatabaseTargetUser
environment: DEV
sid: REMINDMYSQL
schema: remindtest
user: root
password: stilgero

3. copy files InstallationDocumentInsertFailureTest.sql, InstallationDocumentInsertSuccessTest.sql in directory /tmp
If you want use other directory, adjust in InstallationDocumentInsertFailureTest.xml,InstallationDocumentInsertSuccessTest.xml source/fileSystem/Path

4. Validate and deploy InstallationDocumentInsertFailureTest.xml,InstallationDocumentInsertSuccessTest.xml in RemInD webapplication