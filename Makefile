
build:
	javac MyBot.java

check:
	python ./run.py --cmd "java MyBot" --round 1

run:
	java MyBot

clean:
	rm -f *.class
	rm -f *.log
