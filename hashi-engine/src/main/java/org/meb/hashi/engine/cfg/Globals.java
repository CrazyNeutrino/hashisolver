package org.meb.hashi.cfg;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.meb.hashi.model.Coords;
import org.meb.hashi.model.Node;
import org.meb.hashi.model.Side;

public class Globals {

	private final int X_MIN = 0;
	private final int Y_MIN = 0;

	private int xMax;
	private int yMax;

	private Map<Coords, Node> nodeAtMap;

	public Globals(int xMax, int yMax, Node[] nodes) {
		assert xMax > 0;
		assert yMax > 0;

		if (nodeAtMap == null) {
			HashMap<Coords, Node> tmpNodeAtMap = new HashMap<Coords, Node>();
			for (Node node : nodes) {
				assert (!tmpNodeAtMap.containsKey(node.coords));
				tmpNodeAtMap.put(node.coords, node);
			}
			nodeAtMap = Collections.unmodifiableMap(tmpNodeAtMap);
		}
		this.xMax = xMax;
		this.yMax = xMax;
	}

	public int xMin() {
		return X_MIN;
	}

	public int yMin() {
		return Y_MIN;
	}

	public int xMax() {
		return xMax;
	}

	public int yMax() {
		return yMax;
	}

	public Map<Coords, Node> nodeAtMap() {
		return nodeAtMap;
	}

	public Node nodeAt(Coords coords) {
		return nodeAtMap.get(coords);
	}

	public Node nodeAt(int x, int y) {
		return nodeAt(new Coords(x, y));
	}

	public void fillNeighbourNodes(Node[] vs) {
		for (Node node : vs) {
			for (Side side : Side.values()) {
				if (node.neighbour(side) == null) {
					node.neighbour(side, findNode(side, node));
				}
			}
		}
	}

	public Node findNode(Side side, Node node) {
		switch (side) {
			case NORTH:
				return findNodeNorth(node);
			case EAST:
				return findNodeEast(node);
			case SOUTH:
				return findNodeSouth(node);
			case WEST:
				return findNodeWest(node);
			default:
				return null;
		}
	}

	public Node findNodeNorth(Node node) {
		Node vNorth;
		for (int y = node.coords.y - 1; y >= Y_MIN; y--) {
			vNorth = nodeAt(node.coords.x, y);
			if (vNorth != null) {
				return vNorth;
			}
		}
		return null;
	}

	public Node findNodeSouth(Node node) {
		Node vSouth;
		for (int y = node.coords.y + 1; y <= yMax; y++) {
			vSouth = nodeAt(node.coords.x, y);
			if (vSouth != null) {
				return vSouth;
			}
		}
		return null;
	}

	public Node findNodeEast(Node node) {
		Node vEast;
		for (int x = node.coords.x + 1; x <= xMax; x++) {
			vEast = nodeAt(x, node.coords.y);
			if (vEast != null) {
				return vEast;
			}
		}
		return null;
	}

	public Node findNodeWest(Node node) {
		Node vWest;
		for (int x = node.coords.x - 1; x >= X_MIN; x--) {
			vWest = nodeAt(x, node.coords.y);
			if (vWest != null) {
				return vWest;
			}
		}
		return null;
	}
}
