for i in $(seq 100 100 500)
do
    ./ns3 run "scratch/1905118_1.cc --nodes=20 --flows=40 --pps=$i --cov=5 --file=vary_packets_per_sec"
done
./scratch/static/plot_vary_packets_per_sec.sh