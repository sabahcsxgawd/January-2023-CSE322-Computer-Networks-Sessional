#! /usr/bin/bash

clear
outDirPath="${PWD}/out/production/FTP_1905118"
javac -d "${outDirPath}" `find . -name *.java`