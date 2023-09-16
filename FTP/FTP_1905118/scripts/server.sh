#! /usr/bin/bash

clear
outDirPath="./out/production/FTP_1905118"
#rm -r "${serverSrcDirPath}/ClientDirs/"*
./build.sh
cd ..
gnome-terminal --title="Server" -- java -cp "${outDirPath}" "Server/Server"