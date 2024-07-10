@echo off
echo.
echo [信息] 使用Jar命令运行Modules-File工程。
echo.

cd %~dp0
cd ../fl-assistant-modules/fl-assistant-file/target

set JAVA_OPTS=-Xms512m -Xmx1024m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=512m

java -Dfile.encoding=utf-8 %JAVA_OPTS% -jar fl-assistant-modules-file.jar

cd bin
pause