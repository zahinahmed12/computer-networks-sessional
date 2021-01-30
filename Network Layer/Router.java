//Work needed

import java.io.Serializable;
import java.util.*;

public class Router implements Serializable {
    private int routerId;
    private int numberOfInterfaces;
    private ArrayList<IPAddress> interfaceAddresses;//list of IP address of all interfaces of the router
    private ArrayList<RoutingTableEntry> routingTable;//used to implement DVR
    private ArrayList<Integer> neighborRouterIDs;//Contains both "UP" and "DOWN" state routers
    private Boolean state;//true represents "UP" state and false is for "DOWN" state
    private Map<Integer, IPAddress> gatewayIDtoIP;

    //boolean m_flag=false;
    public Router() {
        interfaceAddresses = new ArrayList<>();
        routingTable = new ArrayList<>();
        neighborRouterIDs = new ArrayList<>();

        /**
         * 80% Probability that the router is up
         */
        Random random = new Random();
        double p = random.nextDouble();
        if(p < 0.80) state = true;
        else state = false;

        numberOfInterfaces = 0;
    }

    public Router(int routerId, ArrayList<Integer> neighborRouters, ArrayList<IPAddress> interfaceAddresses, Map<Integer, IPAddress> gatewayIDtoIP) {
        this.routerId = routerId;
        this.interfaceAddresses = interfaceAddresses;
        this.neighborRouterIDs = neighborRouters;
        this.gatewayIDtoIP = gatewayIDtoIP;
        routingTable = new ArrayList<>();



        /**
         * 80% Probability that the router is up
         */
        Random random = new Random();
        double p = random.nextDouble();
        if(p < 0.80) state = true;
        else state = false;

        numberOfInterfaces = interfaceAddresses.size();
    }

    @Override
    public String toString() {
        String string = "";
        string += "Router ID: " + routerId + "\n" + "Interfaces: \n";
        for (int i = 0; i < numberOfInterfaces; i++) {
            string += interfaceAddresses.get(i).getString() + "\t";
        }
        string += "\n" + "Neighbors: \n";
        for(int i = 0; i < neighborRouterIDs.size(); i++) {
            string += neighborRouterIDs.get(i) + "\t";
        }
        return string;
    }
    /**
     * Initialize the distance(hop count) for each router.
     * for itself, distance=0; for any connected router with state=true, distance=1; otherwise distance=Constants.INFTY;
     */
    public void initiateRoutingTable() {

        /*RoutingTableEntry own=new RoutingTableEntry(this.routerId,0,this.routerId);
        this.routingTable.add(own);
        Iterator<Integer> it=neighborRouterIDs.iterator();
        while (it.hasNext())
        {
            int x=it.next();
            RoutingTableEntry r;
            if(NetworkLayerServer.routerMap.get(x).state)
            {
                r=new RoutingTableEntry(x,1,x);
            }
            else
            {
                r=new RoutingTableEntry(x,Constants.INFINITY,x);
            }
            this.routingTable.add(r);
        }*/

        routingTable.clear();
        for (Router router: NetworkLayerServer.routers) {
            if (router.getRouterId() == routerId) {
                routingTable.add(new RoutingTableEntry(routerId, 0, routerId));
            }
            else if (neighborRouterIDs.contains(router.getRouterId()) && router.state) {
                routingTable.add(new RoutingTableEntry(router.getRouterId(), 1, router.getRouterId()));
            }
            else {
                routingTable.add(new RoutingTableEntry(router.getRouterId(), Constants.INFINITY, router.getRouterId()));
            }
        }
    }

    /**
     * Delete all the routingTableEntry
     */
    public void clearRoutingTable() {

        routingTable.clear();
    }

    /**
     * Update the routing table for this router using the entries of Router neighbor
     * @param neighbor
     */
    public boolean updateRoutingTable(Router neighbor) {

        int neighborId=neighbor.routerId;
        boolean flag=false;

        for(RoutingTableEntry ne:neighbor.routingTable)
        {
            boolean found=false;
            for(RoutingTableEntry ce:this.routingTable)
            {
                if(ce.getRouterId()==ne.getRouterId())
                {
                    found=true;
                    break;
                }
            }
            if(!found)
            {
                RoutingTableEntry n=new RoutingTableEntry(ne.getRouterId(),ne.getDistance()+(ne.getDistance() == Constants.INFINITY ? 0 : 1),neighborId);
                this.routingTable.add(n);
                flag=true;
            }
            else
            {
                for (RoutingTableEntry ce: this.routingTable)
                {
                    if(ce.getRouterId()==ne.getRouterId())
                    {
                        if(ce.getDistance()>ne.getDistance()+1)
                        {
                            ce.setDistance(ne.getDistance()+1);
                            ce.setGatewayRouterId(neighborId);
                            flag=true;
                        }
                        else if(ce.getGatewayRouterId()==neighborId)
                        {
                            int newDist = ne.getDistance() + (ne.getDistance() == Constants.INFINITY ? 0 : 1);

                            if(newDist>ce.getDistance())
                            {
                                ce.setDistance(newDist);
                                ce.setGatewayRouterId(neighborId);
                                flag=true;
                            }
                        }
                    }
                }
            }
        }
        return flag;
    }

