CREATE USER 'test_developer'@'%' IDENTIFIED BY '12345';
GRANT USAGE ON *.* TO 'test_developer'@'%';
GRANT CREATE, ALTER, DROP, SELECT, INSERT, UPDATE, DELETE ON ticketing_test.* TO 'test_developer'@'%';
FLUSH PRIVILEGES;