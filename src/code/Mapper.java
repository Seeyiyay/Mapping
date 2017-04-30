package code;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Stack;

/**
 * This is the main class for the mapping program. It extends the GUI abstract
 * class and implements all the methods necessary, as well as having a main
 * function.
 *
 * @author tony
 */
public class Mapper extends GUI {
	public static final Color NODE_COLOUR = new Color(77, 113, 255);
	public static final Color SEGMENT_COLOUR = new Color(130, 130, 130);
	public static final Color HIGHLIGHT_COLOUR = new Color(255, 219, 77);

	// these two constants define the size of the node squares at different zoom
	// levels; the equation used is node size = NODE_INTERCEPT + NODE_GRADIENT *
	// log(scale)
	public static final int NODE_INTERCEPT = 1;
	public static final double NODE_GRADIENT = 0.8;

	// defines how much you move per button press, and is dependent on scale.
	public static final double MOVE_AMOUNT = 100;
	// defines how much you zoom in/out per button press, and the maximum and
	// minimum zoom levels.
	public static final double ZOOM_FACTOR = 1.3;
	public static final double MIN_ZOOM = 1, MAX_ZOOM = 200;

	// how far away from a node you can click before it isn't counted.
	public static final double MAX_CLICKED_DISTANCE = 0.15;

	// these two define the 'view' of the program, ie. where you're looking and
	// how zoomed in you are.
	private Location origin;
	private double scale;

	// our data structures.
	private Graph graph;
	private Trie trie;

	//A*Search fields.
	private boolean isSecond = false;
	private Node startNode;
	private Node endNode;
	private ArrayList<ASearchResult> outputList;

	public double pathDistance;



	@Override
	protected void redraw(Graphics g) {
		if (graph != null)
			graph.draw(g, getDrawingAreaDimension(), origin, scale);
//		for(Node n : graph.articulationPoints) {
//			n.color = Mapper.HIGHLIGHT_COLOUR;
//			n.draw(g, getDrawingAreaDimension(), origin, scale);
//		}
	}

	@Override
	protected void onClick(MouseEvent e) {
		Location clicked = Location.newFromPoint(e.getPoint(), origin, scale);
		// find the closest node.
		double bestDist = Double.MAX_VALUE;
		Node closest = null;

		for (Node node : graph.nodes.values()) {
			double distance = clicked.distance(node.location);
			if (distance < bestDist) {
				bestDist = distance;
				closest = node;
			}
		}

		// if it's close enough, highlight it and show some information.
		if (clicked.distance(closest.location) < MAX_CLICKED_DISTANCE) {
			graph.setHighlight(closest);
			getTextOutputArea().setText(closest.toString());
		}

		if(isSecond == false) {
			startNode = closest;
			endNode = null;
			isSecond = true;
		}
		else {
			endNode = closest;
			isSecond = false;
		}
		SearchNode pathEndNode = aSearchNormal(startNode, endNode);
		if(pathEndNode != null) {
			for(Segment s : this.graph.segments) {
				s.color = new Color(130, 130, 130);
			}
			getTextOutputArea().setText("All roads for the path: " + "\n");
			this.outputList = new ArrayList<ASearchResult>();
			setASearchPath(pathEndNode.getNode());
			Collections.reverse(outputList);
			for(int i = outputList.size()-1; i > 0 ; i--) {
				if(outputList.get(i).markedForDeletion == true) {
					outputList.get(i-1).distance = outputList.get(i-1).distance + outputList.get(i).distance;
					outputList.remove(i);
				}
			}
			if(outputList.get(1).roadName.equals(outputList.get(0).roadName)) {
				outputList.get(0).distance = outputList.get(0).distance + outputList.get(1).distance;
				outputList.remove(1);
			}
			String textOutput = "";
			double totalDistance = 0;
			for(ASearchResult r : outputList) {
				textOutput = textOutput + "Road name: " + r.roadName + "    Distance: " + r.distance + "\n";
				totalDistance = totalDistance + r.distance;
			}
			getTextOutputArea().setText(textOutput);
			getTextOutputArea().append("Total Distance :" + totalDistance);
		}
	}

