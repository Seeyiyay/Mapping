package code;

public class SearchNode {

	private Node node;
	private Node from;
	private double length;
	private double estimate;

	public SearchNode(Node node, Node from, double length, double estimate) {
		this.node = node;
		this.from = from;
		this.length = length;
		this.estimate = estimate;
	}

	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	public Node getFrom() {
		return from;
	}

	public void setFrom(Node from) {
		this.from = from;
	}

	public double getLength() {
		return length;
	}

	public void setLength(double length) {
		this.length = length;
	}

	public double getEstimate() {
		return estimate;
	}

	public void setEstimate(double estimate) {
		this.estimate = estimate;
	}

//	@Override
//	public int compareTo(SearchNode o) {
//		if(o.getEstimate() > this.getEstimate()) {
//			return -1;
//		}
//		else if(o.getEstimate() < this.getEstimate()) {
//			return 1;
//		}
//		else {
//			return 1;
//		}
//	}

}