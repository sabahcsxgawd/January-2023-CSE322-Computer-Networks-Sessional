for i in $(seq 1 1 5)
do
    ./ns3 run "scratch/1905118_1.cc --nodes=20 --flows=40 --pps=100 --cov=$i --file=vary_coverage_area"
done
./scratch/static/plot_vary_coverage_area.sh