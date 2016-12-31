package org.meb.hashi.engine;

import org.meb.hashi.engine.cfg.Globals;
import org.meb.hashi.engine.model.Group;
import org.meb.hashi.engine.model.Node;
import org.meb.hashi.engine.model.Side;
import org.meb.hashi.engine.model.State;
import org.meb.hashi.engine.schedule.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HashiSolver {

	private static final Logger log = LoggerFactory.getLogger(HashiSolver.class);

	private boolean useCompleteMatch = true;
	private boolean usePartialMatch = true;
	private boolean usePreventDeadGroups11 = true;
	private boolean usePreventDeadGroups121 = true;

	private State state;
	private Globals globals;
	private Scheduler scheduler;

	public HashiSolver(Node[] nodes, Globals globals, Scheduler scheduler) {
		this.state = new State(nodes);
		this.globals = globals;
		this.scheduler = scheduler;
	}

	public State getState() {
		return state;
	}

	public Globals getGlobals() {
		return globals;
	}

	public boolean isUseCompleteMatch() {
		return useCompleteMatch;
	}

	public void setUseCompleteMatch(boolean useCompleteMatch) {
		this.useCompleteMatch = useCompleteMatch;
	}

	public boolean isUsePartialMatch() {
		return usePartialMatch;
	}

	public void setUsePartialMatch(boolean usePartialMatch) {
		this.usePartialMatch = usePartialMatch;
	}

	public boolean isUsePreventDeadGroups11() {
		return usePreventDeadGroups11;
	}

	public void setUsePreventDeadGroups11(boolean usePreventDeadGroups11) {
		this.usePreventDeadGroups11 = usePreventDeadGroups11;
	}

	public boolean isUsePreventDeadGroups121() {
		return usePreventDeadGroups121;
	}

	public void setUsePreventDeadGroups121(boolean usePreventDeadGroups121) {
		this.usePreventDeadGroups121 = usePreventDeadGroups121;
	}

	public void solve() {
		solve(new StepsCounter());
	}

	public void solve(StepsCounter counter) {
		boolean success;
		do {
			success = false;
			success |= solveNodes(counter);
			if (counter.isLimitReached()) {
				break;
			}

			success |= solveGroups(counter);
			if (counter.isLimitReached()) {
				break;
			}
		} while (success);
	}

	private boolean solveNodes(StepsCounter counter) {

		boolean success = false;

		while (scheduler.hasNext() && !counter.isLimitReached()) {
			Node node = scheduler.next();
			if (solveNode(node)) {
				success = true;
				counter.increase();
				state.setLastSolvedNode(node);
			}
		}

		return success;
	}

	private boolean solveGroups(StepsCounter counter) {
		// if (!scheduler.hasNext() && usePreventDeadGroups11) {
		preventIsolatedGroupsDegree1();
		// }
		// if (!scheduler.hasNext()) {
		preventIsolatedGroupsDegree2();
		// }
		// if (!scheduler.hasNext()) {
		// preventDeadGroupsDegree3();
		// }
		// if (!scheduler.hasNext() && usePreventDeadGroups121) {
		// preventDeadGroups121();
		// }
		return false;
	}

	private boolean solveNode(Node node) {
		boolean success = false;

		if (node.isNotComplete()) {
			success |= matchCompletely(node);
		}

		if (node.isNotComplete() && usePartialMatch) {
			success |= matchPartially(node);
		}

		return success;
	}

	private boolean matchCompletely(Node node) {
		assert node.isNotComplete();

		boolean success = false;
		int degree = node.degree();
		int sideDegreeTotal = node.sideDegreeTotal();

		if (degree == sideDegreeTotal) {
			log.info("[COMPLETE MATCH] -> node=({})", node);

			for (Side side : Side.values()) {
				Node neighbour = node.neighbour(side);
				if (neighbour != null && neighbour.isNotComplete()) {
					scheduler.schedule(neighbour, node);
					state.connectFully(node, side, scheduler);
					neighbour.update();
				}
			}
			node.update();

			success = true;
		}

		return success;
	}

	private boolean matchPartially(Node node) {
		assert node.isNotComplete();

		boolean success = false;

		int[] availableDegreeOthers = node.sideDegreeOthers();
		int nodeDegree = node.degree();

		for (Side side : Side.values()) {
			if (nodeDegree > availableDegreeOthers[side.ordinal()]) {
				Node neighbour = node.neighbour(side);
				if (neighbour != null && node.edge(side) == null) {
					log.info("[PARTIAL MATCH] -> node=({})", node);
					scheduler.schedule(neighbour, node);
					state.connectPartially(node, side, scheduler);
					neighbour.update();
					success |= true;
				}
			}
		}
		node.update();

		return success;
	}

	private void preventIsolatedGroupsDegree1() {
		if (state.getGroups().size() == 2) {
			return;
		}

		for (Group group : state.getGroups()) {
			group.update();

			if (group.degree() == 1) {
				Node gateway = group.getGateways().iterator().next();

				assert group.getGatewaysCount() == 1 : group;
				assert gateway.degree() == 1 : gateway;

				for (Side side : Side.values()) {
					Node neighbour = gateway.neighbour(side);
					if (neighbour != null) {
						Group neighbourGroup = neighbour.group().update();

						assert !group.equals(neighbourGroup) : group;

						if (neighbourGroup.degree() == 1) {
							log.info("[PREVENT ISOLATED GROUPS D1] -> node=({})", gateway);

							state.clearNeighbours(gateway, side);
							scheduler.schedule(gateway, null);
							scheduler.schedule(neighbour, null);
							neighbour.update();
						}
					}
				}
				gateway.update();
			}
		}
	}

	private void preventDeadGroups121() {
		for (Group group : state.getGroups()) {
			group.update();

			int gd = group.degree();
			int gatewaysCount = group.getGatewaysCount();

			if ((gd == 2 || gd == 3) && gatewaysCount == 1) {
				Node gateway = group.getGateways().iterator().next();

				int d1GroupsCount = 0;
				int d2PlusGroupsCount = 0;
				Side s = null;
				for (Side side : Side.values()) {
					Node neighbour = gateway.neighbour(side);
					if (neighbour != null) {
						if (neighbour.group().update().degree() == 1) {
							d1GroupsCount++;
						} else {
							d2PlusGroupsCount++;
							s = side;
						}
					}
				}

				if (gd == 2 && d1GroupsCount == 2 && d2PlusGroupsCount == 1) {
					if (gateway.edge(s) == null) {
						state.connectPartially(gateway, s, scheduler);
						gateway.update();
						gateway.neighbour(s).update();
					}
					break;
				}
			}
		}
	}

	private void preventIsolatedGroupsDegree2() {
		for (Group group : state.getGroups()) {
			group.update();

			if (group.degree() == 2 && group.getGatewaysCount() == 1) {
				Node gateway = group.getGateways().iterator().next();

				assert gateway.degree() == 2 : gateway;

				for (Side side : Side.values()) {
					Node neighbour = gateway.neighbour(side);
					if (neighbour != null) {
						Group neighbourGroup = neighbour.group().update();
//						if (gateway.sideDegree(side) < 2) {
//							return;
//						}

						assert !group.equals(neighbourGroup) : group;

						if (neighbourGroup.degree() == 2 && neighbourGroup.getGatewaysCount() == 1) {
							log.info("[PREVENT ISOLATED GROUPS D2] -> node=({}), neighbour=({})", gateway, neighbour);
							// TODO
							scheduler.schedule(gateway, null);
							scheduler.schedule(neighbour, null);
							gateway.limitSideDegree(side, 1);
							neighbour.limitSideDegree(side.opposite(), 1);
							neighbour.update();
						}
					}
				}
				gateway.update();
			}
		}
	}

	// private void preventDeadGroupsDegree3() {
	// HashSet<Group> iterGroup = new HashSet<Group>(groups);
	// for (Group group : iterGroup) {
	// // if (!group.isActive()) {
	// // continue;
	// // }
	// if (!groups.contains(group)) {
	// continue;
	// }
	// group.update();
	//
	// if (group.degree() == 2) {
	// Node gateway = group.getGatewayNodes().iterator().next();
	//
	// assert gateway.degree() == 1 : gateway;
	//
	// for (Side side : Side.values()) {
	// Node neighbour = gateway.neighbour(side);
	// if (neighbour != null) {
	//
	// Group neighbourGroup = neighbour.group();
	// if (group.equals(neighbourGroup)) {
	// continue;
	// }
	//
	// connectNodesPartially(gateway, neighbour, side);
	// neighbour.update();
	// }
	// }
	// gateway.update();
	// }
	// }
	// }
}
