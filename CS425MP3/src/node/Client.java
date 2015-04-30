package node;

import java.io.IOException;
import java.net.Socket;
import java.util.Date;
import java.util.Hashtable;

public class Client extends Thread {
	Node node;
	volatile Thread blinker;
	int identifier;
	public Client(int identifier,Node n)
	{
		this.identifier=identifier;
		this.node=n;
	}
	public void start() {
		blinker = new Thread(this);
		blinker.start();
		
	}
	public void initilialization()
	{
		//generate quorums and initialize channels
		for(int i=(identifier/3)*3;i<(identifier/3)*3+3;i++)
		{
			
			while(true)
			{
				try {
					Socket socket = new Socket(node.host, 8000 + i);	
					node.mySockets.put(i, socket);
					break;
				}
				catch (IOException e) {
					System.out.println("Socket fail! "+ this.identifier);
					e.printStackTrace();
					continue;
				}
			}
		}
		for(int i=(identifier%3);i<9;i+=3)
		{
			
			while(true)
			{
				try {
					Socket socket = new Socket(node.host, 8000 + i);	
					node.mySockets.put(i, socket);
					break;
				}
				catch (IOException e) {
					System.out.println("Socket fail!");
					continue;
				}
			}
		}
	}
	public void run() {
		
		while(true)
		{
			synchronized(this)
			{
				node.receivedMessages=0;
				node.receivedReplys=new Hashtable<Integer,Integer>();
				node.state=1;
				
			}
			long timeStamp=new Date().getTime();
			node.timeStamp=timeStamp;
			for(int destination: node.mySockets.keySet())
			{
				
				node.receivedReplys.put(destination, -1);
				Message m=new Message();
				m.From=this.identifier;
				m.To=destination;
				m.TimeStamp=timeStamp;
				m.Operation=0;//Request
				node.sendMessage(m,m.To);
			}
			
			try {
				//Block on waiting for messages
				while(node.receivedMessages< node.mySockets.size())
				{
					Thread.sleep(100);
				}
				
				System.out.print(new Date().getTime()+" ,node "+identifier+" is in critical section! Granted by ");
				for(int key:node.receivedReplys.keySet())
					if(node.receivedReplys.get(key)==1)
						System.out.print(key+" ");
				System.out.println();
				//Enter critical section
				node.state=2;
				Thread.sleep(node.initTime*1000);
			}
				
			 catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			 }
			
			//Release 
			for(int destination: node.mySockets.keySet())
			{
				if(node.receivedReplys.get(destination)!=1)
					continue;
				Message m=new Message();
				m.From=this.identifier;
				m.To=destination;
				m.Operation=5;//Release
				node.state=0;
				node.sendMessage(m,m.To);
			}
			try {
				//Wait for next round
				Thread.sleep(node.nextReqTime*1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}

}
