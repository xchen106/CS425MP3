package node;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Message implements Serializable{
	
	/*
	 * operation code
	 * 0: REQUEST
	 * 1: GRANT
	 * 2: INQUIRE
	 * 3: FAIL
	 * 4: YIELD
	 * 5: RELEASE
	 */
	public int Operation;
	public int From;
	public int To;
	public int Key;
	public String Content;
	public Long TimeStamp;
	public Date dateTime;
	private static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	private static String[] OperationNames={"REQUEST","GRANT","INQUIRE","FAIL","YIELD","RELEASE"};
	public Message()
	{
		dateTime = new Date();
		TimeStamp=new Date().getTime();
	}
	public String getDateTime()
	{
		return dateFormat.format(dateTime);
	}
	public String getName()
	{
		return OperationNames[this.Operation];
	}
	public void print()
	{
		System.out.print("Message from "+this.From+" to "+this.To+" for ");
		System.out.println(getName());
		System.out.println("operation = "+ this.Operation + ", from="+this.From + ", to="+this.To);
	}
	
	public String messageToString() throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        oos.writeObject(this );
        oos.close();
		return new String( Base64Coder.encode( baos.toByteArray() ) );
	}
	public Message stringToMessage(String s) throws ParseException, IOException, ClassNotFoundException
	{
		byte [] data = Base64Coder.decode( s );
	    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream( data ) );
	    Message m  = (Message) ois.readObject();
	    ois.close();
	        
		return m;
	}
	public static void main(String[] args) throws IOException, ClassNotFoundException, ParseException
	{
		System.out.println(new Date().getTime());
		ArrayList<String> test=new ArrayList<String>();
		for(int i=0;i<10;i++)
			test.add("hello");
		for(String s: test)
		{
			if(s.equals("hello"))
				test.remove(s);
		}
	}
}
