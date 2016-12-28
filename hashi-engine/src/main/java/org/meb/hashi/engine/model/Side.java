package org.meb.hashi.engine.model;

public enum Side {

	NORTH, EAST, SOUTH, WEST;

	public Side opposite() {
		return values()[(ordinal() + 2) % 4];
	}

//	public Side[] others() {
//		switch (this) {
//			case NORTH:
//				return new Side[] {NORTH, EAST, SOUTH, WEST};
//		}
//	}
}
