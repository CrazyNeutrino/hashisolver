package org.meb.hashi.engine.model;

import java.util.HashSet;
import java.util.Set;

import org.meb.hashi.engine.schedule.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class State {

	private static final Logger log = LoggerFactory.getLogger(State.class);

	private Node[] nodes;
	private Edge[] edges;
	private Set<Group> groups;
	private Node lastSolvedNode;

	public State(Node[] nodes) {
		this.nodes = nodes;
		this.groups = new HashSet<Group>();

		for (Node node : this.nodes) {
			assert !groups.contains(node.group());
			groups.add(node.group());
		}
	}

	public Node[] getNodes() {
		return nodes;
	}

	public Edge[] getEdges() {
		return edges;
	}

	public Set<Group> getGroups() {
		return groups;
	}

	public Node getLastSolvedNode() {
		return lastSolvedNode;
	}

	public void setLastSolvedNode(Node lastSolvedNode) {
		this.lastSolvedNode = lastSolvedNode;
	}

	public void updateEdges() {
		HashSet<Edge> uniqueEdges = new HashSet<Edge>();

		for (Node node : nodes) {
			for (Side side : Side.values()) {
				Edge edge = node.edge(side);
				if (edge != null && !uniqueEdges.contains(edge)) {
					uniqueEdges.add(edge);
				}
			}
		}
		edges = uniqueEdges.toArray(new Edge[uniqueEdges.size()]);
	}

	public void debugPrintNodes() {
		for (Node node : nodes) {
			log.info("node=({})", node);
		}
	}

	public void debugPrintEdges() {
		for (Edge edge : edges) {
			log.info("edge=({})", edge);
		}
	}

	public void connectFully(Node node, Side side, Scheduler scheduler) {
		Node neighbour = node.neighbour(side);

		assert neighbour != null;
		assert neighbour.neighbour(side.opposite()).equals(node);

		Edge edge = node.edge(side);
		boolean newEdge = false;
		if (edge == null) {
			newEdge = true;
			int edgeDegree = node.sideDegree(side);
			edge = new Edge(edgeDegree, node, neighbour);
			node.edge(side, edge);
			neighbour.edge(side.opposite(), edge);
			mergeGroups(node.group(), neighbour.group());

			log.info("connect edge=({})", edge);
		} else {
			int edgeDegree = Math.min(node.degree(), neighbour.degree());
			edgeDegree = Math.min(edgeDegree, 2);
			// edge.increaseDegree(edgeDegree);
			edge.increaseDegree(node.sideDegree(side));

			log.info("increase edge=({})", edge);
		}
		edge.setComplete(true);

		if (newEdge) {
			clearNeighbours(edge, scheduler);
		}
	}

	public void connectPartially(Node node, Side side, Scheduler scheduler) {
		Node neighbour = node.neighbour(side);
		Edge edge = node.edge(side);

		assert neighbour != null;
		assert neighbour.neighbour(side.opposite()).equals(node);
		assert edge == null;

		boolean complete = node.initialDegree() == 2 && neighbour.initialDegree() == 2;
		edge = new Edge(1, node, neighbour, complete);
		node.edge(side, edge);
		neighbour.edge(side.opposite(), edge);
		mergeGroups(node.group(), neighbour.group());

		log.info("connect edge=({})", edge);

		clearNeighbours(edge, scheduler);
	}

	private void clearNeighbours(Edge edge, Scheduler scheduler) {
		if (edge.isHorizontal()) {
			int y = edge.node1().position.y;
			int xStart = Math.min(edge.node1().position.x, edge.node2().position.x) + 1;
			int xEnd = Math.max(edge.node1().position.x, edge.node2().position.x) - 1;

			for (Node node : nodes) {
				if (node.isComplete() || node.position.x < xStart || node.position.x > xEnd) {
					continue;
				}
				if (node.position.y > y) {
					Node neighbour = node.neighbour(Side.NORTH);
					if (neighbour != null && neighbour.position.y < y) {
						clearNeighbours(node, Side.NORTH);
						schedule(scheduler, new Node[] { node, neighbour }, edge.node1());
					}
				}
				if (node.position.y < y) {
					Node neighbour = node.neighbour(Side.SOUTH);
					if (neighbour != null && neighbour.position.y > y) {
						clearNeighbours(node, Side.SOUTH);
						schedule(scheduler, new Node[] { node, neighbour }, edge.node1());
					}
				}
			}
		} else if (edge.isVertical()) {
			int x = edge.node1().position.x;
			int yStart = Math.min(edge.node1().position.y, edge.node2().position.y) + 1;
			int yEnd = Math.max(edge.node1().position.y, edge.node2().position.y) - 1;

			for (Node node : nodes) {
				if (node.isComplete() || node.position.y < yStart || node.position.y > yEnd) {
					continue;
				}
				if (node.position.x > x) {
					Node neighbour = node.neighbour(Side.WEST);
					if (neighbour != null && neighbour.position.x < x) {
						clearNeighbours(node, Side.WEST);
						schedule(scheduler, new Node[] { node, neighbour }, edge.node1());
					}
				}
				if (node.position.x < x) {
					Node neighbour = node.neighbour(Side.EAST);
					if (neighbour != null && neighbour.position.x > x) {
						clearNeighbours(node, Side.EAST);
						schedule(scheduler, new Node[] { node, neighbour }, edge.node1());
					}
				}
			}
		}
	}

	public void clearNeighbours(Node node, Side side) {
		Node neighbour = node.neighbour(side);

		assert neighbour != null;
		assert neighbour.neighbour(side.opposite()).equals(node);

		log.info("clear neighbours node=({}), neighbour=({})", node, neighbour);
		node.neighbour(side, null);
		neighbour.neighbour(side.opposite(), null);
	}

	private void schedule(Scheduler scheduler, Node[] nodes, Node trigger) {
		if (scheduler != null) {
			for (Node node : nodes) {
				// for (int i = nodes.length - 1; i >= 0; i--) {
				// Node node = nodes[i];
				scheduler.schedule(node, trigger);
			}
		}
	}

	public void mergeGroups(Group g1, Group g2) {
		assert groups.contains(g1) : g1;
		assert groups.contains(g2) : g2;

		log.info("merge groups g1=({}), g2=({})", g1, g2);

		if (g1.equals(g2)) {
			return;
		}

		Group target;
		Group source;
		if (g1.getNodesCount() >= g2.getNodesCount()) {
			target = g1;
			source = g2;
		} else {
			target = g2;
			source = g1;
		}

		groups.remove(source);
		source.setActive(false);
		target.addNodes(source.removeNodes());
	}
}
