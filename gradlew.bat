@echo off
setlocal
set GRADLE_HOME=%~dp0..\gradle-dist\gradle-8.12
set PATH=%GRADLE_HOME%\bin;%PATH%
call gradle %*
endlocal
