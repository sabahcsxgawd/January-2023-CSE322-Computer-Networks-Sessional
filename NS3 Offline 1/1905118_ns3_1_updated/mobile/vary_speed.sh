for i in $(seq 5 5 25)
do
    ./ns3 run "scratch/1905118_2.cc --nodes=20 --flows=40 --pps=100 --speed=$i --file=vary_speed"
done
./scratch/mobile/plot_vary_speed.sh