package code;

import java.util.Queue;

public class Element {

	public Node node;
	public int reach;
	public Element parent;
	public int depth;
	public Queue<Node> children;

	public Element(Node node, int depth, Element parent) {
		this.node = node;
		this.depth = depth;
		this.parent = parent;
	}

}
