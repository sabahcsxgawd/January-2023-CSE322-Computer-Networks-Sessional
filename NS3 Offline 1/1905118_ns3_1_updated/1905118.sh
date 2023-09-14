cd ./scratch/static
rm -r *.dat
cd ./pngs
rm -r *.png
cd ../../mobile
rm -r *.dat
cd ./pngs
rm -r *.png
cd ../../../
./ns3 build
gnome-terminal -- ./scratch/static/vary_nodes.sh
gnome-terminal -- ./scratch/static/vary_flows.sh
gnome-terminal -- ./scratch/static/vary_packets_per_sec.sh
gnome-terminal -- ./scratch/static/vary_coverage_area.sh
gnome-terminal -- ./scratch/mobile/vary_nodes.sh
gnome-terminal -- ./scratch/mobile/vary_flows.sh
gnome-terminal -- ./scratch/mobile/vary_packets_per_sec.sh
gnome-terminal -- ./scratch/mobile/vary_speed.sh