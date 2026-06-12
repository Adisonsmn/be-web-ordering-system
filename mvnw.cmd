@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF)
@REM Maven Wrapper startup batch script
@REM ----------------------------------------------------------------------------

@IF "%__MVNW_ARG0_NAME__%"=="" (SET "__MVNW_ARG0_NAME__=%~nx0")
@SET ___MVNW_UNFURL_BANNER=0

@SET "MAVEN_PROJECTBASEDIR=%~dp0"
@IF "%MAVEN_PROJECTBASEDIR:~-1%"=="\" (
  @SET "MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%"
)

@SET WRAPPER_JAR="%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
@SET WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

@SET DOWNLOAD_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar"

@FOR /F "usebackq tokens=1,2 delims==" %%A IN ("%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties") DO (
  @IF "%%A"=="distributionUrl" (SET "distributionUrl=%%B")
)

@SET JAVA_HOME_CANDIDATE=%JAVA_HOME%
@IF DEFINED JAVA_HOME_CANDIDATE (
  @IF NOT EXIST "%JAVA_HOME_CANDIDATE%\bin\java.exe" (
    @SET "JAVA_HOME_CANDIDATE="
  )
)

@IF NOT DEFINED JAVA_HOME_CANDIDATE (
  @FOR /F "usebackq delims=" %%i IN (`where java 2^>NUL`) DO (
    @SET "JAVA_EXE=%%i"
    @GOTO :found_java
  )
  @ECHO Error: JAVA_HOME not found and java not on PATH.
  @EXIT /B 1
  :found_java
  @SET "JAVA_HOME_CANDIDATE=%JAVA_EXE:~0,-14%"
)

@SET "JAVA_EXE=%JAVA_HOME_CANDIDATE%\bin\java.exe"

@IF NOT EXIST "%WRAPPER_JAR%" (
  @ECHO Downloading Maven Wrapper JAR...
  @"%JAVA_EXE%" -classpath "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper" org.apache.maven.wrapper.MavenWrapperDownloader %DOWNLOAD_URL% "%WRAPPER_JAR%"
)

@"%JAVA_EXE%" ^
  %MAVEN_OPTS% ^
  -classpath "%WRAPPER_JAR%" ^
  "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" ^
  %WRAPPER_LAUNCHER% %*
