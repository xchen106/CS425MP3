package node;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;



public class Server extends Thread{
	// default port number
	int port = 9090;
	// machine index
	char index;
	// node
	Node node;
    ServerSocket listener = null;
    Thread runningThread = null;
    
    // initialize the port number
    public Server(Node node,int port){
    	this.port = port;
    	this.node = node;
    }
	
	public void run(){
		// synchronize
		synchronized(this){
            this.runningThread = Thread.currentThread();
        }
		
		// open server socket
		try {
            this.listener = new ServerSocket(this.port);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open port "+this.port, e);
        }
        
		// wait for request
        while(true){
            Socket clientSocket = null;
            try {
                clientSocket = this.listener.accept();
            } catch (IOException e) {
            	throw new RuntimeException("Error connecting", e);
            }
            // start a new thread to handle the request
            new Thread(new Listener(clientSocket, this.node)).start();
        }
        
	}
	
}
