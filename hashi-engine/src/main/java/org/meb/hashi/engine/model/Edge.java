package org.meb.hashi.model;

public class Edge {

	private int degree;
	private final Node node1;
	private final Node node2;
	private boolean complete;

	public Edge(int degree, Node node1, Node node2) {
		this(degree, node1, node2, true);
	}

	public Edge(int degree, Node node1, Node node2, boolean complete) {
		assert (degree > 0 && degree <= 2);
		this.degree = degree;
		assert (node1 != null);
		this.node1 = node1;
		assert (node2 != null);
		this.node2 = node2;
		this.complete = complete;
	}

	public int degree() {
		return degree;
	}

	public void increaseDegree() {
		increaseDegree(1);
	}

	public void increaseDegree(int increase) {
		assert degree + increase <= 2 : this.toString() + ", increase=" + increase;
		degree += increase;
		// degree = increase;
	}

	public Node node1() {
		return node1;
	}

	public Node node2() {
		return node2;
	}

	public boolean isVertical() {
		return node1.coords.x == node2.coords.x;
	}

	public boolean isHorizontal() {
		return node1.coords.y == node2.coords.y;
	}

	public boolean isComplete() {
		return complete;
	}

	public void setComplete(boolean complete) {
		this.complete = complete;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((node1 == null) ? 0 : node1.hashCode());
		result = prime * result + ((node2 == null) ? 0 : node2.hashCode());
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
		Edge other = (Edge) obj;
		if (node1 == null) {
			if (other.node1 != null)
				return false;
		} else if (!node1.equals(other.node1))
			return false;
		if (node2 == null) {
			if (other.node2 != null)
				return false;
		} else if (!node2.equals(other.node2))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return new StringBuilder("coords1=(").append(node1.coords).append("), coords2=(")
				.append(node2.coords).append("), degree=").append(degree).append(", complete=")
				.append(complete).toString();
	}
}
