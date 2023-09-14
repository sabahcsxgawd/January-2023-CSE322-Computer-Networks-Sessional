#!/usr/bin/gnuplot -persist
set terminal png
set terminal png size 1024,768
set output ARG1
set title "Throughput vs Packet Loss Rate"
set xlabel "Packet Loss Rate"
set ylabel "Throughput (kbps)"
plot ARG2 using 1:3 title ARG3 with linespoints, ARG2 using 1:2 title ARG4 with linespoints