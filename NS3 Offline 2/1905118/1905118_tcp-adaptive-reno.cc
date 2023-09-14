#include "1905118_tcp-adaptive-reno.h"

#include "rtt-estimator.h"
#include "tcp-socket-base.h"

#include "ns3/log.h"
#include "ns3/simulator.h"

NS_LOG_COMPONENT_DEFINE("TcpAdaptiveReno");

namespace ns3
{

NS_OBJECT_ENSURE_REGISTERED(TcpAdaptiveReno);

TypeId
TcpAdaptiveReno::GetTypeId(void)
{
    static TypeId tid =
        TypeId("ns3::TcpAdaptiveReno")
            .SetParent<TcpNewReno>()
            .SetGroupName("Internet")
            .AddConstructor<TcpAdaptiveReno>()
            .AddAttribute(
                "FilterType",
                "Use this to choose no filter or Tustin's approximation filter",
                EnumValue(TcpAdaptiveReno::TUSTIN),
                MakeEnumAccessor(&TcpAdaptiveReno::m_fType),
                MakeEnumChecker(TcpAdaptiveReno::NONE, "None", TcpAdaptiveReno::TUSTIN, "Tustin"))
            .AddTraceSource("EstimatedBW",
                            "The estimated bandwidth",
                            MakeTraceSourceAccessor(&TcpAdaptiveReno::m_currentBW),
                            "ns3::TracedValueCallback::Double");
    return tid;
}

TcpAdaptiveReno::TcpAdaptiveReno(void)
    : TcpWestwoodPlus(),
      member_minimumRTT(Time(0)),
      member_currentRTT(Time(0)),
      member_jPacketLossRTT(Time(0)),
      member_cong_j_RTT(Time(0)),
      member_cong_prev_j_RTT(Time(0)),
      member_incrementWindow(0),
      member_baseWindow(0),
      member_probeWindow(0)
{
}

TcpAdaptiveReno::TcpAdaptiveReno(const TcpAdaptiveReno& sock)
    : TcpWestwoodPlus(sock),
      member_minimumRTT(Time(0)),
      member_currentRTT(Time(0)),
      member_jPacketLossRTT(Time(0)),
      member_cong_j_RTT(Time(0)),
      member_cong_prev_j_RTT(Time(0)),
      member_incrementWindow(0),
      member_baseWindow(0),
      member_probeWindow(0)
{
}

TcpAdaptiveReno::~TcpAdaptiveReno(void)
{
}

void
TcpAdaptiveReno::PktsAcked(Ptr<TcpSocketState> tcb, uint32_t packetsAcked, const Time& rtt)
{

    if (rtt.IsZero())
    {        
        return;
    }

    m_ackedSegments += packetsAcked;

    if (member_minimumRTT.IsZero())
    {
        member_minimumRTT = rtt;
    }
    else if (rtt <= member_minimumRTT)
    {
        member_minimumRTT = rtt;
    }

    member_currentRTT = rtt;

    TcpWestwoodPlus::EstimateBW(rtt, tcb);
}

double
TcpAdaptiveReno::EstimateCongestionLevel()
{
    // From Spec

    double a = 0.85;
    if (member_cong_prev_j_RTT < member_minimumRTT)
        a = 0;

    double cong_j_RTT =
        a * member_cong_prev_j_RTT.GetSeconds() +
        (1 - a) * member_jPacketLossRTT.GetSeconds();
    member_cong_j_RTT = Seconds(cong_j_RTT);

    return std::min((member_currentRTT.GetSeconds() - member_minimumRTT.GetSeconds()) / (cong_j_RTT - member_minimumRTT.GetSeconds()), 1.0);
}

void
TcpAdaptiveReno::EstimateIncWnd(Ptr<TcpSocketState> tcb)
{
    // From Spec

    double congestion = EstimateCongestionLevel();
    int M = 1000;

    double maxIncWindow = static_cast<double>(m_currentBW.Get().GetBitRate() / M * static_cast<double>(tcb->m_segmentSize * tcb->m_segmentSize));

    double alpha = 10;
    double beta = 2 * maxIncWindow * ((1 / alpha) - ((1 / alpha + 1) / (std::exp(alpha))));
    double gamma = 1 - (2 * maxIncWindow * ((1 / alpha) - ((1 / alpha + 0.5) / (std::exp(alpha)))));

    member_incrementWindow = (int)((maxIncWindow / std::exp(alpha * congestion)) + (beta * congestion) + gamma);
    
}

void
TcpAdaptiveReno::CongestionAvoidance(Ptr<TcpSocketState> tcb, uint32_t segmentsAcked)
{
    
   // From Spec

    if (segmentsAcked > 0)
    {
        EstimateIncWnd(tcb);
        double adder = static_cast<double>(tcb->m_segmentSize * tcb->m_segmentSize) / tcb->m_cWnd.Get();
        adder = std::max(1.0, adder);
        member_baseWindow += static_cast<uint32_t>(adder);
        member_probeWindow = std::max((double)(member_probeWindow + member_incrementWindow / (int)tcb->m_cWnd.Get()), (double)0);
    
        tcb->m_cWnd = member_baseWindow + member_probeWindow;
    }
}

uint32_t
TcpAdaptiveReno::GetSsThresh(Ptr<const TcpSocketState> tcb, uint32_t bytesInFlight)
{
    
    member_jPacketLossRTT = member_currentRTT;
    member_cong_prev_j_RTT = member_cong_j_RTT;

    double congestion = EstimateCongestionLevel();

    uint32_t ssthresh = std::max(2 * tcb->m_segmentSize, (uint32_t)(tcb->m_cWnd / (1.0 + congestion)));

    member_baseWindow = ssthresh;
    member_probeWindow = 0;

    return ssthresh;
}

Ptr<TcpCongestionOps>
TcpAdaptiveReno::Fork()
{
    return CreateObject<TcpAdaptiveReno>(*this);
}

}
