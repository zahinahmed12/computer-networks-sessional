import java.util.Scanner;

public class Client {
    public static void main(String[] args) {

        Scanner sc= new Scanner(System.in);

        while(true)
        {
            System.out.println("Enter the name of the file you want to upload");
            String str=sc.nextLine();

            Client_Thread cl=new Client_Thread(str);

            Thread t=new Thread(cl);

            t.start();
        }

    }
}
