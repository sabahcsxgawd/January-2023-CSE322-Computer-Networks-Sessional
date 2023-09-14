#!/usr/bin/gnuplot -persist
set terminal png
set terminal png size 1024,768
set output ARG1
set title "Fairness Index vs Packet Loss Rate"
set xlabel "Packet Loss Rate"
set ylabel "Fairness Index"
plot ARG2 using 1:4 title "" with linespoints