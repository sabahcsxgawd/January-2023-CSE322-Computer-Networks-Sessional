#include <bits/stdc++.h>
// #include <windows.h>
using namespace std;

#define COLOR_WHITE 7
#define COLOR_GREEN 10
#define COLOR_CYAN 11
#define COLOR_RED 12

// HANDLE hConsole = GetStdHandle(STD_OUTPUT_HANDLE);

inline bool isPow2(int n)
{
    return (((n) & ((n)-1)) == 0);
}

inline char xorChar(char c1, char c2)
{
    return (c1 == c2) ? '0' : '1';
}

void pad(string &s, int m)
{
    string temp(((m - (s.size() % m)) % m), '~');
    s = s + temp;
}

string asciiTo8BitBinary(char c)
{
    string temp = "";
    int size = 8;
    while (size--)
    {
        temp = (char)((c & 1) + '0') + temp;
        c >>= 1;
    }
    return temp;
}

vector<string> createDataBlock(const string &data, int m)
{
    vector<string> temp;
    string otherTemp = "";
    for (int i = 0; i < data.size(); i++)
    {
        otherTemp += asciiTo8BitBinary(data[i]);
        if (i % m == m - 1)
        {
            temp.emplace_back(otherTemp);
            otherTemp.clear();
        }
    }

    return temp;
}

void printDataBlock(vector<string> &dataBlock, int color = COLOR_WHITE, void* arg=NULL)
{
    if (color == COLOR_WHITE)
    {
        for (string s : dataBlock)
        {
            cout << s << '\n';
        }
    }

    // -1 means serialize print of data in column major manner
    else if (color == -1)
    {
        int row = dataBlock.size();
        assert(dataBlock.size() > 0);
        int col = dataBlock[0].size();

        for (int i = 0; i < col; i++)
        {
            for (int j = 0; j < row; j++)
            {
                cout << dataBlock[j][i];
            }
        }
    }

    else if (color == COLOR_GREEN)
    {
        for (string s : dataBlock)
        {
            int currPow2 = 1;
            for (int i = 0; i < s.size(); i++)
            {
                if (i == currPow2 - 1)
                {
                    // SetConsoleTextAttribute(hConsole, color);
                    currPow2 <<= 1;
                }
                cout << s[i];
                // SetConsoleTextAttribute(hConsole, COLOR_WHITE);
            }
            cout << '\n';
        }
    }

    else if(color == COLOR_RED) {
        vector<bool> errorBitMap = *((vector<bool> *)arg);
        for(int i = 0; i < dataBlock.size(); i++) {
            for(int j = 0; j < dataBlock[0].size(); j++) {
                if(errorBitMap[(j * dataBlock.size()) + i]) {
                    // SetConsoleTextAttribute(hConsole, COLOR_RED);
                }
                cout << dataBlock[i][j];
                // SetConsoleTextAttribute(hConsole, COLOR_WHITE);
            }
            cout << '\n';
        }
    }
}

int getRedundancyBitCount(int m)
{
    int r = 0;
    while ((m + r + 1) > (1 << r))
    {
        r++;
    }
    return r;
}

vector<string> getCheckBitDataBlock(const vector<string> &data)
{
    vector<string> temp;
    string otherTemp;
    assert(data.size() > 0);
    int r = getRedundancyBitCount(data[0].size());
    for (string s : data)
    {
        int dataIndex = 0;
        otherTemp.resize(1 + r + s.size(), '0');
        for (int i = 1; i < otherTemp.size(); i++)
        {
            if (!isPow2(i))
            {
                otherTemp[i] = s[dataIndex++];
                int k = 1, temp_i = i;
                while (temp_i)
                {
                    if (temp_i & 1)
                    {
                        otherTemp[k] = xorChar(otherTemp[k], otherTemp[i]);
                    }
                    k <<= 1;
                    temp_i >>= 1;
                }
            }
        }
        otherTemp.erase(0, 1);
        temp.emplace_back(otherTemp);
        otherTemp.clear();
    }
    return temp;
}

