package code;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Node represents an intersection in the road graph. It stores its ID and its
 * location, as well as all the segments that it connects to. It knows how to
 * draw itself, and has an informative toString method.
 *
 * @author tony
 */
public class Node {

	public final int nodeID;
	public final Location location;
	public final Collection<Segment> inSegments;
	public final Collection<Segment> outSegments;
	//A*Search fields.
	private boolean isVisited = false;
	private Node from;
	//Articulation fields
	public int depth = 0;
	public HashSet<Node> neighbours = new HashSet<Node>();
	public Color color = Mapper.NODE_COLOUR;

	public Node(int nodeID, double lat, double lon) {
		this.nodeID = nodeID;
		this.location = Location.newFromLatLon(lat, lon);
		this.inSegments = new HashSet<Segment>();
		this.outSegments = new HashSet<Segment>();
	}

	public void addInSegment(Segment seg) {
		inSegments.add(seg);
	}

	public void addOutSegment(Segment seg) {
		outSegments.add(seg);
	}

	public void draw(Graphics g, Dimension area, Location origin, double scale) {
		Point p = location.asPoint(origin, scale);

		// for efficiency, don't render nodes that are off-screen.
		if (p.x < 0 || p.x > area.width || p.y < 0 || p.y > area.height)
			return;

		int size = (int) (Mapper.NODE_GRADIENT * Math.log(scale) + Mapper.NODE_INTERCEPT);
		g.setColor(this.color);
		g.fillRect(p.x - size / 2, p.y - size / 2, size, size);
	}

	public String toString() {
		Set<String> edges = new HashSet<String>();
		for (Segment s : inSegments) {
			if (!edges.contains(s.road.name))
				edges.add(s.road.name);
		}

		for (Segment s : outSegments) {
			if (!edges.contains(s.road.name))
				edges.add(s.road.name);
		}

		String str = "ID: " + nodeID + "  loc: " + location + "\nroads: ";
		for (String e : edges) {
			str += e + ", ";
		}
		return str.substring(0, str.length() - 2);
	}

	public HashSet<Node> getNeighbours() {
		return this.neighbours;
//		HashSet<Node> neighbours = new HashSet<Node>();
//		for(Segment s : inSegments) {
//			if(s.start.nodeID != this.nodeID && neighbours.contains(s.start) == false) {
//				neighbours.add(s.start);
//			}
//			if(s.end.nodeID != this.nodeID && neighbours.contains(s.end) == false) {
//				neighbours.add(s.end);
//			}
//		}
//		for(Segment s : outSegments) {
//			if(s.start.nodeID != this.nodeID && neighbours.contains(s.start) == false) {
//				neighbours.add(s.start);
//			}
//			if(s.end.nodeID != this.nodeID && neighbours.contains(s.end) == false) {
//				neighbours.add(s.end);
//			}
//		}
//		System.out.println(neighbours.size());
//		return neighbours;
	}

	public boolean getIsVisited() {
		return isVisited;
	}

	public void setIsVisited(boolean isVisited) {
		this.isVisited = isVisited;
	}

	public Node getFrom() {
		return from;
	}

	public void setFrom(Node from) {
		this.from = from;
	}

}
