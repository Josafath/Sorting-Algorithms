@echo off
rem Compiles and opens AlgoScope (Windows). Needs JDK 21 or newer.
setlocal enabledelayedexpansion

rem Work from the folder of this script, wherever it is called from
cd /d "%~dp0"

if exist out\app rmdir /s /q out\app
mkdir out\app

rem Only the app, not the tests; quoted and with / so paths with spaces work
(for /r src %%f in (*.java) do (set "p=%%f" & echo "!p:\=/!")) > out\app-sources.txt

javac --release 21 -encoding UTF-8 -d out\app @out\app-sources.txt || exit /b 1

rem javaw opens the window without keeping a console open
start "" javaw -cp out\app algoscope.Main
