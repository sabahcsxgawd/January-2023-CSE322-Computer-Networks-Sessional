#! /usr/bin/bash

clear
outDirPath="../out/production/FTP_1905118"
#rm -r "${serverSrcDirPath}/ClientDirs/"*
gnome-terminal --title="Server" -- java -cp "${outDirPath}" "Server/Server"