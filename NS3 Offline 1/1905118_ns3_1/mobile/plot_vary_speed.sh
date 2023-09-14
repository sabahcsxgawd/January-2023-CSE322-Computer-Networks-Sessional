#!/usr/bin/gnuplot -persist

set terminal png
set output "scratch/mobile/pngs/vary_speed_avg_throughput.png"
set title "Average Throughput vs Speed"
set xlabel "Speed"
set ylabel "Average Throughput (Mbps)"
plot "scratch/mobile/vary_speed.dat" using 4:5 title "Mobile WiFi High Rate" with linespoints
set output "scratch/mobile/pngs/vary_speed_avg_packet_delivery_ratio.png"
set title "Average Packet Delivery Ratio vs Speed"
set xlabel "Speed"
set ylabel "Average Packet Delivery Ratio"
plot "scratch/mobile/vary_speed.dat" using 4:6 title "Mobile WiFi High Rate" with linespoints