@rem Gradle wrapper for Windows. If gradle\wrapper\gradle-wrapper.jar is missing,
@rem run `gradle wrapper --gradle-version 8.7` once (CI does this automatically).
@echo off
setlocal

set DIRNAME=%~dp0
set JAR=%DIRNAME%gradle\wrapper\gradle-wrapper.jar

if not exist "%JAR%" (
  where gradle >nul 2>nul
  if errorlevel 1 (
    echo ERROR: %JAR% not found and 'gradle' is not on PATH.
    echo Install Gradle 8.x once then re-run.
    exit /b 1
  )
  pushd "%DIRNAME%" && gradle wrapper --gradle-version 8.7 && popd
)

if defined JAVA_HOME (
  set JAVA_CMD=%JAVA_HOME%\bin\java.exe
) else (
  set JAVA_CMD=java
)

"%JAVA_CMD%" -classpath "%JAR%" org.gradle.wrapper.GradleWrapperMain %*
endlocal
