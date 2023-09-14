#! /usr/bin/bash
rm scratch/1905118_thrpt_*.dat
rm scratch/1905118_congestion_*.dat
rm scratch/1905118_*.png
gnome-terminal -- ./scratch/1905118_thrpt_btlnck.sh 1
gnome-terminal -- ./scratch/1905118_thrpt_btlnck.sh 2
gnome-terminal -- ./scratch/1905118_thrpt_btlnck.sh 3
gnome-terminal -- ./scratch/1905118_thrpt_plr.sh 1
gnome-terminal -- ./scratch/1905118_thrpt_plr.sh 2
gnome-terminal -- ./scratch/1905118_thrpt_plr.sh 3
gnome-terminal -- ./scratch/1905118_congestion.sh