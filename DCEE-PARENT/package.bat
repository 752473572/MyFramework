@echo off  
set localdir=%~dp0
call mvn clean package -Dmaven.test.skip=true
pause 