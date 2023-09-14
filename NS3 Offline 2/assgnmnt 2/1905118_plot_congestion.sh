#!/usr/bin/gnuplot -persist
set terminal png
set terminal png size 1024,768
set output ARG1
set title "Congestion Window vs Time"
set xlabel "Time (s)"
set ylabel "Congestion Window Size (Bytes)"
plot ARG2 using 1:2 title ARG3 with linespoints, ARG4 using 1:2 title ARG5 with linespoints