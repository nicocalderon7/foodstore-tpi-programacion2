@echo off
REM Compila y ejecuta Food Store en Windows (sin Maven ni Gradle).
cd /d "%~dp0"
chcp 65001 > nul

if not exist lib\*.jar echo [!] No hay ningun driver JDBC en lib\. Descargue mysql-connector-j-X.Y.Z.jar y copielo en lib\.

if exist bin rmdir /s /q bin
dir /s /b src\*.java > fuentes.txt
javac -encoding UTF-8 -d bin @fuentes.txt
if errorlevel 1 (
    del fuentes.txt
    pause
    exit /b 1
)
del fuentes.txt
xcopy /e /i /q src\META-INF bin\META-INF > nul
REM En Windows el separador del classpath es punto y coma
java -cp "bin;lib/*" integrado.prog2.Main
pause
