for i in $(seq 100 100 500)
do
    ./ns3 run "scratch/1905118_2.cc --nodes=20 --flows=40 --pps=$i --speed=10 --file=vary_packets_per_sec"
done
./scratch/mobile/plot_vary_packets_per_sec.sh