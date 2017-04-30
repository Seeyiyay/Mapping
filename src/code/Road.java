package code;

import java.util.Collection;
import java.util.HashSet;

/**
 * Road represents ... a road ... in our graph, which is some metadata and a
 * collection of Segments. We have lots of information about Roads, but don't
 * use much of it.
 *
 * @author tony
 */
public class Road {
	public final int roadID;
	public final String name, city;
	public final Collection<Segment> components;
	public final int oneWay;
	public final int speed;

	public Road(int roadID, int type, String label, String city, int oneway,
			int speed, int roadclass, int notforcar, int notforpede,
			int notforbicy) {
		this.roadID = roadID;
		this.city = city;
		this.name = label;
		this.oneWay = oneway;
		if(speed == 0) {
			this.speed = 5;
		}
		else if(speed == 1) {
			this.speed = 20;
		}
		else if(speed == 2) {
			this.speed = 40;
		}
		else if(speed == 3) {
			this.speed = 60;
		}
		else if(speed == 4) {
			this.speed = 80;
		}
		else if(speed == 5) {
			this.speed = 100;
		}
		else if(speed == 6) {
			this.speed = 110;
		}
		else if(speed == 7) {
			this.speed = 110;
		}
		else {
			this.speed = 110;
		}
		this.components = new HashSet<Segment>();

	}

	public void addSegment(Segment seg) {
		components.add(seg);
	}
}
