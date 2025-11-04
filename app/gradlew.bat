@ECHO OFF
SET APP_HOME=%~dp0
SET DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m"

IF EXIST "%APP_HOME%\gradle\wrapper\gradle-wrapper.jar" GOTO runWrapper

IF NOT "%GRADLE_HOME%"=="" GOTO useGradleHome
WHERE gradle >NUL 2>&1
IF %ERRORLEVEL% EQU 0 (
  gradle -p "%APP_HOME%" %*
  EXIT /B %ERRORLEVEL%
)
ECHO ERROR: Gradle wrapper JAR not found and no Gradle installation detected.
ECHO Install Gradle 8.2.1 and run "gradle wrapper".
EXIT /B 1

:useGradleHome
"%GRADLE_HOME%\bin\gradle" -p "%APP_HOME%" %*
EXIT /B %ERRORLEVEL%

:runWrapper
SET JAVA_EXE=
IF DEFINED JAVA_HOME (
  IF EXIST "%JAVA_HOME%\bin\java.exe" SET JAVA_EXE="%JAVA_HOME%\bin\java.exe"
)
IF NOT DEFINED JAVA_EXE SET JAVA_EXE=java
%JAVA_EXE% %DEFAULT_JVM_OPTS% %GRADLE_OPTS% -classpath "%APP_HOME%\gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*
