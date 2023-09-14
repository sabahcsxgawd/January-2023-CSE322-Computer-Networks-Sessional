for i in $(seq 20 20 100)
do
    flows=`expr $i / 2`
    ./ns3 run "scratch/1905118_2.cc --nodes=$i --flows=$flows --pps=100 --speed=20 --file=vary_nodes"
done
./scratch/mobile/plot_vary_nodes.sh