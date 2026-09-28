@echo off
chcp 65001 > nul
title MathGraph Studio Pro
echo ========================================================
echo   ⚡ MathGraph Studio Pro - Iniciando aplicacion...
echo ========================================================
echo.

cd /d "%~dp0"

:: Si no existe bin, compilar primero
if not exist "bin\mathgraph\Main.class" (
    echo [*] Compilando codigo fuente Java...
    if not exist "bin" mkdir bin
    javac -encoding UTF-8 -d bin src\mathgraph\core\parser\*.java src\mathgraph\model\*.java src\mathgraph\theme\*.java src\mathgraph\ui\*.java src\mathgraph\Main.java
    if errorlevel 1 (
        echo [ERROR] Error durante la compilacion. Asegurate de tener JDK instalado.
        pause
        exit /b 1
    )
    echo [*] Compilacion completada con exito.
)

echo [*] Ejecutando MathGraph Studio...
start javaw -cp bin mathgraph.Main
exit
