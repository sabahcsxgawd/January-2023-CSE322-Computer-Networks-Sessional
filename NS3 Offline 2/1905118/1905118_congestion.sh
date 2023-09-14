#! /usr/bin/bash
algs=("TcpNewReno" "TcpWestwoodPlus" "TcpHighSpeed" "TcpAdaptiveReno")
alg1=${algs[0]}
for i in 1 2 3
do
    alg2=${algs[$i]}
    ./ns3 run "scratch/1905118.cc --PlotMode=3 --CongCntrlAlg2=$i"
    gnuplot -c "scratch/1905118_plot_congestion.sh" "scratch/1905118_congestion_$alg2"_"$alg1.png" "scratch/1905118_congestion_$alg2.dat" "$alg2" "scratch/1905118_congestion_$alg1.dat" "$alg1"

    if [ $i -gt 3 ]
    then
        rm scratch/1905118_congestion_$alg1.dat
    fi    
done