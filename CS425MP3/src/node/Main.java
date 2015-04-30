package node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

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
			nodes[i]=new Node(i,Integer.parseInt(args[0]),Integer.parseInt(args[1]),Integer.parseInt(args[2]),Integer.parseInt(args[3]));
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
		long start=new Date().getTime();
		while((new Date().getTime()-start)<(Integer.parseInt(args[2])*1000))
		{
			Thread.sleep(1000);
		}
		System.exit(0);
		
	}
	
}
