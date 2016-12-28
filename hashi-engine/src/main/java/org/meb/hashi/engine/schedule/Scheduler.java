package org.meb.hashi.engine.schedule;

import org.meb.hashi.engine.model.Node;

public interface Scheduler {

	public void schedule(Node pending, Node trigger);

	public boolean hasNext();

	public Node next();

	public void reset();

}
