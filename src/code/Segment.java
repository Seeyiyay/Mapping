package code;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

/**
 * A Segment is the most interesting class making up our graph, and represents
 * an edge between two Nodes. It knows the Road it belongs to as well as the
 * Nodes it joins, and contains a series of Locations that make up the length of
 * the Segment and can be used to render it.
 *
 * @author tony
 */
public class Segment {

	public final Road road;
	public final Node start, end;
	public final double length;
	public final Location[] points;
	public Color color = new Color(130, 130, 130);

	public Segment(Graph graph, int roadID, double length, int node1ID,
			int node2ID, double[] coords) {

		this.road = graph.roads.get(roadID);
		this.start = graph.nodes.get(node1ID);
		this.end = graph.nodes.get(node2ID);
		this.length = length;

		if(this.start == null || this.end == null) {
			System.out.println("Major mistake");
		}
		this.start.neighbours.add(this.end);
		this.end.neighbours.add(this.start);

		points = new Location[coords.length / 2];
		for (int i = 0; i < points.length; i++) {
			points[i] = Location
					.newFromLatLon(coords[2 * i], coords[2 * i + 1]);
		}

		this.road.addSegment(this);
		if(this.road.oneWay == 0) {
			this.start.addOutSegment(this);
			this.start.addInSegment(this);
			this.end.addInSegment(this);
			this.end.addOutSegment(this);
		}
		else {
			this.start.addOutSegment(this);
			this.end.addInSegment(this);
		}
	}

	public void draw(Graphics g, Location origin, double scale) {
		for (int i = 1; i < points.length; i++) {
			Point p = points[i - 1].asPoint(origin, scale);
			Point q = points[i].asPoint(origin, scale);
			g.setColor(this.color);
			g.drawLine(p.x, p.y, q.x, q.y);
		}
	}
}
