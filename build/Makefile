VERSION:=8u275-b01

.PHONY:build
build:
	docker build -t scmmanager/java-build:${VERSION} .

.PHONY:publish
publish: build
	docker push scmmanager/java-build:${VERSION}
