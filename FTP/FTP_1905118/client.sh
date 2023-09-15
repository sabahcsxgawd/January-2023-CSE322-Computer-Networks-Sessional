#! /usr/bin/bash

clear

clientSrcDirPath="${PWD}/src/Client"
outDirPath="${PWD}/out/production/FTP_1905118"

#TODO rm Downloads of Clients

javac -d "${outDirPath}" "${clientSrcDirPath}/"*.java
gnome-terminal --title="Client" -- java -cp "${outDirPath}" "Client/Client"