	@Override
	protected void onSearch() {
		if (trie == null)
			return;

		// get the search query and run it through the trie.
		String query = getSearchBox().getText();
		Collection<Road> selected = trie.get(query);

		// figure out if any of our selected roads exactly matches the search
		// query. if so, as per the specification, we should only highlight
		// exact matches. there may be (and are) many exact matches, however, so
		// we have to do this carefully.
		boolean exactMatch = false;
		for (Road road : selected)
			if (road.name.equals(query))
				exactMatch = true;

		// make a set of all the roads that match exactly, and make this our new
		// selected set.
		if (exactMatch) {
			Collection<Road> exactMatches = new HashSet<>();
			for (Road road : selected)
				if (road.name.equals(query))
					exactMatches.add(road);
			selected = exactMatches;
		}

		// set the highlighted roads.
		graph.setHighlight(selected);

		// now build the string for display. we filter out duplicates by putting
		// it through a set first, and then combine it.
		Collection<String> names = new HashSet<>();
		for (Road road : selected)
			names.add(road.name);
		String str = "";
		for (String name : names)
			str += name + "; ";

		if (str.length() != 0)
			str = str.substring(0, str.length() - 2);
		getTextOutputArea().setText(str);
	}

	@Override
	protected void onMove(Move m) {
		if (m == GUI.Move.NORTH) {
			origin = origin.moveBy(0, MOVE_AMOUNT / scale);
		} else if (m == GUI.Move.SOUTH) {
			origin = origin.moveBy(0, -MOVE_AMOUNT / scale);
		} else if (m == GUI.Move.EAST) {
			origin = origin.moveBy(MOVE_AMOUNT / scale, 0);
		} else if (m == GUI.Move.WEST) {
			origin = origin.moveBy(-MOVE_AMOUNT / scale, 0);
		} else if (m == GUI.Move.ZOOM_IN) {
			if (scale < MAX_ZOOM) {
				// yes, this does allow you to go slightly over/under the
				// max/min scale, but it means that we always zoom exactly to
				// the centre.
				scaleOrigin(true);
				scale *= ZOOM_FACTOR;
			}
		} else if (m == GUI.Move.ZOOM_OUT) {
			if (scale > MIN_ZOOM) {
				scaleOrigin(false);
				scale /= ZOOM_FACTOR;
			}
		}
	}

	@Override
	protected void onLoad(File nodes, File roads, File segments, File polygons, File restrictions) {
		graph = new Graph(nodes, roads, segments, polygons, restrictions);
		trie = new Trie(graph.roads.values());
		origin = new Location(-250, 250); // close enough
		scale = 1;
	}

	/**
	 * This method does the nasty logic of making sure we always zoom into/out
	 * of the centre of the screen. It assumes that scale has just been updated
	 * to be either scale * ZOOM_FACTOR (zooming in) or scale / ZOOM_FACTOR
	 * (zooming out). The passed boolean should correspond to this, ie. be true
	 * if the scale was just increased.
	 */
	private void scaleOrigin(boolean zoomIn) {
		Dimension area = getDrawingAreaDimension();
		double zoom = zoomIn ? 1 / ZOOM_FACTOR : ZOOM_FACTOR;

		int dx = (int) ((area.width - (area.width * zoom)) / 2);
		int dy = (int) ((area.height - (area.height * zoom)) / 2);

		origin = Location.newFromPoint(new Point(dx, dy), origin, scale);
	}

	public static void main(String[] args) {
		new Mapper();
	}

	/**
	 * An A*Search algorithm.
	 * @param start The node to start the search from.
	 * @param destination The node we are trying to find.
	 */
	public SearchNode aSearchNormal(Node start, Node destination) {
		Comparator<SearchNode> comparator = new Comparator<SearchNode>() {
	        @Override
	        public int compare(SearchNode o1, SearchNode o2) {
	            if (o1.getEstimate() > o2.getEstimate()) {
	                return 1;
	            }
	            else {
	            	return -1;
	            }
	        }
	    };

		if(startNode == null || endNode == null) {
			return null;
		}
		for(Map.Entry<Integer, Node> n : this.graph.nodes.entrySet()) {
			n.getValue().setIsVisited(false);
			n.getValue().setFrom(null);
		}
		ArrayList<SearchNode> path = new ArrayList<SearchNode>();
		PriorityQueue<SearchNode> fringe = new PriorityQueue<SearchNode>(10, comparator);
		double startToEnd = start.location.distance(destination.location);
		SearchNode starting = new SearchNode(start, null, 0, startToEnd);
		fringe.add(starting);
		while(!fringe.isEmpty()) {
			SearchNode currentNode = fringe.remove();
			path.add(currentNode);
			if(!currentNode.getNode().getIsVisited()) {
				currentNode.getNode().setIsVisited(true);
				if(currentNode.getNode().nodeID == (destination.nodeID)) {
					return currentNode;
				}
				for(Segment e : currentNode.getNode().outSegments) {
					Node edgeNode;
					if(e.end.nodeID == currentNode.getNode().nodeID) {
						edgeNode = e.start;
					}
					else {
						edgeNode = e.end;
					}

					if(!edgeNode.getIsVisited()) {
						double costToNeigh = currentNode.getLength() + currentNode.getNode().location.distance(edgeNode.location);
						double estTotal = costToNeigh + edgeNode.location.distance(destination.location);
						edgeNode.setFrom(currentNode.getNode());
						fringe.add(new SearchNode(edgeNode, currentNode.getNode(), costToNeigh, estTotal));
					}
				}
			}
		}
		return null;
	}

