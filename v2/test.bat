@echo off
rem Compiles AlgoScope and runs all its tests (Windows). Needs JDK 21 or newer.
setlocal enabledelayedexpansion

rem Work from the folder of this script, wherever it is called from
cd /d "%~dp0"

rem Start from a clean output folder so no old class can hide a problem
if exist out rmdir /s /q out
mkdir out

rem Every .java file under src\ and test\, quoted and with / so paths with spaces work
(for /r src %%f in (*.java) do (set "p=%%f" & echo "!p:\=/!")) > out\sources.txt
(for /r test %%f in (*.java) do (set "p=%%f" & echo "!p:\=/!")) >> out\sources.txt

javac --release 21 -encoding UTF-8 -d out @out\sources.txt || exit /b 1

java -Djava.awt.headless=true -cp out algoscope.AllTests || exit /b 1
