@echo off
setlocal
cd /d "%~dp0"
if not exist build mkdir build
javac -encoding UTF-8 -cp "lib\mysql-connector-j-26.10.0.jar" -d build *.java
if errorlevel 1 (
    echo.
    echo Compilation failed. Check the errors above.
    pause
    exit /b 1
)
java -cp "build;lib\mysql-connector-j-26.10.0.jar" Main
if errorlevel 1 (
    echo.
    echo GamePulse exited with an error.
    pause
)
endlocal