    public boolean sfupdateRoutingTable(Router neighbor) {

        boolean m_flag=false;
        int neighborId=neighbor.routerId;

        for(RoutingTableEntry ne:neighbor.routingTable)
        {
            if(ne.getGatewayRouterId()!=this.routerId)
            {
                /*boolean found=false;

                for(RoutingTableEntry ce:this.routingTable)
                {
                    if(ce.getRouterId()==ne.getRouterId())
                    {
                        found=true;
                        break;
                    }
                }
                if(!found)
                {
                    RoutingTableEntry n=new RoutingTableEntry(ne.getRouterId(),ne.getDistance()+(ne.getDistance() == Constants.INFINITY ? 0 : 1),neighborId);
                    this.routingTable.add(n);
                    m_flag=true;
                }*/
                //else
                {
                    for (RoutingTableEntry ce: this.routingTable)
                    {
                        if(ce.getRouterId()==ne.getRouterId())
                        {
                            if(ce.getDistance()>ne.getDistance()+1)
                            {
                                ce.setDistance(ne.getDistance()+1);
                                ce.setGatewayRouterId(neighborId);
                                m_flag=true;
                            }
                            else if(ce.getGatewayRouterId()==neighborId)
                            {
                                int newDist = ne.getDistance() + (ne.getDistance() == Constants.INFINITY ? 0 : 1);

                                if(newDist>ce.getDistance())
                                {
                                    ce.setDistance(newDist);
                                    ce.setGatewayRouterId(neighborId);
                                    m_flag=true;

                                    for(int nid: this.neighborRouterIDs)
                                    {
                                        if (nid != neighborId) {
                                            Router r = NetworkLayerServer.routerMap.get(nid);
                                            if (r.state) {
                                                r.sfupdateRoutingTable(this);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return m_flag;
    }

    /**
     * If the state was up, down it; if state was down, up it
     */
    public synchronized void revertState() {
        state = !state;
        if(state) { initiateRoutingTable(); }
        else { clearRoutingTable(); }
    }

    public int getRouterId() {
        return routerId;
    }

    public void setRouterId(int routerId) {
        this.routerId = routerId;
    }

    public int getNumberOfInterfaces() {
        return numberOfInterfaces;
    }

    public void setNumberOfInterfaces(int numberOfInterfaces) {
        this.numberOfInterfaces = numberOfInterfaces;
    }

    public ArrayList<IPAddress> getInterfaceAddresses() {
        return interfaceAddresses;
    }

    public void setInterfaceAddresses(ArrayList<IPAddress> interfaceAddresses) {
        this.interfaceAddresses = interfaceAddresses;
        numberOfInterfaces = interfaceAddresses.size();
    }

    public ArrayList<RoutingTableEntry> getRoutingTable() {
        return routingTable;
    }

    public void addRoutingTableEntry(RoutingTableEntry entry) {
        this.routingTable.add(entry);
    }

    public ArrayList<Integer> getNeighborRouterIDs() {
        return neighborRouterIDs;
    }

    public void setNeighborRouterIDs(ArrayList<Integer> neighborRouterIDs) { this.neighborRouterIDs = neighborRouterIDs; }

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

    public Map<Integer, IPAddress> getGatewayIDtoIP() { return gatewayIDtoIP; }

    public void printRoutingTable() {
        System.out.println("Router " + routerId);
        System.out.println("DestID Distance Nexthop");
        for (RoutingTableEntry routingTableEntry : routingTable) {
            System.out.println(routingTableEntry.getRouterId() + " " + routingTableEntry.getDistance() + " " + routingTableEntry.getGatewayRouterId());
        }
        System.out.println("-----------------------");
    }
    public String strRoutingTable() {
        String string = "Router" + routerId + "\n";
        string += "DestID Distance Nexthop\n";
        for (RoutingTableEntry routingTableEntry : routingTable) {
            string += routingTableEntry.getRouterId() + " " + routingTableEntry.getDistance() + " " + routingTableEntry.getGatewayRouterId() + "\n";
        }

        string += "-----------------------\n";
        return string;
    }

}
