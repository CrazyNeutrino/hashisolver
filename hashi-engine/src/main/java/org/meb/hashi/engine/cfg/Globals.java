package org.meb.hashi.engine.cfg;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.meb.hashi.engine.model.Node;
import org.meb.hashi.engine.model.Position;
import org.meb.hashi.engine.model.Side;

public class Globals {

	private final int X_MIN = 0;
	private final int Y_MIN = 0;

	private int xMax;
	private int yMax;

	private Map<Position, Node> nodeAtMap;

	public Globals(int xMax, int yMax, Node[] nodes) {
		assert xMax > 0;
		assert yMax > 0;

		if (nodeAtMap == null) {
			HashMap<Position, Node> tmpNodeAtMap = new HashMap<Position, Node>();
			for (Node node : nodes) {
				assert (!tmpNodeAtMap.containsKey(node.position));
				tmpNodeAtMap.put(node.position, node);
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

	public Map<Position, Node> nodeAtMap() {
		return nodeAtMap;
	}

	public Node nodeAt(Position position) {
		return nodeAtMap.get(position);
	}

	public Node nodeAt(int x, int y) {
		return nodeAt(new Position(x, y));
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
		for (int y = node.position.y - 1; y >= Y_MIN; y--) {
			vNorth = nodeAt(node.position.x, y);
			if (vNorth != null) {
				return vNorth;
			}
		}
		return null;
	}

	public Node findNodeSouth(Node node) {
		Node vSouth;
		for (int y = node.position.y + 1; y <= yMax; y++) {
			vSouth = nodeAt(node.position.x, y);
			if (vSouth != null) {
				return vSouth;
			}
		}
		return null;
	}

	public Node findNodeEast(Node node) {
		Node vEast;
		for (int x = node.position.x + 1; x <= xMax; x++) {
			vEast = nodeAt(x, node.position.y);
			if (vEast != null) {
				return vEast;
			}
		}
		return null;
	}

	public Node findNodeWest(Node node) {
		Node vWest;
		for (int x = node.position.x - 1; x >= X_MIN; x--) {
			vWest = nodeAt(x, node.position.y);
			if (vWest != null) {
				return vWest;
			}
		}
		return null;
	}
}
