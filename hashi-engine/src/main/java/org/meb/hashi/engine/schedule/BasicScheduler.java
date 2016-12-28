package org.meb.hashi.engine.schedule;

import java.util.LinkedList;
import java.util.Queue;

import org.meb.hashi.engine.model.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicScheduler implements Scheduler {

	private static final Logger log = LoggerFactory.getLogger(BasicScheduler.class);

	private Node[] nodes;
	private Queue<Node> scheduledNodes = new LinkedList<Node>();
	private int idx = 0;

	public BasicScheduler(Node[] all) {
		this.nodes = all;
		scheduleInternal();
	}

	public void schedule(Node node, Node trigger) {
		log.info("schedule node=({}), trigger=({}), queue size = {}",
				new Object[] { node, trigger, scheduledNodes.size() });

		scheduledNodes.offer(node);
		reset();
	}

	public boolean hasNext() {
		Node node = scheduledNodes.peek();
		if (node == null) {
			scheduleInternal();
			node = scheduledNodes.peek();
			if (node == null) {
				return false;
			}
		}
		return true;
	}

	public Node next() {
		Node node = scheduledNodes.poll();
		if (node == null) {
			scheduleInternal();
			node = scheduledNodes.poll();
		}
		return node;
	}

	private void scheduleInternal() {
		if (idx < nodes.length) {
			Node node = nodes[idx++];
			scheduledNodes.offer(node);

			log.debug("schedule internal node=({})", node);
		}
	}

	public void reset() {
		idx = 0;
	}
}
