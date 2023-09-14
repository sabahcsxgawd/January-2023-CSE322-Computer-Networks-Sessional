#ifndef TCP_ADAPTIVERENO_H
#define TCP_ADAPTIVERENO_H

#include "tcp-congestion-ops.h"
#include "tcp-westwood-plus.h"
#include "ns3/tcp-recovery-ops.h"
#include "ns3/sequence-number.h"
#include "ns3/traced-value.h"
#include "ns3/event-id.h"

namespace ns3 {

class Time;

class TcpAdaptiveReno : public TcpWestwoodPlus
{
public:
  
  static TypeId GetTypeId (void);

  TcpAdaptiveReno (void);
  
  TcpAdaptiveReno (const TcpAdaptiveReno& sock);

  virtual ~TcpAdaptiveReno (void);

  enum FilterType 
  {
    NONE,
    TUSTIN
  };

  virtual uint32_t GetSsThresh (Ptr<const TcpSocketState> tcb,
                                uint32_t bytesInFlight);

  virtual void PktsAcked (Ptr<TcpSocketState> tcb, uint32_t packetsAcked,
                          const Time& rtt);

  virtual Ptr<TcpCongestionOps> Fork ();

private:

  double EstimateCongestionLevel();

  void EstimateIncWnd(Ptr<TcpSocketState> tcb);

protected:

  void EstimateBW (const Time& rtt, Ptr<TcpSocketState> tcb);

  virtual void CongestionAvoidance (Ptr<TcpSocketState> tcb, uint32_t segmentsAcked);

  Time       member_minimumRTT;
  Time       member_currentRTT;
  Time       member_jPacketLossRTT;
  Time       member_cong_j_RTT;
  Time       member_cong_prev_j_RTT;
 
  int32_t    member_incrementWindow;
  uint32_t   member_baseWindow;
  int32_t    member_probeWindow;
};

}

#endif
