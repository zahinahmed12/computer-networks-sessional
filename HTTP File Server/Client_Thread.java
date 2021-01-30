import java.io.*;
import java.net.Socket;
import java.util.StringTokenizer;

public class Client_Thread implements Runnable {

    String s;

    public Client_Thread(String s)
    {
        this.s=s;
    }

    @Override
    public void run() {

        try{
            Socket ss=new Socket("127.0.0.1",6789);
            File folder= new File(s);

            s="UPLOAD "+s+"\n";

            /*BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(ss.getOutputStream()));
            bw.write(s);
            bw.flush();*/
            PrintWriter pr = new PrintWriter(ss.getOutputStream());
            pr.write(s);
            pr.flush();

            if(folder.exists())
            {
                /*PrintWriter pr = new PrintWriter(ss.getOutputStream());
                pr.write(s);
                pr.flush();*/

                StringTokenizer st =new StringTokenizer(s, " ");
                String req= st.nextToken();
                String find_root=st.nextToken();

                BufferedReader br= new BufferedReader(new InputStreamReader(ss.getInputStream()));
                String income=br.readLine();

                if(income.equalsIgnoreCase("got it"))
                {
                    int count;
                    byte[] buffer = new byte[1024];

                    OutputStream out = ss.getOutputStream();
                    BufferedInputStream inp = new BufferedInputStream(new FileInputStream(folder));
                    while ((count = inp.read(buffer)) > 0) {
                        out.write(buffer, 0, count);
                        out.flush();
                    }

                    inp.close();
                    pr.close();
                    out.close();
                }

                //File folder= new File(find_root);


            }
            else
            {
                System.out.println("\nTHE GIVEN FILE DOESN'T EXIST\n");
                //ss.close();
            }
            ss.close();

            //ObjectInputStream inps=new ObjectInputStream(ss.getInputStream());
            //String inp2= (String)inps.readObject();
            //System.out.println(inp2);

        }
        catch (IOException  e)
        {
            e.printStackTrace();
        }



    }
}
