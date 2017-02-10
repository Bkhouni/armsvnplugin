@echo off

cls

set D=%~dp0
set D=%D%src\main\resources\svnwebclient

echo Polarion Subversion Web Client directory: %D%
echo.
echo The maven compile command makes a backup for all the jsp files
echo This command restores all the backups mentioned above
echo So, you should run this command ONCE (and only once) before make any jsp modification
echo.
echo Please, confirm that you want to restore all the current jsp files...
echo.
pause

echo Deleting .jsp files...

for /R %D% %%x in ("*.jsp") do del "%%x"

echo Restoring .bak files..
for /R %D% %%x in ("*.bak") do ren "%%x" *.


echo ...done!
