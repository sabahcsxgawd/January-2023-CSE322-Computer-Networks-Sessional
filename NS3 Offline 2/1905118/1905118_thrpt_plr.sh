#! /usr/bin/bash

algs=("TcpNewReno" "TcpWestwoodPlus" "TcpHighSpeed" "TcpAdaptiveReno")
alg1=${algs[0]}
alg2=${algs[$1]}
for i in $(seq 2.0 0.2 6.0)
do
    ./ns3 run "scratch/1905118.cc --PacketLossExponent=$i --PlotMode=2 --CongCntrlAlg2=$1"
done
gnuplot -c "scratch/1905118_plot_thrpt_plr.sh" "scratch/1905118_thrpt_pktLossRate_$alg2"_"$alg1.png" "scratch/1905118_thrpt_pktLossRate_$alg2"_"$alg1.dat" "$alg2" "$alg1"