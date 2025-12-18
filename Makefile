.PHONY: run clean

run:
	mvn -q clean compile exec:java

clean:
	mvn -q clean
