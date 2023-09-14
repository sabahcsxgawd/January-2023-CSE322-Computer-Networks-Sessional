#!/usr/bin/gnuplot -persist

set terminal png
set output "scratch/static/pngs/vary_pps_avg_throughput.png"
set title "Average Throughput vs Packets_Per_Sec"
set xlabel "Packets_Per_Sec"
set ylabel "Average Throughput (Mbps)"
plot "scratch/static/vary_packets_per_sec.dat" using 3:5 title "Static WiFi High Rate" with linespoints
set output "scratch/static/pngs/vary_pps_avg_packet_delivery_ratio.png"
set title "Average Packet Delivery Ratio vs Packets_Per_Sec"
set xlabel "Packets_Per_Sec"
set ylabel "Average Packet Delivery Ratio"
plot "scratch/static/vary_packets_per_sec.dat" using 3:6 title "Static WiFi High Rate" with linespoints