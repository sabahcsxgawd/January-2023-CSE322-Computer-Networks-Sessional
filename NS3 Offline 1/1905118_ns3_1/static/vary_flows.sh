for i in $(seq 10 10 50)
do
    ./ns3 run "scratch/1905118_1.cc --nodes=20 --flows=$i --pps=100 --cov=5 --file=vary_flows"
done
./scratch/static/plot_vary_flows.sh