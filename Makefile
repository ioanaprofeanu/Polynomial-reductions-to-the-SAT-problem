build: retele reclame registre

run_retele:
	java Retele

run_reclame:
	java Reclame

run_registre:
	java Registre

retele: Retele.java Task.java Constants.java
	javac $^

reclame: Reclame.java Task.java Constants.java
	javac $^

registre: Registre.java Task.java Constants.java
	javac $^

clean:
	rm -f *.class

.PHONY: build clean
