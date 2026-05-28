@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF)
@REM Maven Wrapper startup batch script
@REM ----------------------------------------------------------------------------
@IF "%__MVNW_ARG0_NAME__%"=="" (SET "__MVNW_ARG0_NAME__=%~nx0")
@SET ___MVNW_SCRIPT_DIR=%~dp0
@SET "___MVNW_SCRIPT_DIR_NO_SLASH=%___MVNW_SCRIPT_DIR%"
@IF "%___MVNW_SCRIPT_DIR_NO_SLASH:~-1%"=="\" SET "___MVNW_SCRIPT_DIR_NO_SLASH=%___MVNW_SCRIPT_DIR_NO_SLASH:~0,-1%"
@IF "%MVNW_VERBOSE%"=="" (@SET MVNW_VERBOSE=false)
@IF "%MVNW_USERNAME%"=="" (@SET MVNW_USERNAME=)
@IF "%MVNW_PASSWORD%"=="" (@SET MVNW_PASSWORD=)

@SET _JAVACMD=%JAVACMD%
@IF "%_JAVACMD%"=="" (
  @IF NOT "%JAVA_HOME%"=="" (@SET "_JAVACMD=%JAVA_HOME%\bin\java.exe")
)
@IF "%_JAVACMD%"=="" (@SET _JAVACMD=java)

@SET WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain
@SET WRAPPER_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar

@SET DOWNLOAD_URL=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.9/apache-maven-3.9.9-bin.zip

@SET WRAPPER_JAR=%___MVNW_SCRIPT_DIR%.mvn\wrapper\maven-wrapper.jar

@IF EXIST "%WRAPPER_JAR%" (
  @CALL :sub_call_wrapper_jar %*
  @GOTO :EOF
)

@ECHO Downloading Maven Wrapper...
@"%_JAVACMD%" -classpath "%WRAPPER_JAR%" "-Dmaven.multiModuleProjectDirectory=%___MVNW_SCRIPT_DIR_NO_SLASH%" %WRAPPER_LAUNCHER% %MAVEN_CMD_LINE_ARGS% %*

:sub_call_wrapper_jar
"%_JAVACMD%" -classpath "%WRAPPER_JAR%" "-Dmaven.multiModuleProjectDirectory=%___MVNW_SCRIPT_DIR_NO_SLASH%" %WRAPPER_LAUNCHER% %*
@EXIT /B %ERRORLEVEL%
