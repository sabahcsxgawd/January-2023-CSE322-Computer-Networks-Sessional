#include <fstream>
#include "ns3/core-module.h"
#include "ns3/network-module.h"
#include "ns3/internet-module.h"
#include "ns3/point-to-point-module.h"
#include "ns3/point-to-point-layout-module.h"
#include "ns3/applications-module.h"
#include "ns3/stats-module.h"
#include "ns3/callback.h"
#include "ns3/flow-monitor-module.h"
#include "ns3/flow-monitor-helper.h"
#include "ns3/csma-module.h"

using namespace ns3;

class TestApp : public Application
{
  public:
    TestApp();
    ~TestApp() override;

    /**
     * Register this type.
     * \return The TypeId.
     */
    static TypeId GetTypeId();

    /**
     * Setup the socket.
     * \param socket The socket.
     * \param address The destination address.
     * \param packetSize The packet size to transmit.
     * \param simulationTime The whole simulation time
     * \param dataRate the data rate to use.
     */
    void Setup(Ptr<Socket> socket,
               Address address,
               uint32_t packetSize,
               double simulationTime,
               DataRate dataRate);

  private:
    void StartApplication() override;
    void StopApplication() override;

    /// Schedule a new transmission.
    void ScheduleTx();
    /// Send a packet.
    void SendPacket();

    Ptr<Socket> m_socket;      //!< The transmission socket.
    Address m_peer;            //!< The destination address.
    uint32_t m_packetSize;     //!< The packet size.
    double m_simulationTime;   //!< The whole simulation time
    DataRate m_dataRate;       //!< The data rate to use.
    EventId m_sendEvent;       //!< Send event.
    bool m_running;            //!< True if the application is running.
    uint32_t m_packetsSent;    //!< The number of packets sent.
};

TestApp::TestApp()
    : m_socket(nullptr),
      m_peer(),
      m_packetSize(0),
      m_simulationTime(0),
      m_dataRate(0),
      m_sendEvent(),
      m_running(false),
      m_packetsSent(0)
{
}

TestApp::~TestApp()
{
    m_socket = nullptr;
}

/* static */
TypeId
TestApp::GetTypeId()
{
    static TypeId tid = TypeId("TestApp")
                            .SetParent<Application>()
                            .SetGroupName("Tutorial")
                            .AddConstructor<TestApp>();
    return tid;
}

void
TestApp::Setup(Ptr<Socket> socket,
               Address address,
               uint32_t packetSize,
               double simulationTime,
               DataRate dataRate)
{
    m_socket = socket;
    m_peer = address;
    m_packetSize = packetSize;
    m_simulationTime = simulationTime;
    m_dataRate = dataRate;
}

void
TestApp::StartApplication()
{
    m_running = true;
    m_packetsSent = 0;
    // TODO
    m_socket->Bind();
    m_socket->Connect(m_peer);
    SendPacket();
}

void
TestApp::StopApplication()
{
    m_running = false;

    if (m_sendEvent.IsRunning())
    {
        Simulator::Cancel(m_sendEvent);
    }

    if (m_socket)
    {
        m_socket->Close();
    }
}

void
TestApp::SendPacket()
{
    Ptr<Packet> packet = Create<Packet>(m_packetSize);
    m_socket->Send(packet);

    if (Simulator::Now().GetSeconds() < m_simulationTime)
    {
        ScheduleTx();
    }
}

void
TestApp::ScheduleTx()
{
    if (m_running)
    {
        Time tNext(Seconds(m_packetSize * 8 / static_cast<double>(m_dataRate.GetBitRate())));
        m_sendEvent = Simulator::Schedule(tNext, &TestApp::SendPacket, this);
    }
}

std::ofstream ThroughputFile, CongFile0, CongFile1;
int mode = 0;

static void
CwndChange0(uint32_t oldCwnd, uint32_t newCwnd)
{
    // NS_LOG_UNCOND(Simulator::Now().GetSeconds() << "\t" << newCwnd);
    if(mode == 3) {
        CongFile0 << Simulator::Now().GetSeconds() << "\t" << newCwnd << '\n';
    }
}


static void
CwndChange1(uint32_t oldCwnd, uint32_t newCwnd)
{
    // NS_LOG_UNCOND(Simulator::Now().GetSeconds() << "\t" << newCwnd);
    if(mode == 3) {
        CongFile1 << Simulator::Now().GetSeconds() << "\t" << newCwnd << '\n';
    }
}

