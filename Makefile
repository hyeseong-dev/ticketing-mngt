# Makefile

# Variables
DOCKER_COMPOSE_CMD = docker-compose -f compose.yml -f ./docker/db/compose.yml

# Targets
.PHONY: up
up:
	$(DOCKER_COMPOSE_CMD) up --build

.PHONY: down
down:
	$(DOCKER_COMPOSE_CMD) down -v

.PHONY: restart
restart: down up

.PHONY: logs
logs:
	$(DOCKER_COMPOSE_CMD) logs -f

.PHONY: ps
ps:
	$(DOCKER_COMPOSE_CMD) ps

.PHONY: clean
clean:
	docker image prune -f

.PHONY: clean-dangling
clean-dangling:
	docker image prune -f --filter "dangling=true"

.PHONY: clean-all
clean-all:
	docker image prune -a -f
