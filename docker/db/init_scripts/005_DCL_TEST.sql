CREATE USER 'test_developer'@'%' IDENTIFIED BY '12345';
GRANT CREATE, ALTER, DROP, SELECT, INSERT, UPDATE, DELETE ON ticketing_test.* TO 'test_developer'@'%';

CREATE USER 'test_developer'@'localhost' IDENTIFIED BY '12345';
GRANT CREATE, ALTER, DROP, SELECT, INSERT, UPDATE, DELETE ON ticketing_test.* TO 'test_developer'@'localhost';

FLUSH PRIVILEGES;
