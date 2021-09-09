import java.net.*;
import java.io.*;

public class BridgeServer{
	public static void main(String[] args) {
		boolean flag = true;
		while(flag){
			// initialisation of the server
			try (ServerSocket serverSocket = new ServerSocket(2024)){
				  Socket socket = serverSocket.accept();
				  ServerTask sT = new ServerTask(socket, Integer.parseInt(args[0]));
					serverSocket.close();
			}catch(Exception e){System.out.println("Server had an exception. See next line:");System.out.println(e);}
		}
	}
}
