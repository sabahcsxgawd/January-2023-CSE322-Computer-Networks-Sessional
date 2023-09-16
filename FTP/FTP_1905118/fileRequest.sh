#! /usr/bin/bash

clear

fileReqSrcDirPath="${PWD}/src/FileRequest"
outDirPath="${PWD}/out/production/FTP_1905118"

rm -r "${outDirPath}/FileRequest"

javac -d "${outDirPath}" "${fileReqSrcDirPath}/"*.java