@echo off
set XDB=xdb
javadoc -d docs -author -version %XDB%\*.java %XDB%\addIns\*.java
javac %XDB%\*.java %XDB%\addIns\*.java
jar uvf packages\xdb.jar %XDB%\*.class %XDB%\addIns\*.class
del %XDB%\*.class
del %XDB%\addIns\*.class
