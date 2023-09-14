#include <bits/stdc++.h>
#include <windows.h>

using namespace std;

char Map[10][20] = {"###################",
                    "#@................#",
                    "#.................#",
                    "#.................#",
                    "#.................#",
                    "#.................#",
                    "#.................#",
                    "#.................#",
                    "#.................#",
                    "###################"};


string asciiTo8BitBinary(char c) {
    string temp = "";
    int size = 8;
    while(size--) {
        temp = (char)((c & 1) + '0') + temp;
        c >>= 1;
    }
    return temp;
}

int main()
{
    HANDLE hConsole = GetStdHandle(STD_OUTPUT_HANDLE);    
    // const int rows = 10, columns = 20;
    // for (int i = 0; i < rows; ++i)
    // {
    //     for (int j = 0; j < columns; ++j)
    //     {
    //         if (Map[i][j] == '@')
    //         {
    //             SetConsoleTextAttribute(hConsole, 2);
    //             cout << Map[i][j];
    //             SetConsoleTextAttribute(hConsole, 7);
    //         }
    //         else
    //         {
    //             SetConsoleTextAttribute(hConsole, 4);
    //             cout << Map[i][j];
    //             SetConsoleTextAttribute(hConsole, 7);
    //         }
    //     }
    //     cout << '\n';
    // }
    SetConsoleTextAttribute(hConsole, 11); // cyan
    cout << asciiTo8BitBinary('a');
    SetConsoleTextAttribute(hConsole, 10); // green
    cout << asciiTo8BitBinary('a');
    SetConsoleTextAttribute(hConsole, 12); // red
    cout << asciiTo8BitBinary('a');
    SetConsoleTextAttribute(hConsole, 7); // white
    return 0;
}