static void (*CwndChanges[])(uint32_t oldCwnd, uint32_t newCwnd) = {
   CwndChange0,
   CwndChange1
};

NS_LOG_COMPONENT_DEFINE("1905118_TaskA");

std::string CongCntrlAlgs[] = {
    "ns3::TcpNewReno",
    "ns3::TcpWestwoodPlus",
    "ns3::TcpHighSpeed",
    "ns3::TcpAdaptiveReno"
};

int
main(int argc, char* argv[])
{

    uint32_t packetSize = 1024;
    std::string CongCntrlAlg1 = "ns3::TcpNewReno";
    std::string CongCntrlAlg2 = "ns3::TcpWestwoodPlus";
    uint32_t whichCongCntrlAlg2 = 1;
    std::string SnRxDataRate = "1Gbps";
    std::string SnRxDelay = "1ms";
    int i_bottleneckDelay = 100;
    std::string bottleneckDelay = std::to_string(i_bottleneckDelay) + "ms";

    uint32_t leafCount = 2;
    uint32_t i_bottleneckDataRate = 50;
    double d_packetLossExponent = 6.0;

    double simulationTime = 30.0;



    CommandLine cmd(__FILE__);
    cmd.AddValue ("CongCntrlAlg2", "Set Congestion Control Algorithm to compare\n(1) for TcpWestWoodPlus\n(2) for TcpHighSpeed\n(3) for TcpAdaptiveReno", whichCongCntrlAlg2);
    cmd.AddValue ("BottleneckDataRate", "Set BottleneckDataRate", i_bottleneckDataRate);
    cmd.AddValue ("PacketLossExponent", "Set Packet Loss Rate to 10^(-exponent)", d_packetLossExponent);
    cmd.AddValue ("PlotMode", "Set Plot Mode", mode);
    cmd.Parse(argc, argv);

    if(whichCongCntrlAlg2 > 0 && whichCongCntrlAlg2 < (sizeof(CongCntrlAlgs) / sizeof(CongCntrlAlgs[0]))) {
        CongCntrlAlg2 = CongCntrlAlgs[whichCongCntrlAlg2];
        if(mode == 3) {
            std::string output = "scratch/1905118_congestion_" + CongCntrlAlg1.substr(5, CongCntrlAlg1.size() - 4) + ".dat";
            CongFile0.open(output, std::ios_base::app);

            output = "scratch/1905118_congestion_" + CongCntrlAlg2.substr(5, CongCntrlAlg2.size() - 4) + ".dat";
            CongFile1.open(output, std::ios_base::app);        
        }        
    }

    double packetLossRate = std::pow(10, -d_packetLossExponent);
    std::string bottleneckDataRate = std::to_string(i_bottleneckDataRate) + "Mbps";

    Config::SetDefault ("ns3::TcpSocket::SegmentSize", UintegerValue (packetSize));

    PointToPointHelper bottleneckLink, p2pLeaf;

    bottleneckLink.SetDeviceAttribute("DataRate", StringValue(bottleneckDataRate));
    bottleneckLink.SetChannelAttribute("Delay", StringValue(bottleneckDelay));

    p2pLeaf.SetDeviceAttribute("DataRate", StringValue(SnRxDataRate));
    p2pLeaf.SetChannelAttribute("Delay", StringValue(SnRxDelay));

    p2pLeaf.SetQueue("ns3::DropTailQueue", "MaxSize", StringValue(std::to_string((i_bottleneckDataRate * i_bottleneckDelay * 125 / packetSize)) + "p"));

    PointToPointDumbbellHelper dumbel(leafCount, p2pLeaf, leafCount, p2pLeaf, bottleneckLink);

    Ptr<RateErrorModel> em = CreateObject<RateErrorModel>();
    em->SetAttribute("ErrorRate", DoubleValue(packetLossRate));
    dumbel.m_routerDevices.Get(0)->SetAttribute("ReceiveErrorModel", PointerValue(em));
    dumbel.m_routerDevices.Get(1)->SetAttribute("ReceiveErrorModel", PointerValue(em));

    Config::SetDefault ("ns3::TcpL4Protocol::SocketType", StringValue (CongCntrlAlg1));

    InternetStackHelper stackHelper;

    stackHelper.Install(dumbel.GetLeft(0));
    stackHelper.Install(dumbel.GetRight(0));

    // bottleneckDevices get stack corresponding to TcpNewReno
    stackHelper.Install(dumbel.GetLeft());
    stackHelper.Install(dumbel.GetRight());

    Config::SetDefault ("ns3::TcpL4Protocol::SocketType", StringValue (CongCntrlAlg2));

    stackHelper.Install(dumbel.GetLeft(1));
    stackHelper.Install(dumbel.GetRight(1));


    dumbel.AssignIpv4Addresses
    (
        Ipv4AddressHelper("10.1.1.0", "255.255.255.0"),
        Ipv4AddressHelper("10.2.1.0", "255.255.255.0"),
        Ipv4AddressHelper("10.3.1.0", "255.255.255.0")
    );

    Ipv4GlobalRoutingHelper::PopulateRoutingTables();

    FlowMonitorHelper flowMonitorHelper;
    flowMonitorHelper.SetMonitorAttribute("MaxPerHopDelay", TimeValue(Seconds(2.0)));
    Ptr<FlowMonitor> flowMonitor = flowMonitorHelper.InstallAll();
    
    PacketSinkHelper packetSinkHelper("ns3::TcpSocketFactory", InetSocketAddress (Ipv4Address::GetAny(), 9));
    ApplicationContainer sinkApps;

    for(uint32_t i = 0; i < leafCount; i++) {
        sinkApps.Add(packetSinkHelper.Install(dumbel.GetRight(i)));
    }

    sinkApps.Start(Seconds(0.0));
    sinkApps.Stop(Seconds(simulationTime + 2.0));

    for(uint32_t i = 0; i < leafCount; i++) {
        Ptr<Socket> ns3TcpSocket = Socket::CreateSocket(dumbel.GetLeft(i), TcpSocketFactory::GetTypeId());
        ns3TcpSocket->TraceConnectWithoutContext("CongestionWindow", MakeCallback(CwndChanges[i % leafCount]));
        Address sinkAddress(InetSocketAddress(dumbel.GetRightIpv4Address(i), 9));
        Ptr<TestApp> app = CreateObject<TestApp>();
        app->Setup(ns3TcpSocket, sinkAddress, packetSize, simulationTime, DataRate(SnRxDataRate));
        dumbel.GetLeft(i)->AddApplication(app);
        app->SetStartTime(Seconds(1.0));
        app->SetStopTime(Seconds(simulationTime));
    }    

    Simulator::Stop(Seconds(simulationTime + 2.0));
    Simulator::Run();

    uint64_t receivedBytesSum[leafCount] = {0};
    uint64_t jainIndexNumerator = 0, jainIndexDenominator = 0;
    double jainIndex;
    int i = 0;

    FlowMonitor::FlowStatsContainer stats = flowMonitor->GetFlowStats();

    for(auto it = stats.begin(); it != stats.end(); it++) {
        receivedBytesSum[i % leafCount] += it->second.rxBytes;
        jainIndexNumerator += it->second.rxBytes;
        jainIndexDenominator += (it->second.rxBytes * it->second.rxBytes);
        i++;
    }
    receivedBytesSum[0] *= (8.0 / (simulationTime * 1e3));
    receivedBytesSum[1] *= (8.0 / (simulationTime * 1e3));
    jainIndexNumerator *= jainIndexNumerator;
    jainIndex = (1.0 * jainIndexNumerator) / (i * jainIndexDenominator);

    CongCntrlAlg1 = CongCntrlAlg1.substr(5, CongCntrlAlg1.size() - 4);
    CongCntrlAlg2 = CongCntrlAlg2.substr(5, CongCntrlAlg2.size() - 4);
    
    if(mode == 1) {
        ThroughputFile.open("scratch/1905118_thrpt_btlnk_datarate_" + CongCntrlAlg2 + "_" + CongCntrlAlg1 + ".dat", std::ios_base::app);
        ThroughputFile << i_bottleneckDataRate << '\t' 
                       << receivedBytesSum[0] << '\t'
                       << receivedBytesSum[1] << '\t'
                       << jainIndex << '\n';
    }
    if(mode == 2) {
        ThroughputFile.open("scratch/1905118_thrpt_pktLossRate_" + CongCntrlAlg2 + "_" + CongCntrlAlg1 + ".dat", std::ios_base::app);
        ThroughputFile << d_packetLossExponent << '\t' 
                       << receivedBytesSum[0] << '\t'
                       << receivedBytesSum[1] << '\t'
                       << jainIndex << '\n';
    }
    
    ThroughputFile.close();
    if(mode == 3) {
        CongFile0.close();
        CongFile1.close();
    }    
    Simulator::Destroy();
    return 0;
}
