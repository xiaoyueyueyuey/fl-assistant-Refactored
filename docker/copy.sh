#!/bin/sh

# 复制项目的文件到对应docker路径，便于一键生成镜像。
usage() {
	echo "Usage: sh copy.sh"
	exit 1
}


# copy sql
echo "begin copy sql "
cp ../sql/ry_20231130.sql ./mysql/db
cp ../sql/ry_config_20231204.sql ./mysql/db

# copy html
echo "begin copy html "
cp -r ../fl-assistant-ui/dist/** ./nginx/html/dist


# copy jar
echo "begin copy fl-assistant-gateway "
cp ../fl-assistant-gateway/target/fl-assistant-gateway.jar ./ruoyi/gateway/jar

echo "begin copy fl-assistant-auth "
cp ../fl-assistant-auth/target/fl-assistant-auth.jar ./ruoyi/auth/jar

echo "begin copy fl-assistant-visual "
cp ../fl-assistant-visual/fl-assistant-monitor/target/fl-assistant-visual-monitor.jar  ./ruoyi/visual/monitor/jar

echo "begin copy fl-assistant-modules-system "
cp ../fl-assistant-modules/fl-assistant-system/target/fl-assistant-modules-system.jar ./ruoyi/modules/system/jar

echo "begin copy fl-assistant-modules-file "
cp ../fl-assistant-modules/fl-assistant-file/target/fl-assistant-modules-file.jar ./ruoyi/modules/file/jar

echo "begin copy fl-assistant-modules-job "
cp ../fl-assistant-modules/fl-assistant-job/target/fl-assistant-modules-job.jar ./ruoyi/modules/job/jar

echo "begin copy fl-assistant-modules-gen "
cp ../fl-assistant-modules/fl-assistant-gen/target/fl-assistant-modules-gen.jar ./ruoyi/modules/gen/jar

