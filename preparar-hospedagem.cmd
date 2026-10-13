@echo off
setlocal
cd /d "%~dp0"
rem Reutiliza o perfil Maven que inclui React e Spring no mesmo JAR.
call preparar-demonstracao.cmd
if errorlevel 1 exit /b 1
cd /d "%~dp0"
echo.
echo Pacote de hospedagem preparado. No servidor, ativar o perfil Spring hospedagem.
echo Arquivo: backend\target\techhelp-backend-0.0.1-SNAPSHOT.jar
echo Confira o hash SHA-256 abaixo antes e depois da transferencia:
certutil -hashfile "backend\target\techhelp-backend-0.0.1-SNAPSHOT.jar" SHA256
if errorlevel 1 exit /b 1
