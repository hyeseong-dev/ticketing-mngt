CREATE USER 'developer'@'%' IDENTIFIED BY '12345';
GRANT CREATE, ALTER, DROP, SELECT, INSERT, UPDATE, DELETE ON ticketing.* TO 'developer'@'%';

CREATE USER 'developer'@'localhost' IDENTIFIED BY '12345';
GRANT CREATE, ALTER, DROP, SELECT, INSERT, UPDATE, DELETE ON ticketing.* TO 'developer'@'localhost';

FLUSH PRIVILEGES;
