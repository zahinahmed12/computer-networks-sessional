

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

public class ServerThread implements Runnable {

    NetworkUtility networkUtility;
    EndDevice endDevice;

    ArrayList<Router> routers_path;

    ServerThread(NetworkUtility networkUtility, EndDevice endDevice) {
        this.networkUtility = networkUtility;
        this.endDevice = endDevice;
        System.out.println("Server Ready for client " + NetworkLayerServer.clientCount);
        //NetworkLayerServer.clientCount++;
        new Thread(this).start();
    }

    @Override
    public void run() {
        /**
         * Synchronize actions with client.
         */
        networkUtility.write(endDevice);
        networkUtility.write(NetworkLayerServer.endDevices);

        for(int i=0;i<100;i++)
        {
            Packet p=(Packet) networkUtility.read();
            boolean succeeded=deliverPacket(p);
            if(p.getSpecialMessage().equals("SHOW_ROUTE"))
            {
                networkUtility.write(routers_path);
            }

            if(succeeded)
            {
                networkUtility.write("Packet successfully delivered! :) ");
                networkUtility.write(p.hopcount);
            }
            else
            {
                networkUtility.write("Packet dropped! :( ");
                networkUtility.write(p.hopcount);
            }
        }
    }


    public Boolean deliverPacket(Packet p) {

        routers_path=new ArrayList<>();

        IPAddress srcIP=p.getSourceIP();
        Short[] src=srcIP.getBytes();
        IPAddress destIP=p.getDestinationIP();
        Short[] dest=destIP.getBytes();

        Router source=null, destination=null;
        boolean f1=false, f2=false;

        for (Map.Entry<IPAddress,Integer> entry: NetworkLayerServer.interfacetoRouterID.entrySet()) {

            IPAddress key = entry.getKey();
            Integer value = entry.getValue();

            Short[] temp=key.getBytes();

            if(temp[0].equals(src[0]) && temp[1].equals(src[1]) && temp[2].equals(src[2]))
            {
                source=NetworkLayerServer.routerMap.get(value);
                f1=true;
            }

            if(temp[0].equals(dest[0]) && temp[1].equals(dest[1]) && temp[2].equals(dest[2]))
            {
                destination=NetworkLayerServer.routerMap.get(value);
                f2=true;
            }

            if(f1 && f2)
            {
                break;
            }
        }
        routers_path.add(source);

        boolean outcome=forwarding(source,destination,p);

        if (!outcome) {
            p.hopcount = -1;
        }
        return outcome;
    }

    public boolean forwarding(Router s, Router d, Packet p)
    {
        if(s.equals(d)) return true;

        for(RoutingTableEntry entry: s.getRoutingTable())
        {
            if(entry.getRouterId()==d.getRouterId()) {

                /*if (entry.getDistance() == Constants.INFINITY) {
                    return false;
                }*/
                Router r = NetworkLayerServer.routerMap.get(entry.getGatewayRouterId());

                if (r.getState()) {
                    routers_path.add(r);
                    p.hopcount++;

                    // packet is sent

                    RoutingTableEntry entryR = null;
                    for (RoutingTableEntry er : r.getRoutingTable()) {
                        if (er.getRouterId() == s.getRouterId()) {
                            entryR = er;
                            break;
                        }
                    }

                    if (entryR != null && entryR.getDistance() == Constants.INFINITY) {
                        entryR.setDistance(1);

                        synchronized (RouterStateChanger.msg) {
                            NetworkLayerServer.DVR(r.getRouterId());
//                            NetworkLayerServer.simpleDVR(s.getRouterId());
                        }
                    }
                    return forwarding(r, d, p);
                }
                else {

                    boolean found = false;
                    for (RoutingTableEntry src_entry : s.getRoutingTable()) {
                        if (src_entry.getRouterId() == r.getRouterId()) {
                            src_entry.setDistance(Constants.INFINITY);
                            src_entry.setGatewayRouterId(r.getRouterId());
                            found = true;
                            break;
                        }
                    }
                    /*if(!found)
                    {
                        RoutingTableEntry enter=new RoutingTableEntry(r.getRouterId(),Constants.INFINITY,r.getRouterId());
                        s.getRoutingTable().add(enter);
                    }*/

                    synchronized (RouterStateChanger.msg) {
                        NetworkLayerServer.DVR(s.getRouterId());
//                        NetworkLayerServer.simpleDVR(s.getRouterId());
                    }
                    break;
                }
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj); //To change body of generated methods, choose Tools | Templates.
    }
}
