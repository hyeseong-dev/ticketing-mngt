CREATE USER 'test_developer'@'%' IDENTIFIED WITH mysql_native_password BY '12345';
GRANT ALL PRIVILEGES ON ticketing_test.* TO 'test_developer'@'%';

CREATE USER 'test_developer'@'localhost' IDENTIFIED WITH mysql_native_password BY '12345';
GRANT ALL PRIVILEGES ON ticketing_test.* TO 'test_developer'@'localhost';

CREATE USER 'test_developer'@'_gateway' IDENTIFIED WITH mysql_native_password BY '12345';
GRANT ALL PRIVILEGES ON ticketing_test.* TO 'test_developer'@'_gateway';

FLUSH PRIVILEGES;
