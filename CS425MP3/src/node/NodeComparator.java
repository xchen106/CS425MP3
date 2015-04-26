package node;

import java.util.Comparator;

public class NodeComparator implements Comparator<Node>{
	public int compare(Node x, Node y) {
		if (x.timeStamp==y.timeStamp)
			return x.identifier - y.identifier;
		return (int) (x.timeStamp - y.timeStamp);
	}
}
