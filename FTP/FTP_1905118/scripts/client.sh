#! /usr/bin/bash

clear
outDirPath="./out/production/FTP_1905118"

#TODO rm Downloads of Clients
cd ..
gnome-terminal --title="Client" -- java -cp "${outDirPath}" "Client/Client"