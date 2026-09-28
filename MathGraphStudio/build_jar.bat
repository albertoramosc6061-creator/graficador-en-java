@echo off
chcp 65001 > nul
title Empaquetar JAR - MathGraph Studio Pro
echo ========================================================
echo   📦 Generando archivo JAR ejecutable independiente...
echo ========================================================
echo.

cd /d "%~dp0"

if not exist "bin" mkdir bin

echo [*] Compilando clases...
javac -encoding UTF-8 -d bin src\mathgraph\core\parser\*.java src\mathgraph\model\*.java src\mathgraph\theme\*.java src\mathgraph\ui\*.java src\mathgraph\Main.java
if errorlevel 1 (
    echo [ERROR] Fallo al compilar.
    pause
    exit /b 1
)

echo [*] Creando archivo JAR: MathGraphStudio.jar...
jar cfe MathGraphStudio.jar mathgraph.Main -C bin .

if errorlevel 1 (
    echo [ERROR] Fallo al crear JAR.
    pause
    exit /b 1
)

echo.
echo ========================================================
echo   ✅ EXITO: MathGraphStudio.jar creado correctamente!
echo   Puedes hacer doble clic en MathGraphStudio.jar o ejecutar.bat
echo ========================================================
echo.
pause
