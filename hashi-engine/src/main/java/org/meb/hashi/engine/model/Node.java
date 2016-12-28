package org.meb.hashi.engine.model;

import org.apache.commons.lang3.ArrayUtils;

public class Node {

	public final Position position;
	private final int initialDegree;
	private int degree;
	private final Edge[] edges = new Edge[4];
	private final Node[] neighbours = new Node[4];
	private final Integer[] limits = new Integer[4];
	private int neighbourCount = 0;
	private Group group;

	public Node(int x, int y, int initialDegree) {
		this.position = new Position(x, y);
		this.initialDegree = initialDegree;
		this.degree = initialDegree;
		new Group().addNode(this);

	}

	public int degree() {
		return degree;
	}

	public int initialDegree() {
		return initialDegree;
	}

	public int edgeDegree(Side side) {
		Edge edge = edges[side.ordinal()];
		return edge == null ? 0 : edge.degree();
	}

	public int neighbourDegree(Side side) {
		Node neighbour = neighbours[side.ordinal()];
		return neighbour == null ? 0 : neighbour.degree();
	}

	public int sideDegree(Side side) {
		Edge edge = edges[side.ordinal()];
		if (edge != null && edge.isComplete()) {
			return 0;
		} else {
			int ed = edgeDegree(side);
			int nd = neighbourDegree(side);
			int d = Math.min(nd + ed, 2) - ed;
			d = Math.min(d, degree);
			Integer limit = limits[side.ordinal()];
			if (limit != null) {
				d = Math.min(d, limit.intValue());
			}
			return d;
		}
	}
	
	public int sideDegreeTotal() {
		int sideDegreeTotal = 0;
		for (Side side : Side.values()) {
			sideDegreeTotal += sideDegree(side);
		}
		return sideDegreeTotal;
	}
	

	public int[] sideDegreeOthers() {
		int[] ds = new int[4];
		for (Side side : Side.values()) {
			int d = sideDegree(side);
			for (int i = 0; i < 4; i++) {
				if (i != side.ordinal()) {
					ds[i] += d;
				}
			}
		}
		return ds;
	}

	public Edge edge(Side side) {
		return edges[side.ordinal()];
	}

	public void edge(Side side, Edge edge) {
		edges[side.ordinal()] = edge;
	}

	public Node neighbour(Side side) {
		return neighbours[side.ordinal()];
	}

	public Node firstNeighbour() {
		return nthNeighbour(0);
	}

	public Node secondNeighbour() {
		return nthNeighbour(1);
	}

	private Node nthNeighbour(int n) {
		assert n >= 0 && n <= 3;

		int i = 0;
		for (Node neighbour : neighbours) {
			if (neighbour != null) {
				if (i++ == n) {
					return neighbour;
				}
			}
		}
		return null;
	}

	public Node nextNeighbour(Node previous) {
		int idx = ArrayUtils.indexOf(neighbours, previous);
		for (int i = idx + 1; i < neighbours.length; i++) {
			Node neighbour = neighbours[i];
			if (neighbour != null) {
				return neighbour;
			}
		}
		return null;
	}

	public void neighbour(Side side, Node neighbour) {
		if (neighbours[side.ordinal()] == null && neighbour != null) {
			neighbourCount++;
		} else if (neighbours[side.ordinal()] != null && neighbour == null) {
			neighbourCount--;
		}
		neighbours[side.ordinal()] = neighbour;
	}

	public int neighbourCount() {
		return neighbourCount;
	}

	public Node update() {
		updateDegree();
		updateNeighbours();
		updateEdges();
		return this;
	}

	private void updateDegree() {
		degree = initialDegree;
		for (Edge edge : edges) {
			if (edge != null) {
				degree -= edge.degree();
				assert degree >= 0 : this;
			}
		}
	}

	private void updateNeighbours() {
		if (isComplete()) {
			for (Side s : Side.values()) {
				Node neighbour = neighbour(s);
				if (neighbour != null) {
					neighbour(s, null);
					neighbour.neighbour(s.opposite(), null);
				}
			}
		}
	}

	private void updateEdges() {
		if (isComplete()) {
			for (Side s : Side.values()) {
				Edge edge = edges[s.ordinal()];
				if (edge != null && !edge.isComplete()) {
					edge.setComplete(true);
				}
			}
		}
	}

	@Override
	public String toString() {
		return new StringBuilder("pos=(").append(position.toString()).append("), initialDegree=")
				.append(initialDegree).append(", degree=").append(degree).append(", group=(")
				.append(group).append(")").toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((position == null) ? 0 : position.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		if (position == null) {
			if (other.position != null)
				return false;
		} else if (!position.equals(other.position))
			return false;
		return true;
	}

	public boolean isComplete() {
		return degree == 0;
	}

	public boolean isNotComplete() {
		return degree != 0;
	}

	public Group group() {
		return group;
	}

	public void group(Group group) {
		this.group = group;
	}

	public void limitSideDegree(Side side, int limit) {
		Edge edge = edges[side.ordinal()];

		assert edge == null || edge.degree() <= limit;

		limits[side.ordinal()] = limit;
		if (edge != null && edge.degree() == limit) {
			edge.setComplete(true);
		}
	}
}
