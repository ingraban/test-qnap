@ECHO OFF
SETLOCAL
set MVNW_DIR=%~dp0
if defined JAVA_HOME (
  "%JAVA_HOME%\bin\java" -jar "%MVNW_DIR%\.mvn\wrapper\maven-wrapper.jar" %*
) else (
  java -jar "%MVNW_DIR%\.mvn\wrapper\maven-wrapper.jar" %*
)