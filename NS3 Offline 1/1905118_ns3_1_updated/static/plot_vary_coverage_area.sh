#!/usr/bin/gnuplot -persist

set terminal png
set output "scratch/static/pngs/vary_coverage_area_avg_throughput.png"
set title "Average Throughput vs Coverage_Area"
set xlabel "Coverage_Area"
set ylabel "Average Throughput (Mbps)"
plot "scratch/static/vary_coverage_area.dat" using 4:5 title "Static WiFi High Rate" with linespoints
set output "scratch/static/pngs/vary_coverage_area_avg_packet_delivery_ratio.png"
set title "Average Packet Delivery Ratio vs Coverage_Area"
set xlabel "Coverage_Area"
set ylabel "Average Packet Delivery Ratio"
plot "scratch/static/vary_coverage_area.dat" using 4:6 title "Static WiFi High Rate" with linespoints