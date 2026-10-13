@echo off
setlocal
cd /d "%~dp0"
if not defined JAVA_HOME set "JAVA_HOME=C:\Program Files\Java\jdk-25.0.4"
if not exist "%JAVA_HOME%\bin\java.exe" (
  echo Configure JAVA_HOME com a pasta do Java 25.
  exit /b 1
)
set "VITE_API_URL=/"
cd frontend
call npm.cmd run build
if errorlevel 1 exit /b 1
cd ..\backend
call mvnw.cmd -B -ntp -Pdemonstracao -DskipTests clean package
if errorlevel 1 exit /b 1
echo Pacote pronto em backend\target\techhelp-backend-0.0.1-SNAPSHOT.jar
echo Os testes nao sao executados por este empacotamento.
