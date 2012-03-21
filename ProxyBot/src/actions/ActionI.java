package actions;

public abstract class ActionI {
	public static enum ACTION {ACTION_ATTACK, ACTION_RETREAT};
	
	public static ACTION getRandom() {
		ACTION[] actions = ACTION.values();
		int index = (int)Math.floor(Math.random()*actions.length);
		return actions[index];
	}
}
