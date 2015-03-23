# Makefile for building and executing a haddop job in a single node setup



JUNIT_JAR=/usr/share/java/junit4.jar


setup:
	mkdir classes test_classes lib docs
unset: 
	rm -rf classes test_classes lib docs
	rm files.txt *~

build: clean
	ls src/*.java > files.txt
	javac -d classes @files.txt
	jar -cvf lib/coalescingTools.jar -C classes/ .

clean:
	rm -rf classes/* lib/*

cleanTest:
	rm -rf test_classes/*

buildTest: build cleanTest
	ls test/*.java > files.txt
	javac -cp ${JUNIT_JAR}:lib/coalescingTools.jar -d test_classes  @files.txt
	jar -cvf lib/testing.jar -C test_classes/ .

test:  buildTest
	java -cp lib/coalescingTools.jar:${JUNIT_JAR}:lib/testing.jar org.junit.runner.JUnitCore ch.blogspot.prozakcode.coalesce.CoalescingTest
	find -name "*-*" -delete

javadoc: 
	ls src/*.java > files.txt
	ls test/*.java >> files.txt
	javadoc  -classpath ${JUNIT_JAR} -d docs @files.txt
