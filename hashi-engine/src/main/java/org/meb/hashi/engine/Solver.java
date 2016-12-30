package org.meb.hashi.engine;

import org.meb.hashi.engine.cfg.Globals;
import org.meb.hashi.engine.model.Group;
import org.meb.hashi.engine.model.Node;
import org.meb.hashi.engine.model.Side;
import org.meb.hashi.engine.model.State;
import org.meb.hashi.engine.schedule.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Solver {

	private static final Logger log = LoggerFactory.getLogger(Solver.class);

	private boolean useRemainingMatch = true;
	private boolean useCompleteMatch = true;
	private boolean usePartialMatch = true;
	private boolean usePreventDeadGroups11 = true;
	private boolean usePreventDeadGroups121 = true;

	private State state;
	private Globals globals;
	private Scheduler scheduler;

	public Solver(Node[] nodes, Globals globals, Scheduler scheduler) {
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

	public boolean isUseRemainingMatch() {
		return useRemainingMatch;
	}

	public void setUseRemainingMatch(boolean useRemainingMatch) {
		this.useRemainingMatch = useRemainingMatch;
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
		solve(0);
	}

	public void solve(int solvedLimit) {
		boolean success;
		do {
			success = false;
			success |= solveNodes(solvedLimit);
			success |= solveGroups();
		} while (success);
	}

	private boolean solveNodes(int stepsLimit) {

		boolean success = false;
		int stepsCount = 0;

		while (scheduler.hasNext() && (stepsLimit == 0 || stepsLimit > stepsCount)) {
			Node node = scheduler.next();
			if (solveNode(node)) {
				success = true;
				stepsCount++;
				state.setLastSolvedNode(node);
			}
		}

		return success;
	}

	private boolean solveGroups() {
		if (!scheduler.hasNext() && usePreventDeadGroups11) {
			preventDeadGroups11();
		}
		if (!scheduler.hasNext()) {
			preventDeadGroupsDegree2();
		}
		// if (!scheduler.hasNext()) {
		// preventDeadGroupsDegree3();
		// }
		// if (!scheduler.hasNext() && usePreventDeadGroups121) {
		// preventDeadGroups121();
		// }
		return false;
	}

	// public void solve() {
	// scheduler.reset();
	// solve(0);
	// }

	// public void solve(int limit) {
	//
	// int successCount = 0;
	// boolean hadNext = false;
	//
	// while (limit == 0 || (limit > 0 && successCount < limit)) {
	//
	// if (!scheduler.hasNext() && usePreventDeadGroups11) {
	// preventDeadGroups11();
	// }
	// if (!scheduler.hasNext()) {
	// preventDeadGroupsDegree2();
	// }
	// if (!scheduler.hasNext()) {
	// preventDeadGroupsDegree3();
	// }
	// if (!scheduler.hasNext() && usePreventDeadGroups121) {
	// preventDeadGroups121();
	// }
	//
	// if (scheduler.hasNext()) {
	// Node node = scheduler.next();
	// if (solveNode(node)) {
	// successCount++;
	// state.setLastSolvedNode(node);
	// }
	//
	// hadNext = true;
	// } else {
	// if (!hadNext) {
	// break;
	// }
	//
	// hadNext = false;
	// }
	// }
	// }

	private boolean solveNode(Node node) {
		boolean success = false;

		// if (node.isNotComplete()) {
		// success |= matchRemaining(node);
		// }

		if (node.isNotComplete()) {
			success |= matchCompletely(node);
		}

		if (node.isNotComplete() && usePartialMatch) {
			success |= matchPartially(node);
		}

		return success;
	}

	private boolean matchRemaining(Node node) {
		assert node.isNotComplete();

		boolean success = false;
		Node remaining = null;
		Side side = null;

		for (Side s : Side.values()) {
			Node neighbour = node.neighbour(s);
			if (neighbour != null) {
				if (remaining == null) {
					remaining = neighbour;
					side = s;
				} else {
					remaining = null;
					side = null;
					break;
				}
			}
		}

		if (remaining != null) {
			int edgeDegree = Math.min(remaining.degree(), 2);
			// int edgeDegree = node.availableDegree(side);

			assert remaining.isNotComplete();
			assert node.degree() <= edgeDegree;

			log.info("[REMAINING MATCH] -> node=({})", node);
			scheduler.schedule(remaining, node);
			state.connectFully(node, side, scheduler);
			node.update();
			remaining.update();

			success = true;
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

	private void preventDeadGroups11() {
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
							log.info("[PREVENT DEAD GROUPS 1-1] -> node=({})", gateway);

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

	//
	// private Group findGroup(int degree, int gatewayCount, Set<Group>
	// excludes) {
	// for (Group group : state.getGroups()) {
	// if (!excludes.contains(group) && group.degree() == degree
	// && group.getGatewayNodesCount() == gatewayCount) {
	// return group;
	// }
	// }
	// return null;
	// }

	private void preventDeadGroupsDegree2() {
		for (Group group : state.getGroups()) {
			group.update();

			if (group.degree() == 2 && group.getGatewaysCount() == 1) {
				Node gateway = group.getGateways().iterator().next();

				assert gateway.degree() == 2 : gateway;

				for (Side side : Side.values()) {
					Node neighbour = gateway.neighbour(side);
					if (neighbour != null) {
						Group neighbourGroup = neighbour.group().update();
						if (gateway.sideDegree(side) < 2) {
							return;
						}

						assert !group.equals(neighbourGroup) : group;

						if (neighbourGroup.degree() == 2
								&& neighbourGroup.getGatewaysCount() == 1) {
							log.info("[PREVENT DEAD GROUPS d2] -> node=({}), neighbour=({})",
									gateway, neighbour);
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

	private void preventDeadGroupsDegree3() {
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
	}
}
