#!/usr/bin/gnuplot -persist
set terminal png
set output "scratch/static/pngs/vary_nodes_avg_throughput.png"
set title "Average Throughput vs Nodes"
set xlabel "Nodes"
set ylabel "Average Throughput (Mbps)"
plot "scratch/static/vary_nodes.dat" using 1:5 title "Static WiFi High Rate" with linespoints
set output "scratch/static/pngs/vary_nodes_avg_packet_delivery_ratio.png"
set title "Average Packet Delivery Ratio vs Nodes"
set xlabel "Nodes"
set ylabel "Average Packet Delivery Ratio"
plot "scratch/static/vary_nodes.dat" using 1:6 title "Static WiFi High Rate" with linespoints