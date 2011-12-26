#
# RestyleOdt --  
#
XERCES=/glp/code/xerces-2_9_0
GETOPT=/glp/code/getopt
BINDIR=/glp/src/OO
CP=${XERCES}/xercesImpl.jar:${XERCES}/serializer.jar:${GETOPT}/java-getopt-1.0.13.jar:${CLASSPATH}:${BINDIR}
#OPTS=-verbose -Xlint
OPTS=-Xlint
RUN=java -jar RestyleOdt.jar 
CLASSES=NodeTyper.class OdtRestyle.class RestyleOdt.class OdtRebalance.class PropertyPrinter.class ReadMapFile.class

%.class:%.java ; javac $<

all: RestyleOdt.jar test

RestyleOdt.jar: RestyleOdt.mf ${CLASSES} 
	jar -cvfm RestyleOdt.jar RestyleOdt.mf *.class

RestyleOdt.class: RestyleOdt.java 
	javac ${OPTS} -classpath ${CP} RestyleOdt.java

test:
	#${RUN} -v Tests/1-introduction.xml  > 1-comparison.xml
	#${RUN} -d --rebalance --inserted-heading 'T' -v  \
	#	Tests/badhier.xml 2>&1 | more
	${RUN} -d --restyle --paragraph-map para.map \
		-v Tests/1-introduction.xml 2>&1 | more


tests: 
	(${RUN} || echo "failed properly"; \
	 ${RUN} -V || echo "failed properly"; \
	 ${RUN} -p Tests/1-introduction.xml; \
		) 2>&1 | more

install:
	cp RestyleOdt ${HOME}/bin
	cp *.class ${HOME}/lib

push: tar
	scp ../RestyleOdt.tar.gz drcb@dev:

tar: clean
	cd ..; tar cvf RestyleOdt.tar ./RestyleOdt; gzip RestyleOdt.tar

clean:
	rm -f *.class
