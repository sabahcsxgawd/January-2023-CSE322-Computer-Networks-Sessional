#! /usr/bin/bash

clear
outDirPath="${PWD}/out/production/FTP_1905118"

#TODO rm Downloads of Clients
gnome-terminal --title="Client" -- java -cp "${outDirPath}" "Client/Client"