	public SearchNode aSearchTime(Node start, Node destination) {
		Comparator<SearchNode> comparator = new Comparator<SearchNode>() {
	        @Override
	        public int compare(SearchNode o1, SearchNode o2) {
	            if (o1.getEstimate() > o2.getEstimate()) {
	                return 1;
	            }
	            else {
	            	return -1;
	            }
	        }
	    };

	    Node restNode1 = null;
	    Node restNode2 = null;
	    int restId = 0;
		if(startNode == null || endNode == null) {
			return null;
		}
		for(Map.Entry<Integer, Node> n : this.graph.nodes.entrySet()) {
			n.getValue().setIsVisited(false);
			n.getValue().setFrom(null);
		}
		ArrayList<SearchNode> path = new ArrayList<SearchNode>();
		PriorityQueue<SearchNode> fringe = new PriorityQueue<SearchNode>(10, comparator);
		double startToEnd = start.location.distance(destination.location)/110;
		SearchNode starting = new SearchNode(start, null, 0, startToEnd);
		fringe.add(starting);
		Node oldNode = null;
		while(!fringe.isEmpty()) {
			SearchNode currentNode = fringe.remove();
			path.add(currentNode);
			if(!currentNode.getNode().getIsVisited()) {
				currentNode.getNode().setIsVisited(true);
				if(currentNode.getNode().nodeID == (destination.nodeID)) {
					return currentNode;
				}
				for(Segment e : currentNode.getNode().outSegments) {
					Node edgeNode;
					if(e.end.nodeID == currentNode.getNode().nodeID) {
						edgeNode = e.start;
					}
					else {
						edgeNode = e.end;
					}
					boolean restricted = false;
					for(Restriction r : this.graph.restrictions) {
						if(oldNode != null && oldNode.equals(r.node1) && r.node3.equals(edgeNode) && r.node2.equals(currentNode.getNode())) {
							restricted = true;
						}
					}
					if(!edgeNode.getIsVisited() && restricted == false) {
						double costToNeigh = (currentNode.getLength() + currentNode.getNode().location.distance(edgeNode.location))/(e.road.speed);
						double estTotal = costToNeigh + edgeNode.location.distance(destination.location)/110;
						edgeNode.setFrom(currentNode.getNode());
						fringe.add(new SearchNode(edgeNode, currentNode.getNode(), costToNeigh, estTotal));
					}
				}
				oldNode = currentNode.getNode();
			}
		}
		return null;
	}

	public void setASearchPath(Node endNode) {
		if(null == endNode.getFrom()) {
			getTextOutputArea().append("Total path distance: " + pathDistance);
			return;
		}
		else {

			for(Segment e : endNode.inSegments) {
				if(endNode.getFrom().inSegments.contains(e) || endNode.getFrom().outSegments.contains(e)) {
					e.color = Color.RED;
					e.draw(drawing.getGraphics(), origin, scale);

					boolean toDelete = false;
					double currentRoadDistance = endNode.location.distance(endNode.getFrom().location);
					if(outputList.isEmpty() == false && outputList.get(outputList.size()-1).roadName.contains(e.road.name)) {
						toDelete = true;
					}
					outputList.add(new ASearchResult(e.road.name, currentRoadDistance, toDelete));
//					if(!getTextOutputArea().getText().contains(e.road.name)) {
//						double currentRoadDistance = endNode.location.distance(endNode.getFrom().location);
//						pathDistance = pathDistance + currentRoadDistance;
//						getTextOutputArea().append(e.road.name + "             " + "Distance of this road:" + currentRoadDistance + "\n");
//					}
				}
			}
		}
		setASearchPath(endNode.getFrom());
	}

