@ECHO OFF
SETLOCAL

SET APP_HOME=%~dp0
SET CLASSPATH=%APP_HOME%gradle\wrapper\gradle-wrapper.jar

IF NOT EXIST "%CLASSPATH%" (
    ECHO ERROR: Gradle wrapper JAR not found: %CLASSPATH%
    EXIT /B 1
)

"%JAVA_HOME%\bin\java.exe" %JAVA_OPTS% %GRADLE_OPTS% -Dorg.gradle.appname=gradlew -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*

ENDLOCAL
