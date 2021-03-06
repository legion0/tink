package agents;

import eisbot.proxy.JNIBWAPI;
import eisbot.proxy.model.Unit;
import actions.ActionI.ACTION;
import learning.Qlearner;
import states.StateFull;
import states.StateI;

public abstract class Aagent {
	protected JNIBWAPI _game;
	protected Qlearner _qlearn;
	protected int _id;
	protected StateFull _lastState;
	protected StateFull _newState;
	protected ACTION _lastAction = null, _newAction = null;
	protected boolean _isDead;

	protected int _hpHistory[] = { 7, 7, 7, 7, 7, 7, 7, 7, 7, 7 };
	protected int _hpHIndex = 0;

	public Aagent(JNIBWAPI game, Qlearner qlearn, int id) {
		_game = game;
		_qlearn = qlearn;
		_id = id;
		_isDead = false;
	}

	public void turn() {
//		System.out.println("XXX Playing agent " + _id);
		if (updateState()) {
			newAction();
			_lastState = _newState;
			_lastAction = _newAction;
		}
	}

	public boolean isDead() {
		return _isDead;
	}

	public int getID() {
		return _id;
	}

	protected boolean updateState() {
//		System.out.println("XXX Update State for agent " + _id);
		Unit unit = _game.getUnit(_id);
		if (unit == null || unit.getHitPoints() == 0) {
//			System.out.println("XXX Update State for agent " + _id + ": unit is dead");
			_isDead = true;
			StateI last = getLastState();
			StateI current = null;
			Double reward = rewardDeath();
			_qlearn.update(last, _lastAction, current, reward);
			return false;
		}
		_newState = new StateFull(_game);
		_hpHIndex = (_hpHIndex + 1) % _hpHistory.length;
		_hpHistory[_hpHIndex] = _newState.getUnitHP(_id);
		boolean done = actionDone();
//		System.out.println("XXX Update State for agent " + _id + ": done " + done);
		return done;
	}

	protected void newAction() {
//		System.out.println("XXX newAction for agent " + _id);
		// give reward for last action
		StateI last = getLastState();
		StateI current = getCurrentState();
		if (_lastState != null) {
			Double reward = getReward(last, _lastAction, current);
			_qlearn.update(last, _lastAction, current, reward);
		}
		// get next action
		_newAction = _qlearn.getAction(current);
		// preform next action
		preformAction();
	}

	protected abstract Double rewardDeath();

	protected abstract boolean actionDone();

	protected abstract StateI getLastState();

	protected abstract StateI getCurrentState();

	protected abstract Double getReward(StateI last, ACTION _lastAction2,
			StateI current);

	protected abstract void preformAction();

}
