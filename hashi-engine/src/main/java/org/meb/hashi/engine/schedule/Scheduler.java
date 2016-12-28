package org.meb.hashi.schedule;

import org.meb.hashi.model.Node;

public interface Scheduler {

	public void schedule(Node pending, Node trigger);

	public boolean hasNext();

	public Node next();

	public void reset();

}
