for i in $(seq 10 10 50)
do
    ./ns3 run "scratch/1905118_2.cc --nodes=20 --flows=$i --pps=100 --speed=20 --file=vary_flows"
done
./scratch/mobile/plot_vary_flows.sh