setlocal
for /F "delims== tokens=1,* eol=#" %%i in (.env) do set %%i=%%~j
java -jar zebra-proxy-0.0.1-SNAPSHOT.jar
endlocal