	public void articulateIterFirst(Node start) {
		for(Map.Entry<Integer, Node> n : this.graph.nodes.entrySet()) {
			n.getValue().depth = Integer.MAX_VALUE;
		}
		start.depth = 0;
		int numSubTrees = 0;
		for(Node neighbour : start.getNeighbours()) {
			if(neighbour.depth == Integer.MAX_VALUE && neighbour.getIsVisited() == false) {
				articulateIterParts(neighbour, start);
				numSubTrees = numSubTrees + 1;
			}
		}
		if(numSubTrees > 1) {
			this.graph.articulationPoints.add(start);
		}
	}

	public void articulateIterParts(Node firstNode, Node root) {
		Stack<Element> articulationStack = new Stack<Element>();
		articulationStack.push(new Element(firstNode, 1, new Element(root, 0, null)));
		while(!articulationStack.isEmpty()) {
			Element elem = articulationStack.peek();
			Node node = elem.node;
			if(elem.children == null) {
				node.depth = elem.depth;
				elem.reach = elem.depth;
				elem.children = new LinkedList<Node>();
				for(Node neighbour : node.getNeighbours()) {
					if(neighbour != elem.parent.node) {
						elem.children.add(neighbour);
					}
				}
			}
			else if(elem.children != null && elem.children.isEmpty() == false) {
				Node child = elem.children.poll();
				if(child.depth < Integer.MAX_VALUE) {
					if(child.depth < elem.reach) {
						elem.reach = child.depth;
					}
				}
				else {
					child.setIsVisited(true);
					articulationStack.push(new Element(child, node.depth + 1, elem));
				}
			}
			else {
				if(node.nodeID != firstNode.nodeID) {
					if(elem.reach > elem.parent.depth) {
						this.graph.articulationPoints.add(elem.parent.node);
					}
					if(elem.reach < elem.parent.reach) {
						elem.parent.reach = elem.reach;
					}
				}
				articulationStack.pop();
			}

		}
	}





	protected void articulateRecurFirst(Node start) {
		for(Map.Entry<Integer, Node> n : this.graph.nodes.entrySet()) {
			n.getValue().depth = Integer.MAX_VALUE;
		}
		int subtrees = 0;
		for(Node neigh : start.getNeighbours()) {
			if(neigh.depth == Integer.MAX_VALUE && neigh.getIsVisited() == false) {
				articulationRecurParts(neigh, 1, start);
				subtrees = subtrees + 1;
			}
		}
		if(subtrees > 1) {
			this.graph.articulationPoints.add(start);
		}
	}

	public int articulationRecurParts(Node node, int depth, Node from) {
		node.depth = depth;
		int reachBack = depth;
		for(Node neigh : node.getNeighbours()) {
			if(neigh.nodeID != from.nodeID && neigh.getIsVisited() == false) {
				if(neigh.depth < Integer.MAX_VALUE) {
					if(neigh.depth < reachBack) {
						reachBack = neigh.depth;
					}
				}
				else {
					neigh.setIsVisited(true);
					int childReach = articulationRecurParts(neigh, depth + 1, node);
					if(childReach >= depth) {
						this.graph.articulationPoints.add(node);
					}
					if(childReach < reachBack) {
						reachBack = childReach;
					}
				}
			}
		}
		return reachBack;
	}


	@Override
	protected void articulateIterStart() {
		for(Node n : this.graph.nodes.values()) {
			n.setIsVisited(false);
		}
		this.graph.articulationPoints = new HashSet<Node>();
		for(Map.Entry<Integer, Node> n : this.graph.nodes.entrySet()) {
			if(n.getValue().getIsVisited() == false) {
				articulateIterFirst(n.getValue());
			}
		}
//		System.out.println(this.graph.articulationPoints.size());
		for(Node n: this.graph.articulationPoints) {
			n.color = HIGHLIGHT_COLOUR;
		}
	}

	@Override
	protected void articulateRecurStart() {
		for(Map.Entry<Integer, Node> n : this.graph.nodes.entrySet()) {
			n.getValue().setIsVisited(false);
		}
		this.graph.articulationPoints = new HashSet<Node>();
		for(Map.Entry<Integer, Node> n : this.graph.nodes.entrySet()) {
			if(n.getValue().getIsVisited() == false) {
				articulateRecurFirst(n.getValue());
			}
		}
//		System.out.println(this.graph.articulationPoints.size());
		for(Node n: this.graph.articulationPoints) {
			n.color = HIGHLIGHT_COLOUR;
		}
	}

}
