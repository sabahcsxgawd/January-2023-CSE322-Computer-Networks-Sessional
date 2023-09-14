#include "ns3/applications-module.h"
#include "ns3/core-module.h"
#include "ns3/csma-module.h"
#include "ns3/internet-module.h"
#include "ns3/mobility-module.h"
#include "ns3/network-module.h"
#include "ns3/point-to-point-module.h"
#include "ns3/ssid.h"
#include "ns3/yans-wifi-helper.h"

#include <string>

using namespace ns3;

NS_LOG_COMPONENT_DEFINE("WiFi_High_Rate_Mobile");

int total_packets_sent = 0;
int total_packets_received = 0;
int total_received_bytes = 0;

void
receiver_callback(Ptr<const Packet> packet, const Address& address)
{
    total_packets_received++;
    total_received_bytes += packet->GetSize();
}

void
sender_callback(Ptr<const Packet> packet)
{
    total_packets_sent++;
}

int
main(int argc, char* argv[])
{
    bool verbose = false;
    uint32_t nodes = 20;
    uint32_t flows = 50;
    uint32_t packets_per_sec = 100;
    uint32_t coverage = 3;
    uint32_t packetSize = 1024;
    double simulationTime = 5.0;
    double Tx_range = 5.0;
    double speed = 5;
    double ap_distance = 200;
    std::string dataRate = std::to_string(packets_per_sec * packetSize * 8 / 1e6) + "Mbps";
    std::string outputFile;

    CommandLine cmd(__FILE__);
    cmd.AddValue("nodes", "Number of mobile wireless nodes", nodes);
    cmd.AddValue("flows", "Number of flows", flows);
    cmd.AddValue("pps", "How many packets per second", packets_per_sec);
    cmd.AddValue("speed", "Speed of nodes", speed);
    cmd.AddValue("file", "Data Output File", outputFile);

    cmd.Parse(argc, argv);

    outputFile = "scratch/mobile/" + outputFile + ".dat";

    Config::SetDefault("ns3::TcpSocket::SegmentSize", UintegerValue(packetSize));

    if (verbose)
    {
        LogComponentEnable("PacketSink", LOG_LEVEL_INFO);
        LogComponentEnable("OnOffApplication", LOG_LEVEL_INFO);
    }

    NodeContainer wifiApNodes;
    wifiApNodes.Create(2);

    PointToPointHelper bottleneck;
    bottleneck.SetDeviceAttribute("DataRate", StringValue("2Mbps"));
    bottleneck.SetChannelAttribute("Delay", StringValue("2ms"));

    NetDeviceContainer p2pDevices;
    p2pDevices = bottleneck.Install(wifiApNodes);

    NodeContainer wifiStaNodes_senders, wifiStaNodes_receivers;
    wifiStaNodes_senders.Create(nodes / 2);
    wifiStaNodes_receivers.Create(nodes / 2);

    YansWifiChannelHelper channel = YansWifiChannelHelper::Default();
    channel.AddPropagationLoss("ns3::RangePropagationLossModel",
                               "MaxRange",
                               ns3::DoubleValue(coverage * Tx_range));
    YansWifiPhyHelper phy;

    WifiHelper wifi;
    WifiMacHelper mac;

    NetDeviceContainer staDevices_senders, staDevices_receivers, apDevice_sender, apDevice_receiver;

    Ssid ssid_senders = Ssid("ns-3-ssid-senders");
    Ssid ssid_receivers = Ssid("ns-3-ssid-receivers");

    phy.SetChannel(channel.Create());
    mac.SetType("ns3::StaWifiMac",
                "Ssid",
                SsidValue(ssid_senders),
                "ActiveProbing",
                BooleanValue(false));

    staDevices_senders = wifi.Install(phy, mac, wifiStaNodes_senders);
    mac.SetType("ns3::ApWifiMac", "Ssid", SsidValue(ssid_senders));
    apDevice_sender = wifi.Install(phy, mac, wifiApNodes.Get(0));

    phy.SetChannel(channel.Create());
    mac.SetType("ns3::StaWifiMac",
                "Ssid",
                SsidValue(ssid_receivers),
                "ActiveProbing",
                BooleanValue(false));

    staDevices_receivers = wifi.Install(phy, mac, wifiStaNodes_receivers);
    mac.SetType("ns3::ApWifiMac", "Ssid", SsidValue(ssid_receivers));
    apDevice_receiver = wifi.Install(phy, mac, wifiApNodes.Get(1));

    MobilityHelper mobility;

    mobility.SetMobilityModel("ns3::ConstantPositionMobilityModel");
    Ptr<ListPositionAllocator> ap_positions = CreateObject<ListPositionAllocator>();
    ap_positions->Add(Vector(0.0, 5.0, 0.0));
    ap_positions->Add(Vector(ap_distance, 5.0, 0.0));
    mobility.SetPositionAllocator(ap_positions);
    mobility.Install(wifiApNodes);

    double box_half_side = (2.0 * coverage * Tx_range);
    mobility.SetMobilityModel(
        "ns3::RandomWalk2dMobilityModel",
        "Bounds",
        RectangleValue(Rectangle(-box_half_side, box_half_side, -box_half_side, box_half_side)),
        "Speed",
        StringValue("ns3::ConstantRandomVariable[Constant=" + std::to_string(speed) + "]"));

    Ptr<UniformRandomVariable> diskRho = CreateObject<UniformRandomVariable>();
    diskRho->SetAttribute("Min", ns3::DoubleValue(Tx_range));
    diskRho->SetAttribute("Max", ns3::DoubleValue(coverage * Tx_range));

    mobility.SetPositionAllocator("ns3::RandomDiscPositionAllocator",
                                  "X",
                                  ns3::DoubleValue(0.0),
                                  "Y",
                                  ns3::DoubleValue(5.0),
                                  "Rho",
                                  ns3::PointerValue(diskRho));

    mobility.Install(wifiStaNodes_senders);

    mobility.SetMobilityModel(
        "ns3::RandomWalk2dMobilityModel",
        "Bounds",
        RectangleValue(Rectangle(-box_half_side + ap_distance,
                                 box_half_side + ap_distance,
                                 -box_half_side,
                                 box_half_side)),
        "Speed",
        StringValue("ns3::ConstantRandomVariable[Constant=" + std::to_string(speed) + "]"));
    mobility.SetPositionAllocator("ns3::RandomDiscPositionAllocator",
                                  "X",
                                  ns3::DoubleValue(ap_distance),
                                  "Y",
                                  ns3::DoubleValue(5.0),
                                  "Rho",
                                  ns3::PointerValue(diskRho));
    mobility.Install(wifiStaNodes_receivers);

    InternetStackHelper stack;
    stack.Install(wifiApNodes);
    stack.Install(wifiStaNodes_senders);
    stack.Install(wifiStaNodes_receivers);

    Ipv4AddressHelper address;

    address.SetBase("10.1.1.0", "255.255.255.0");
    address.Assign(p2pDevices);

    address.SetBase("10.1.2.0", "255.255.255.0");
    address.Assign(apDevice_sender);
    address.Assign(staDevices_senders);

    address.SetBase("10.1.3.0", "255.255.255.0");
    address.Assign(apDevice_receiver);
    Ipv4InterfaceContainer receiver_interfaces = address.Assign(staDevices_receivers);

    PacketSinkHelper sinkHelper("ns3::TcpSocketFactory",
                                InetSocketAddress(Ipv4Address::GetAny(), 9));

    ApplicationContainer receiverApps = sinkHelper.Install(wifiStaNodes_receivers);

    for (uint32_t i = 0; i < receiverApps.GetN(); i++)
    {
        receiverApps.Get(i)->TraceConnectWithoutContext("Rx", MakeCallback(&receiver_callback));
    }

    receiverApps.Start(Seconds(0.0));

    ApplicationContainer senderApps;

    OnOffHelper sender_helper("ns3::TcpSocketFactory", Address());
    sender_helper.SetAttribute("OnTime", StringValue("ns3::ConstantRandomVariable[Constant=1]"));
    sender_helper.SetAttribute("OffTime", StringValue("ns3::ConstantRandomVariable[Constant=0]"));
    sender_helper.SetAttribute("DataRate", DataRateValue(DataRate(dataRate)));
    sender_helper.SetAttribute("PacketSize", UintegerValue(packetSize));

    int sender_receiver_cnt = nodes / 2;
    for (uint32_t i = 0; i < flows; i++)
    {
        sender_helper.SetAttribute(
            "Remote",
            AddressValue(
                InetSocketAddress(receiver_interfaces.GetAddress(i % sender_receiver_cnt), 9)));
        senderApps.Add(sender_helper.Install(wifiStaNodes_senders.Get(i % sender_receiver_cnt)));
    }

    for (uint32_t i = 0; i < senderApps.GetN(); i++)
    {
        senderApps.Get(i)->TraceConnectWithoutContext("Tx", MakeCallback(&sender_callback));
    }

    senderApps.Start(Seconds(1.0));

    Ipv4GlobalRoutingHelper::PopulateRoutingTables();

    Simulator::Stop(Seconds(simulationTime));

    Simulator::Run();
    Simulator::Destroy();

    double average_throughput = (total_received_bytes * 8.0 / 1e6) / simulationTime;

    double average_packet_delivery_ratio = (total_packets_received * 1.0) / (total_packets_sent);

    std::ofstream writer(outputFile, std::ios::app);

    writer <<
    nodes << ' ' <<
    flows << ' ' <<
    packets_per_sec << ' ' <<
    speed << ' ' <<
    average_throughput << ' ' <<
    average_packet_delivery_ratio << '\n';

    writer.close();

    return 0;
}
