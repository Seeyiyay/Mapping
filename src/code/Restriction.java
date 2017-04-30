package code;

public class Restriction {

	public int id;
	public Node node1;
	public Road segment1;
	public Node node2;
	public Road segment2;
	public Node node3;

	public Restriction(int id, Graph graph, int nodeId1, int segmentId1, int nodeId2, int segmentId2, int nodeId3) {
		this.id = id;
		this.node1 = graph.nodes.get(nodeId1);
		this.segment1 = graph.roads.get(segmentId1);
		this.node2 = graph.nodes.get(nodeId2);
		this.segment2 = graph.roads.get(segmentId2);
		this.node3 = graph.nodes.get(nodeId3);
	}

}
