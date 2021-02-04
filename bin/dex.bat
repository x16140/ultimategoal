@echo off
setlocal

REM Get File
for /f "delims=" %%F in ('dir ..\build\libs\*.jar /b /o-n') do set file=%%F

REM Call dx
call java -Xmx1024M -Xss1m -jar ".\libs\dx.jar" --dex --output="..\build\libs\programs.dex" "..\build\libs\%file%"