import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;
import java.io.*;

public class Server_Thread implements Runnable {

    Socket s;
    BufferedReader in;
    PrintWriter pr;
    PrintWriter myfile;

    //FileWriter myfile=new FileWriter("C:\\Users\\lenovo\\IdeaProjects\\Http9\\1605057_log.txt");
    //PrintWriter myWrite=new PrintWriter(new FileWriter("myfilw.txt",true));

    public Server_Thread(Socket s, BufferedReader in, PrintWriter pr, PrintWriter myfile)
    {
        this.s=s;
        this.in=in;
        this.pr=pr;
        this.myfile=myfile;
    }

    @Override
    public void run() {

        String input;
        //System.out.println(input);
        try
        {
            //FileWriter myfile=new FileWriter("C:\\Users\\lenovo\\IdeaProjects\\Http9\\1605057_log.txt");

            String content;
            content="";

            //System.out.println(in.ready());
            input=in.readLine();
            //System.out.println(input);

            if(input!=null)
            {
                System.out.println(input);
                StringTokenizer st =new StringTokenizer(input, " ");
                String req= st.nextToken();
                String find_root=st.nextToken();

                File folder= new File(find_root.substring(1));
                //if(input == null) continue;
                if(input.length() > 0) {
                    if(input.startsWith("GET"))
                    {
                        myfile.println("\nHTTP REQUEST: "+input+"\n\n");
                        myfile.println();
                        myfile.flush();
                        //myfile.close();
                        if(find_root.equals("/"))
                        {
                            //System.out.println("hi");

                            content="<html><head>" + "\n\t\t<link rel=\"icon\" href=\"data:,\">\n</head>"+
                                    "\n<body>" ;

                            final File ff= new File("root");
                            for(final File fileEntry : ff.listFiles())
                            {
                                if(fileEntry.isDirectory())
                                {
                                    content+="<b><a href=\"http://localhost:6789/root/"+fileEntry.getName()+"\"" +
                                            ">"+fileEntry.getName()+"</a></b><br>\n";
                                }
                                else
                                {
                                    content+="<a href=\"http://localhost:6789/root/"+fileEntry.getName()+"\"" +
                                            ">"+fileEntry.getName()+"</a><br>\n";
                                }
                            }
                            content+="</body>\n" +
                                    "</html>";

                            pr.write("HTTP/1.1 200 OK\r\n");
                            pr.write("Server: Java HTTP Server: 1.0\r\n");
                            pr.write("Date: " + new Date() + "\r\n");
                            pr.write("Content-Type: text/html\r\n");
                            pr.write("Content-Length: " + content.length() + "\r\n");
                            pr.write("\r\n");
                            pr.write(content);
                            pr.flush();

                            myfile.println("HTTP RESPONSE: \n");
                            myfile.println("HTTP/1.1 200 OK\r\n");
                            myfile.write("Server: Java HTTP Server: 1.0\r\n");
                            myfile.write("Date: " + new Date() + "\r\n");
                            myfile.write("Content-Type: text/html\r\n");
                            myfile.write("Content-Length: " + content.length() + "\r\n");
                            myfile.write("\r\n");
                            myfile.write(content);
                            myfile.println();
                            myfile.println();
                            myfile.flush();


                        }

                        else if(folder.isDirectory())
                        {
                            //System.out.println("hi");
                            content="";
                            content="<html><head>" + "\t\t<link rel=\"icon\" href=\"data:,\">\n</head>"+
                                    "<body>";

                            for (final File fileEntry : folder.listFiles()) {
                                //System.out.println(fileEntry.getName());
                                if(fileEntry.isDirectory())
                                {
                                    content+="<b><a href=\"http://localhost:6789"+find_root+"/"+fileEntry.getName()+"\"" +
                                            ">"+fileEntry.getName()+"</a></b><br>";
                                }
                                else
                                {
                                    content+="<a href=\"http://localhost:6789"+find_root+"/"+fileEntry.getName()+"\"" +
                                            ">"+fileEntry.getName()+"</a><br>";
                                }
                            }
                            content+="</body>" +
                                    "</html>";

                            pr.write("HTTP/1.1 200 OK\r\n");
                            pr.write("Server: Java HTTP Server: 1.0\r\n");
                            pr.write("Date: " + new Date() + "\r\n");
                            pr.write("Content-Type: text/html\r\n");
                            pr.write("Content-Length: " + content.length() + "\r\n");
                            pr.write("\r\n");
                            pr.write(content);
                            pr.flush();

                            myfile.println("HTTP RESPONSE: \n");
                            myfile.println("HTTP/1.1 200 OK\r\n");
                            myfile.write("Server: Java HTTP Server: 1.0\r\n");
                            myfile.write("Date: " + new Date() + "\r\n");
                            myfile.write("Content-Type: text/html\r\n");
                            myfile.write("Content-Length: " + content.length() + "\r\n");
                            myfile.write("\r\n");
                            myfile.write(content);
                            myfile.println();
                            myfile.println();
                            myfile.flush();

                        }
                        else if (folder.isFile())
                        {
                       /*content="<html>" +
                                "<body>" +
                                "<a href=\"http://localhost:6789"+find_root+"/\"" +
                                //"<h1>This file is being downloaded <h1>"+
                                " download>"+folder.getName()+"</a><br>" +
                                "</body>" +
                                "</html>";*/
                            myfile.println("HTTP RESPONSE: \n");
                            myfile.println("HTTP/1.1 200 OK\r\n");
                            myfile.write("Server: Java HTTP Server: 1.0\r\n");
                            myfile.write("Date: " + new Date() + "\r\n");
                            myfile.write("Content-Type: text/html\r\n");
                            //pr.write("Content-Disposition: attachment\r\n");
                            myfile.write("Content-Length: " + folder.length() + "\r\n");
                            myfile.write("Content-type: application/x-force-download \r\n");

                            myfile.write("\r\n");
                            myfile.println();
                            myfile.println();
                            myfile.flush();

                            pr.write("HTTP/1.1 200 OK\r\n");
                            pr.write("Server: Java HTTP Server: 1.0\r\n");
                            pr.write("Date: " + new Date() + "\r\n");
                            pr.write("Content-Type: text/html\r\n");
                            //pr.write("Content-Disposition: attachment\r\n");
                            pr.write("Content-Length: " + folder.length() + "\r\n");
                            pr.write("Content-type: application/x-force-download \r\n");

                            pr.write("\r\n");
                            //pr.write(content);
                            pr.flush();

                            int count;
                            byte[] buffer = new byte[1024];

                            OutputStream out = this.s.getOutputStream();
                            BufferedInputStream inp = new BufferedInputStream(new FileInputStream(folder));
                            while ((count = inp.read(buffer)) > 0) {
                                out.write(buffer, 0, count);
                                out.flush();
                            }

                        }
                        else
                        {
                            //System.out.println("hi");
                            content="<html><head> <title> 404 NOT FOUND </title>" + "\t\t<link rel=\"icon\" href=\"data:,\">\n</head>"+
                                    "<body>";
                            content+="<h1>404 Not Found </h1>";
                            content+="<p> The requested "+find_root+" was not found on this server";
                            content+="</body>" +
                                    "</html>";

                            pr.write("HTTP/1.1 404 PAGE NOT FOUND\r\n");
                            pr.write("Server: Java HTTP Server: 1.0\r\n");
                            pr.write("Date: " + new Date() + "\r\n");
                            pr.write("Content-Type: text/html\r\n");
                            pr.write("Content-Length: " + content.length() + "\r\n");
                            pr.write("\r\n");
                            pr.write(content);
                            pr.flush();

                            myfile.println("HTTP RESPONSE: \n");
                            myfile.println("HTTP/1.1 404 PAGE NOT FOUND\r\n");
                            myfile.write("Server: Java HTTP Server: 1.0\r\n");
                            myfile.write("Date: " + new Date() + "\r\n");
                            myfile.write("Content-Type: text/html\r\n");
                            myfile.write("Content-Length: " + content.length() + "\r\n");
                            myfile.write("\r\n");
                            myfile.write(content);
                            myfile.println();
                            myfile.println();
                            myfile.flush();

                        }
                        myfile.close();

                    }

                    else
                    {
                        //System.out.println("reached");
                       // pr.write("got it");
                        File file= new File(find_root);
                        if(!file.exists())
                        {
                            System.out.println("\nTHIS GIVEN FILE DOESN'T EXIST\n");
                        }
                        else
                        {
                            byte[] buffer = new byte[1024];
                            int count = 0;

                            //System.out.println("HERE");
                            BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                            bw.write("got it\n");
                            bw.flush();

                            InputStream stream = s.getInputStream();
                            FileOutputStream fos = new FileOutputStream("root/"+find_root);
                            //BufferedInputStream newin = new BufferedInputStream(new FileInputStream(file));

                            while((count = stream.read(buffer)) > 0)
                            {
                                //System.out.println("START");
                                fos.write(buffer, 0, count);
                                fos.flush();
                            }
                            //System.out.println("END");

                            stream.close();
                            in.close();
                            bw.close();
                            fos.close();
                        }

                        //System.out.println("done");

                        //InputStream inp= s.getInputStream();
                       /* final File file=new File(find_root);

                        int cc=1;

                        content="<html><head>" + "\t\t<link rel=\"icon\" href=\"data:,\">\n</head>"+
                                "<body>";
                        content+="<a href=\"http://localhost:6789/root"+"/\"" +
                                ">"+"new_file"+cc+"</a><br>";
                        content+="</body>" +
                                "</html>";

                        cc++;

                        pr.write("HTTP/1.1 200 OK\r\n");
                        pr.write("Server: Java HTTP Server: 1.0\r\n");
                        pr.write("Date: " + new Date() + "\r\n");
                        pr.write("Content-Type: text/html\r\n");
                        pr.write("Content-Length: " + content.length() + "\r\n");
                        pr.write("\r\n");
                        pr.write(content);
                        pr.flush();

                        int count;
                        byte[] buffer = new byte[1024];

                        OutputStream out = this.s.getOutputStream();
                        BufferedInputStream inp = new BufferedInputStream(new FileInputStream(file));
                        while ((count = inp.read(buffer)) > 0) {
                            out.write(buffer, 0, count);
                            out.flush();
                        }*/



                    }
                    //System.out.println(input);
                }

                s.close();

            }


        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
