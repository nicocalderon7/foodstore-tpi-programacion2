#!/usr/bin/env bash
# Compila y ejecuta Food Store en Linux / macOS (sin Maven ni Gradle).
set -e
cd "$(dirname "$0")"

if ! ls lib/*.jar >/dev/null 2>&1; then
    echo "[!] No hay ningun driver JDBC en lib/. Descargue mysql-connector-j-X.Y.Z.jar y copielo en lib/."
fi

rm -rf bin
javac -encoding UTF-8 -d bin $(find src -name "*.java")
cp -r src/META-INF bin/META-INF
java -cp "bin:lib/*" integrado.prog2.Main
