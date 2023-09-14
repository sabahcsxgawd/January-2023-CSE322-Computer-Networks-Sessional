for i in $(seq 20 20 100)
do
    flows=`expr $i / 2`
    ./ns3 run "scratch/1905118_1.cc --nodes=$i --flows=$flows --pps=100 --cov=5 --file=vary_nodes"
done
./scratch/static/plot_vary_nodes.sh