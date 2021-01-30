import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer {

    static final int PORT = 6789;

    public static void main(String[] args) throws IOException {

        ServerSocket serverConnect = new ServerSocket(PORT);
        System.out.println("Server started.\nListening for connections on port : " + PORT + " ...\n");

        while (true)
        {
            Socket s=serverConnect.accept();
            //System.out.println("connection made");
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            PrintWriter pr = new PrintWriter(s.getOutputStream());

            PrintWriter myfile=new PrintWriter(new FileWriter("1605057_log.txt",true));

            Server_Thread new_req= new Server_Thread(s, in, pr, myfile);

            Thread t= new Thread(new_req);

            t.start();
        }
    }

}