string serializeData(const vector<string> &dataBlock)
{
    int row = dataBlock.size();
    assert(dataBlock.size() > 0);
    int col = dataBlock[0].size();
    string temp = "";

    for (int i = 0; i < col; i++)
    {
        for (int j = 0; j < row; j++)
        {
            temp += dataBlock[j][i];
        }
    }
    return temp;
}

string subtractModulo2(const string &d, const string &g)
{
    string temp = d;
    if (d[0] == '1')
    {
        for (int i = 1; i < g.size(); i++)
        {
            temp[i] = xorChar(temp[i], g[i]);
        }
    }
    temp.erase(0, 1);
    return temp;
}

string getRemainderCRC(const string &M, const string &G)
{
    assert(M.size() > G.size());
    // append r (degree of G(x)) 0 bits to M(x)
    string tempM(G.size() - 1, '0'); // always true because the MSB of G(x) is 1; asserted
    tempM = M + tempM;
    string quotient, tempDividend = tempM.substr(0, G.size());
    int i = G.size();
    while (i <= tempM.size())
    {
        quotient += tempDividend[0];
        tempDividend = subtractModulo2(tempDividend, G);
        if (i < tempM.size())
        {
            tempDividend += tempM[i];
        }
        i++;
    }
    return tempDividend;
}

string verifyCorrectNessReceivedFrame(const string &rf, const string &G)
{
    string quotient, tempDividend = rf.substr(0, G.size());
    int i = G.size();
    while (i <= rf.size())
    {
        quotient += tempDividend[0];
        tempDividend = subtractModulo2(tempDividend, G);
        if (i < rf.size())
        {
            tempDividend += rf[i];
        }
        i++;
    }
    for(char c : tempDividend) {
        if(c == '1') {
            return "error detected";
        }
    }
    return "no error detected";
}

vector<string> deSerializeData(const string &data, int rowSize, int colSize) {
    vector<string>temp(rowSize, string(colSize, '0'));
    assert(data.size() > 0);
    assert(data.size() % rowSize == 0);
    assert(data.size() % colSize == 0);
    int row = 0, col = 0;
    for(char c : data) {
        temp[row][col] = c;
        row++;
        if(row == rowSize) {
            row = 0;
            col++;
        }
    }
    return temp;
}

vector<string> correctErrorHamming(const vector<string> &dataBlock) {
    vector<string> temp;
    string otherTemp;

    for(string s : dataBlock) {        
        int dataIndex = 0;
        otherTemp = '0' + s;
        for (int i = 1; i < otherTemp.size(); i++)
        {
            if (!isPow2(i))
            {
                int k = 1, temp_i = i;
                while (temp_i)
                {
                    if (temp_i & 1)
                    {
                        otherTemp[k] = xorChar(otherTemp[k], otherTemp[i]);
                    }
                    k <<= 1;
                    temp_i >>= 1;
                }
            }
        }

        int pos = 0;
        for(int k = 1; k < otherTemp.size(); k<<=1) {
            if(otherTemp[k] == '1') {
                pos += k;
            }
        }        
        if(pos < otherTemp.size()) {
            otherTemp[pos] = '1' + '0' - otherTemp[pos]; // otherwise multi bit error i guess
        }
        temp.emplace_back(otherTemp);
    }
    return temp;
}

vector<string> removeHammingCheckBits(const vector<string> &dataBlock) {
    vector<string> temp;
    for(string s : dataBlock) {
        string otherTemp;
        for(int i = 1; i < s.size(); i++) {
            if(!isPow2(i)) {
                otherTemp += s[i];
            }
        }
        temp.emplace_back(otherTemp);
    }
    return temp;
}

char _8bitBinaryToAscii(const string &s) {
    assert(s.size() == 8);
    char temp = 0;
    for(int i = 0; i < s.size(); i++) {
        if(s[i] == '1') {
            temp += (1 << (s.size() - 1 - i));
        }
    }
    return temp;
}

