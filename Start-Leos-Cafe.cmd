@echo off
cd /d "%~dp0"
if exist "data\store.json" goto existing
powershell -NoProfile -Command "$u=Read-Host 'Admin username (Enter for khurram.saeed)'; if([string]::IsNullOrWhiteSpace($u)){$u='khurram.saeed'}; $env:ADMIN_USER=$u; $env:ADMIN_PHONE=Read-Host 'Admin recovery mobile 03XXXXXXXXX'; $s=Read-Host 'Initial admin password' -AsSecureString; $b=[Runtime.InteropServices.Marshal]::SecureStringToBSTR($s); try{$env:ADMIN_PASSWORD=[Runtime.InteropServices.Marshal]::PtrToStringBSTR($b)} finally{[Runtime.InteropServices.Marshal]::ZeroFreeBSTR($b)}; & '.\node.exe' 'server.js'"
goto done
:existing
".\node.exe" server.js
:done
echo.
echo Server stopped. Press any key to close.
pause >nul
