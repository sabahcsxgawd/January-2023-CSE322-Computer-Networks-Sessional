#!/usr/bin/gnuplot -persist

set terminal png
set output "scratch/mobile/pngs/vary_flows_avg_throughput.png"
set title "Average Throughput vs Flows"
set xlabel "Flows"
set ylabel "Average Throughput (Mbps)"
plot "scratch/mobile/vary_flows.dat" using 2:5 title "Mobile WiFi High Rate" with linespoints
set output "scratch/mobile/pngs/vary_flows_avg_packet_delivery_ratio.png"
set title "Average Packet Delivery Ratio vs Flows"
set xlabel "Flows"
set ylabel "Average Packet Delivery Ratio"
plot "scratch/mobile/vary_flows.dat" using 2:6 title "Mobile WiFi High Rate" with linespoints