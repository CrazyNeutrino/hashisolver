package org.meb.hashi.schedule;

import java.util.LinkedList;
import java.util.Queue;

import org.meb.hashi.model.Node;

public class BasicScheduler implements Scheduler {

	private Node[] all;
	private Queue<Node> pendingQueue = new LinkedList<Node>();
	private int idx = 0;

	public BasicScheduler(Node[] all) {
		this.all = all;
		tryPrepareNext();
	}

	public void schedule(Node pending, Node trigger) {
		pendingQueue.offer(pending);
	}

	public boolean hasNext() {
		Node node = pendingQueue.peek();
		if (node == null) {
			tryPrepareNext();
			node = pendingQueue.peek();
			if (node == null) {
				return false;
			}
		}
		return true;
	}

	public Node next() {
		Node node = pendingQueue.poll();
		if (node == null) {
			tryPrepareNext();
			node = pendingQueue.poll();
		}
		return node;
	}

	private void tryPrepareNext() {
		if (idx < all.length) {
			pendingQueue.offer(all[idx++]);
		}
	}

	public void reset() {
		idx = 0;
	}
}
