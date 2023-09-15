#! /usr/bin/bash

clear

serverSrcDirPath="${PWD}/src/Server"
outDirPath="${PWD}/out/production/FTP_1905118"

rm -r "${outDirPath}/Server"
#rm -r "${serverSrcDirPath}/ClientDirs/"*

javac -d "${outDirPath}" "${serverSrcDirPath}/"*.java
gnome-terminal --title="Server" -- java -cp "${outDirPath}" "Server/Server"