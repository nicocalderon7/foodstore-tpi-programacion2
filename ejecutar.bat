@echo off
REM Compila y ejecuta Food Store en Windows (sin Maven ni Gradle).
REM Usa rutas relativas para funcionar aunque la carpeta tenga espacios o acentos.
cd /d "%~dp0"

if not exist lib\*.jar echo [!] No hay ningun driver JDBC en lib\. Copie el .jar de mysql-connector-j en lib\.

if exist bin rmdir /s /q bin
javac -encoding UTF-8 -sourcepath src -d bin src\integrado\prog2\Main.java
if errorlevel 1 (
    echo [ERROR] No se pudo compilar el proyecto.
    pause
    exit /b 1
)
xcopy /e /i /q src\META-INF bin\META-INF > nul
java -cp "bin;lib\*" integrado.prog2.Main
pause
