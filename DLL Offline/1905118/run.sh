clear
g++ -std=c++17 -fsanitize=address 1905118_.cpp -o 1905118_.o
# g++ -std=c++17 -g 1905118_.cpp -o 1905118_.o
# g++ -std=c++17 1905118_.cpp -o 1905118_.o
# ./1905118_.o < 8_in.txt
for i in 1 2 3 .. 10
do
    ./1905118_.o < 4_in.txt
done