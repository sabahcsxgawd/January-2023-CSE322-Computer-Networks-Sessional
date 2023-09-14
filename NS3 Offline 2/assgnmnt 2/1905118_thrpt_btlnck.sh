#! /usr/bin/bash

algs=("TcpNewReno" "TcpWestwoodPlus" "TcpHighSpeed" "TcpAdaptiveReno")
alg1=${algs[0]}
alg2=${algs[$1]}
for i in $(seq 10 10 300)
do
    ./ns3 run "scratch/1905118.cc --BottleneckDataRate=$i --PlotMode=1 --CongCntrlAlg2=$1"
done
gnuplot -c "scratch/1905118_plot_thrpt_btlnck.sh" "scratch/1905118_thrpt_btlnk_datarate_$alg2"_"$alg1.png" "scratch/1905118_thrpt_btlnk_datarate_$alg2"_"$alg1.dat" "$alg2" "$alg1"
gnuplot -c "scratch/1905118_plot_jain_btlnck.sh" "scratch/1905118_jain_btlnk_datarate_$alg2"_"$alg1.png" "scratch/1905118_thrpt_btlnk_datarate_$alg2"_"$alg1.dat" "$alg2" "$alg1"