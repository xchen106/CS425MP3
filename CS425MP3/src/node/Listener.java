package node;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.text.ParseException;

public class Listener implements Runnable {

	Socket clientSocket = null;
	Node node;

	public Listener(Socket clientSocket, Node node) {
		this.clientSocket = clientSocket;
		this.node = node;
	}

	public void run() {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					clientSocket.getInputStream()));
			String line = in.readLine();
			while (true) { // read input and let node to run it
//				System.out.println("In listener, reading line: "+line);
				if(line==null)
				{
					line=in.readLine();
					continue;
				}
				Message m = new Message();
				m = m.stringToMessage(line);
				// m.print();
				node.handleMessages(line);
				line = in.readLine();

			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} 
	}

}
