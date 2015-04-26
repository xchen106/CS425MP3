package node;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.PriorityQueue;
import java.util.concurrent.Semaphore;

public class Node {
	boolean granted;
	Message grantedMessage;
	PriorityQueue<Message> waitingQueue;
	int identifier;
	int initTime;
	int nextReqTime;
	int totalTime;
	String host;
	volatile Thread blinker;
	Server myServer;
	Hashtable<Integer, Socket> mySockets;
	Hashtable<Integer, Integer> receivedReplys;
	int receivedMessages;
	long timeStamp;
	int state;// 0 released, 1 wanted, 2 held
	boolean sentInquire;

	public Node(int identifier, int time1, int time2, int time3)
			throws InterruptedException {
		this.identifier = identifier;
		this.initTime = time1;
		this.nextReqTime = time2;
		this.totalTime = time3;
		granted = false;
		myServer = new Server(this, identifier + 8000);
		myServer.start();
		receivedMessages = 0;
		waitingQueue = new PriorityQueue<Message>(9, new MessageComparator());
		this.host = "0.0.0.0";
		mySockets = new Hashtable<Integer, Socket>();
		receivedReplys = new Hashtable<Integer, Integer>();
		state = 0;
		sentInquire = false;
	}

	public void printNodeStatus() {
		if (granted)
			System.out.print("Node " + identifier + " " + timeStamp
					+ " granted :" + grantedMessage.From + ", waiting list: ");
		else
			System.out.print("Node " + identifier + " " + timeStamp
					+ " not granted, waiting list: ");
		for (Message mm : waitingQueue) {
			System.out.print(mm.From + " ");
		}
		System.out.print(", Got reply from: ");
		int count = 0;
		for (int key : receivedReplys.keySet()) {
			if (receivedReplys.get(key) == 1)
				System.out.print(key + " ");
			if (receivedReplys.get(key) >= 0)
				count++;
		}
		System.out.println("Received messages: " + count);
		System.out.println();

	}

	public synchronized void printNode(Message m) {
		System.out.println("Node " + identifier + " received " + m.getName());
		System.out.print("Waiting queue: ");
		for (Message mm : waitingQueue) {
			System.out.print(mm.From + " ");
		}
		System.out.println();
	}

	public void handleMessages(String line) {
		Message m = new Message();
		try {
			m = m.stringToMessage(line);
		} catch (ClassNotFoundException | ParseException | IOException e) {
			e.printStackTrace();
		}
		// printNodeStatus();
		// printNode(m);
		switch (m.Operation) {
		case 0:
			onReceiveRequest(m);
			break;
		case 1:
			onReceiveGrant(m);
			break;
		case 2:
			onReceiveInquire(m);
			break;
		case 4:
			onReceiveYield(m);
			break;
		case 5:
			onReceiveRelease(m);
			break;
		}
	}
	public synchronized void onReceiveRelease(Message m) {

		// Grant the top requester
		Message reply = new Message();
		Message topQueue;
		synchronized (this) {
			topQueue = waitingQueue.poll();

		}
		if (topQueue == null) {
			System.out.println("I am " + this.identifier
					+ ", I received a release from " + m.From);
			synchronized (this) {
				granted = false;
				grantedMessage = null;
			}
		} else {
			System.out.println("I am " + this.identifier
					+ ", I received a release and granted " + topQueue.From);
			reply.From = this.identifier;
			reply.To = topQueue.From;// The top requester
			reply.Operation = 1;// Grant
			grantedMessage = topQueue;
			granted = true;
			sendMessage(reply, reply.To);
		}
	}
	
	public synchronized void onReceiveInquire(Message m)
	{
		if(state!=2)
		{
			receivedMessages--;
			receivedReplys.put(m.From, -1);
			Message yield=new Message();
			yield.From = this.identifier;
			yield.To = m.From;
			yield.Operation = 4;// Yield
			sendMessage(yield, yield.To);
		}
	}
	public synchronized void onReceiveYield(Message m) {
		
		//Put the grantedMessage into the queue again
		waitingQueue.add(grantedMessage);
		
		// Grant the top requester
		Message reply = new Message();
		Message topQueue;
		topQueue = waitingQueue.poll();

		if (topQueue == null) {
			
			synchronized (this) {
				granted = false;
				grantedMessage = null;
			}
		} else {
		
			reply.From = this.identifier;
			reply.To = topQueue.From;// The top requester
			reply.Operation = 1;// Grant
			grantedMessage = topQueue;
			granted = true;
			sentInquire=false;
			sendMessage(reply, reply.To);
		}
		

	}

	public synchronized void onReceiveRequest(Message m) {
		if (!granted && !(state == 2))// Not yet granted permission to anyone
		{
			System.out.println("I am " + this.identifier + ", I am granting "
					+ m.From + " " + granted);
			Message reply = new Message();
			reply.From = this.identifier;
			reply.To = m.From;
			reply.Operation = 1;// Grant
			grantedMessage = m;
			granted = true;
			sendMessage(reply, reply.To);

		} else {
			if(new MessageComparator().compare(grantedMessage,m)>=0&&!sentInquire)
			{
				Message inquire = new Message();
				inquire.From = this.identifier;
				inquire.To = grantedMessage.From;
				inquire.Operation = 2;// Inquire
				waitingQueue.add(m);
				sentInquire=true;
				sendMessage(inquire, inquire.To);
			}
			else
				waitingQueue.add(m);
		}
	}

	public synchronized void onReceiveGrant(Message m) {
		receivedMessages++;
		receivedReplys.put(m.From, 1);
	}

	public void sendMessage(Message m, int destination) {
		try {
			// m.print();
			Socket socket = mySockets.get(destination);

			if (socket == null)
				System.out.println("No socket from " + this.identifier + " to "
						+ destination);
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					socket.getOutputStream()));
			out.write(m.messageToString() + "\n");
			out.flush();
			// System.out.print("Is socket closed? "+socket.isClosed());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public class MessageComparator implements Comparator<Message> {
		@Override
		public int compare(Message x, Message y) {
			if (x.TimeStamp.equals(y.TimeStamp))
				return x.From - y.From;
			return (int) (x.TimeStamp - y.TimeStamp);
		}
	}
	

}
