@echo off
setlocal
cd /d "%~dp0"
if not defined JAVA_HOME set "JAVA_HOME=C:\Program Files\Java\jdk-25.0.4"
if not exist "%JAVA_HOME%\bin\java.exe" exit /b 1
if not exist "backend\target\techhelp-backend-0.0.1-SNAPSHOT.jar" (
  echo Execute preparar-demonstracao.cmd primeiro.
  exit /b 1
)
if /i "%~1"=="https" (
  set "TECHHELP_COOKIE_SECURE=true"
) else if /i "%~1"=="local" (
  set "TECHHELP_COOKIE_SECURE=false"
) else (
  echo Uso: iniciar-demonstracao.cmd local OU iniciar-demonstracao.cmd https
  exit /b 1
)
"%JAVA_HOME%\bin\java.exe" -jar "backend\target\techhelp-backend-0.0.1-SNAPSHOT.jar" --spring.profiles.active=demonstracao
