# Use the official MySQL 8.0 image from Docker Hub
FROM mysql:8.0.31

# Copy custom configuration file to the container
COPY mysql/my.cnf /etc/mysql/my.cnf

# Copy initialization scripts to the container
COPY init_scripts /docker-entrypoint-initdb.d

# Change permissions of the SQL files
RUN chmod 644 /docker-entrypoint-initdb.d/*.sql

# Expose the default MySQL port
EXPOSE 3306
