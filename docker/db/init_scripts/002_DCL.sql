CREATE USER 'developer'@'%' IDENTIFIED BY '12345';
GRANT USAGE ON *.* TO 'developer'@'%';
GRANT SELECT, UPDATE, DELETE, INSERT ON ticketing.* TO 'developer'@'%';
FLUSH PRIVILEGES;