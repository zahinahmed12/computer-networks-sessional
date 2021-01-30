import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

//Work needed
public class Client {
    public static void main(String[] args) throws InterruptedException {
        double average_drop_rate = 0;
        int clientCount = 10;
        for (int i=0; i<clientCount; i++) {

            average_drop_rate += simulateClient();
        }
        System.out.println("\n\nAverage Drop Rate: " + average_drop_rate / clientCount);
    }
    private static double simulateClient () {
        NetworkUtility networkUtility = new NetworkUtility("127.0.0.1", 4444);
        System.out.println("Connected to server");
        /**
         * Tasks
         */
        
        /*
        1. Receive EndDevice configuration from server
        2. Receive active client list from server
        3. for(int i=0;i<100;i++)
        4. {
        5.      Generate a random message
        6.      Assign a random receiver from active client list
        7.      if(i==20)
        8.      {
        9.            Send the message and recipient IP address to server and a special request "SHOW_ROUTE"
        10.           Display routing path, hop count and routing table of each router [You need to receive
                            all the required info from the server in response to "SHOW_ROUTE" request]
        11.     }
        12.     else
        13.     {
        14.           Simply send the message and recipient IP address to server.
        15.     }
        16.     If server can successfully send the message, client will get an acknowledgement along with hop count
                    Otherwise, client will get a failure message [dropped packet]
        17. }
        18. Report average number of hops and drop rate
        */
        EndDevice e=(EndDevice) networkUtility.read();
        ArrayList<EndDevice> arr=(ArrayList<EndDevice>) networkUtility.read();

        int len=arr.size();
        Random random=new Random(System.currentTimeMillis());

        ArrayList<Integer> hopcounts=new ArrayList<>();

        for(int i=0;i<100;i++)
        {
            int id=Math.abs(random.nextInt(len));

            /*Scanner sc=new Scanner(System.in);
            String s1=sc.nextLine();
            Scanner sc2=new Scanner(System.in);
            String s2=sc.nextLine();

            Packet p=new Packet(s1,s2,e.getIpAddress(),arr.get(id).getIpAddress());*/

            Packet packet=new Packet("Hi there, "+i+1+" !","",e.getIpAddress(),arr.get(id).getIpAddress());

            if(i==20)
            {
                packet.setSpecialMessage("SHOW_ROUTE");
                networkUtility.write(packet);

                ArrayList<Router> router_on_path=(ArrayList<Router>) networkUtility.read();

                System.out.print("Path: ");
                for(Router r:router_on_path)
                {
                    System.out.print(r.getRouterId()+" ");
                }
                System.out.println();

                System.out.println("Tables:");
                for(Router r:router_on_path)
                {
                    System.out.println("RouterID-> "+r.getRouterId()+" : ");
                    System.out.println("DestinationID Distance GatewayID");
                    for(RoutingTableEntry entry: r.getRoutingTable())
                    {
                        System.out.println("       "+entry.getRouterId()+"      "+entry.getDistance()+"        "+entry.getGatewayRouterId());
                    }
                }

            }
            else
            {
                networkUtility.write(packet);
            }

            String show_msg=(String) networkUtility.read();
            int hop_count=(int) networkUtility.read();
            hopcounts.add(hop_count);

            System.out.println(show_msg + " -> hop_count: " + hop_count);
//            System.out.println(hop_count);
        }

        float avg_hop_count=0;
        int drop_rate=0;
        for(Integer i:hopcounts)
        {
            if(i!=-1) avg_hop_count+=i;
            else drop_rate++;
        }
        System.out.println("Average number of hops: "+avg_hop_count/(100-drop_rate));
        System.out.println("Drop rate: "+drop_rate/100.0);

        return drop_rate/100.0;
    }
}