string getOutputFrame(const vector<string> &dataBlock) {
    string temp = "";
    for(string s : dataBlock) {
        assert(s.size() % 8 == 0);
        int cntSize = 0;
        while(cntSize != s.size()) {
            temp += _8bitBinaryToAscii(s.substr(cntSize, 8));
            cntSize += 8;
        }
    }
    return temp;
}

int main(void)
{
    string dataString, generatorPolynomial, serializedData, sentFrame, receivedFrame;
    vector<string> initialDataBlock, checkBitDataBlock, deSerializedData, rmvBitDataBlock, correctedDataBlock;
    int m;
    double errorProbability;

    // input
    cout << "enter data string: ";
    getline(cin, dataString);
    cout << '\n';
    cout << "enter number of data bytes in a row (m): ";
    cin >> m;
    cout << '\n';
    cout << "enter probability (p): ";
    cin >> errorProbability;
    cout << '\n';
    cout << "enter generator polynomial: ";
    cin >> generatorPolynomial;
    assert(generatorPolynomial[0] == '1');
    cout << "\n\n\n";

    // pad ~
    pad(dataString, m);
    cout << "data string after padding: " << dataString << "\n\n";

    // ascii to 8bit binary
    cout << "data block (ascii code of m characters per row): \n";
    initialDataBlock = createDataBlock(dataString, m);
    printDataBlock(initialDataBlock);
    cout << '\n';

    // add check bits with color
    cout << "data block after adding check bits: \n";
    checkBitDataBlock = getCheckBitDataBlock(initialDataBlock);
    printDataBlock(checkBitDataBlock, COLOR_GREEN);
    cout << '\n';

    // serialize data in column major manner
    cout << "data bits after column-wise serialization: \n";
    serializedData = serializeData(checkBitDataBlock);
    printDataBlock(checkBitDataBlock, -1);
    cout << '\n';

    // compute CRC checksum and append it to serialized data
    cout << "data bits after appending CRC checksum (sent frame): \n";
    string remainder = getRemainderCRC(serializedData, generatorPolynomial);
    sentFrame = serializedData + remainder;
    cout << serializedData;
    // SetConsoleTextAttribute(hConsole, COLOR_CYAN);
    cout << remainder << '\n';
    // SetConsoleTextAttribute(hConsole, COLOR_WHITE);
    cout << '\n';

    // add error to received frame
    vector<bool> errorBitMap(sentFrame.size(), false);
    random_device rd;
    mt19937 gen(rd());
    mt19937 rng(chrono::steady_clock::now().time_since_epoch().count());
    uniform_int_distribution<> dist(0, 100);
    cout << "received frame:\n";
    int i = 0;
    for (char c : sentFrame)
    {
        if (dist(gen) < (int)(errorProbability * 100.0))
        {
            // SetConsoleTextAttribute(hConsole, COLOR_RED);
            c = '1' + '0' - c;
            errorBitMap[i] = true;
        }
        i++;
        receivedFrame += c;
        cout << c;
        // SetConsoleTextAttribute(hConsole, COLOR_WHITE);
    }
    cout << "\n\n";

    // verify correctness of received frame
    cout << "result of CRC checksum matching: " << verifyCorrectNessReceivedFrame(receivedFrame, generatorPolynomial) << "\n\n";

    // remove CRC checksum bits and de-serialize into datablock
    cout << "data block after removing CRC checksum bits: \n";
    string removedBitsCRC = receivedFrame.substr(0, receivedFrame.size() - generatorPolynomial.size() + 1);
    deSerializedData = deSerializeData(removedBitsCRC, checkBitDataBlock.size(), checkBitDataBlock[0].size());
    printDataBlock(deSerializedData, COLOR_RED, &errorBitMap);
    cout << "\n";

    // correct errors and build data block without check bits
    cout << "data block after removing check bits: \n";
    correctedDataBlock = correctErrorHamming(deSerializedData);
    rmvBitDataBlock = removeHammingCheckBits(correctedDataBlock);    
    printDataBlock(rmvBitDataBlock, COLOR_WHITE);
    cout << "\n";

    // 8bit Binary to ASCII
    cout << "output frame: " << getOutputFrame(rmvBitDataBlock) << '\n';

    return 0;
}