all: aar

aar: clean
	./gradlew assembleRelease --no-build-cache && \
	mkdir -p release/ && \
	cp piano-dispatcher/build/outputs/aar/piano-dispatcher-release.aar release/ && \
	cp LICENSE release/

clean:
	./gradlew clean
	rm -rf release/

test:
	./gradlew testDebugUnitTest

lint:
	./gradlew lintDebug

ci: clean lint test aar

publish: aar
	./gradlew piano-dispatcher:publish

.PHONY: test aar

