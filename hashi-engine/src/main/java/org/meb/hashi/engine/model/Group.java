package org.meb.hashi.engine.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class Group {

	private static int idx = 0;

	private String code;
	private int degree;
	private Collection<Node> nodes;
	private Collection<Node> gatewayNodes;
	private boolean active = true;

	public Group() {
		code = Integer.toString(idx++);
		nodes = new HashSet<Node>();
		gatewayNodes = new HashSet<Node>();
	}

	public String getCode() {
		return code;
	}

	public int degree() {
		return degree;
	}

	public Collection<Node> getNodes() {
		return Collections.unmodifiableCollection(nodes);
	}

	public int getNodesCount() {
		return nodes.size();
	}

	public Collection<Node> getGateways() {
		return Collections.unmodifiableCollection(gatewayNodes);
	}

	public int getGatewaysCount() {
		return gatewayNodes.size();
	}

	public void addNode(Node node) {
		assert !nodes.contains(node);

		node.group(this);
		nodes.add(node);
	}

	public void addNodes(Collection<Node> nodes) {
		for (Node node : nodes) {
			addNode(node);
		}
	}

	public Collection<Node> removeNodes() {
		Collection<Node> out = nodes;
		for (Node node : out) {
			node.group(null);
		}
		nodes = null;
		return out;
	}

	public Group update() {
		degree = 0;
		gatewayNodes.clear();
		for (Node node : nodes) {
			if (node.isNotComplete()) {
				degree += node.degree();
				gatewayNodes.add(node);
			}
		}
		return this;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	@Override
	public String toString() {
		return new StringBuilder("code=").append(code).toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((code == null) ? 0 : code.hashCode());
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
		Group other = (Group) obj;
		if (code == null) {
			if (other.code != null)
				return false;
		} else if (!code.equals(other.code))
			return false;
		return true;
	}
}
