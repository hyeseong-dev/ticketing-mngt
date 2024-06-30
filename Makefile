# Variables
DEV_DOCKER_COMPOSE_FILE = dev-compose.yml
AWS_REGION = ap-northeast-2
ECR_REGISTRY = 471112705943.dkr.ecr.ap-northeast-2.amazonaws.com
APP_IMAGE_NAME = dev-ticketing
REGISTRY_NAME = ticketing
DB_IMAGE_NAME = ticketing-db

# Targets
.PHONY: build
build:
	docker-compose -f $(DEV_DOCKER_COMPOSE_FILE) build

.PHONY: login
login:
	aws ecr get-login-password --region $(AWS_REGION) | docker login --username AWS --password-stdin $(ECR_REGISTRY)

.PHONY: tag-app
tag-app:
	docker tag $(APP_IMAGE_NAME):latest $(ECR_REGISTRY)/$(REGISTRY_NAME):latest

.PHONY: tag-db
tag-db:
	docker tag $(DB_IMAGE_NAME):latest $(ECR_REGISTRY)/$(DB_IMAGE_NAME):latest

.PHONY: push-app
push-app: tag-app
	docker push $(ECR_REGISTRY)/$(REGISTRY_NAME):latest

.PHONY: push-db
push-db: tag-db
	docker push $(ECR_REGISTRY)/$(DB_IMAGE_NAME):latest

.PHONY: clean-unused
clean-unused:
	docker image prune -f

.PHONY: clean-dangling
clean-dangling:
	docker image prune -f --filter "dangling=true"

.PHONY: stop-services
stop-services:
	docker-compose -f $(DEV_DOCKER_COMPOSE_FILE) down

.PHONY: remove-images
remove-images:
	docker rmi -f $(APP_IMAGE_NAME):latest $(DB_IMAGE_NAME):latest || true
	docker rmi -f $(ECR_REGISTRY)/$(APP_IMAGE_NAME):latest $(ECR_REGISTRY)/$(DB_IMAGE_NAME):latest || true

.PHONY: stop-and-clean
stop-and-clean: stop-services remove-images clean-unused clean-dangling

.PHONY: clean-registry-image
clean-registry-image:
	docker rmi -f $(ECR_REGISTRY)/$(REGISTRY_NAME):latest

.PHONY: all
all: build login push-app
