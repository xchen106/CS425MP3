package node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class Main {
	public static int deadLock(int[][] waitForGraph,int pos,int target,boolean checked)
	{
		if(pos==target&&checked)
			return target;
		checked=true;
		int ret=-1;
		for(int i=0;i<9;i++)
		{
			if(waitForGraph[pos][i]==1)
			{
				waitForGraph[pos][i]=0;
				ret=deadLock(waitForGraph,i,target,checked);
				waitForGraph[pos][i]=1;
				if(ret>=0)
					break;
			}
		}
		return ret;
	}
	public static void main(String[] args) throws InterruptedException
	{
		Client[] clients=new Client[9];
		Node[] nodes=new Node[9];
		for(int i=0;i<9;i++)
		{
			nodes[i]=new Node(i,Integer.parseInt(args[0]),Integer.parseInt(args[1]),Integer.parseInt(args[2]));
			Thread.sleep(100);
		}
		for(int i=0;i<9;i++)
		{
			clients[i]=new Client(i,nodes[i]);
			clients[i].initilialization();
		}
		for(int i=0;i<9;i++)
		{
			clients[i].start();
		}
		while(true)
		{
//			System.out.println("********************************");
//			for(int i=0;i<9;i++)
//				nodes[i].printNodeStatus();
//			System.out.println("********************************");
//			int[][] waitForGraph=new int[9][9];
//			for(int i=0;i<9;i++)
//			{
//				for(int key:nodes[i].receivedReplys.keySet())
//				{
//					if(nodes[key].grantedMessage!=null)
//					{
//						if(i==nodes[key].grantedMessage.From)
//						{
//							continue;
//						}
//							
//						waitForGraph[i][nodes[key].grantedMessage.From]=1;
//					}
//						
//				}
//			}
//			ArrayList<Node> abandonNodes=new ArrayList<Node>();
//			for(int i=0;i<9;i++)
//			{
//				int abandonProcess=deadLock(waitForGraph,i,i,false);
//				if(abandonProcess>=0)
//				{
//					System.out.println("Deadlock in "+abandonProcess);
//					abandonNodes.add(nodes[abandonProcess]);
//					
//				}
//			}
//			if(abandonNodes.size()>0)
//			{
//				Collections.sort(abandonNodes, new NodeComparator());
//				nodes[abandonNodes.get(abandonNodes.size()-1).identifier].abandon=true;
//			}
			Thread.sleep(1000);
		}
		
	}
	
}
