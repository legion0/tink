package agents;

import java.util.Calendar;

import actions.ActionI.ACTION;
import learning.Qlearner;
import starcraftbot.proxybot.Game;
import starcraftbot.proxybot.wmes.unit.UnitWME;
import states.MarineState;
import states.StateI;

public class MarineAgent extends Aagent {
	private static final int ATTACK_LENGTH = 5;
	//private static final int MARINE_FIRE_REACH = 192;
	private static final int MARINE_FIRE_REACH = 132;
	
	MarineState _last = null;
	MarineState _current = null;
	int _finishAttack = 0;
	
	public MarineAgent(Game game, Qlearner qlearn, int id) {
		super(game, qlearn , id);
	}

	@Override
	protected StateI getLastState() {
		return _last;
	}

	@Override
	protected StateI getCurrentState() {
		int hpLost = 0;
		if(_lastState != null) {
			hpLost = _lastState.getUnitHP(_id) - _newState.getUnitHP(_id);
		}
		_current = new MarineState(_game, _id, hpLost);
		return _current;
	}
	
	@Override
	protected boolean actionDone() {
		getCurrentState();
		//System.out.println("XXX Starting action done for agent " + _id + " with action " + _action);
		if(_lastAction == null) {
			return true;
		} else if(_lastAction == ACTION.ACTION_ATTACK) {
			return _game.getGameFrame() >= _finishAttack || _current.getHpLost() > 0;
		} else if(_lastAction == ACTION.ACTION_RETREAT) {
			//System.out.println("XXX Agent " + _id + " has finished retreating at " + Calendar.getInstance().getTimeInMillis());
//			return true;
			if (_current.getRealDistance() > MARINE_FIRE_REACH) {
				//System.out.println("XXX Ran away at " + _current.getRealDistance());
				return true;
			} else {
				//System.out.println("XXX Distance is " + _current.getRealDistance() + " not retreating.");
				return false;
			}
//			if (_current.getHpLost() == 0) {
//				return true;
//			}
		}
		return false;
	}

	@Override
	protected Double getReward(StateI last, ACTION action, StateI current) {
		if(action == ACTION.ACTION_ATTACK) {
			if(_current.getRealDistance() <= MARINE_FIRE_REACH) {
				return 1.0;
			}
		}
		return 0.0;
	}
	
	@Override
	protected Double rewardDeath() {
		double enemyHP = _newState.enemyTotalHP();
		return -enemyHP/13.0;
	}

	@Override
	protected void preformAction() {		
		UnitWME unit = _game.getUnitByID(_id);
		int retreatX = unit.getRealX();
		int retreatY = unit.getRealY() - 10*32;
		if (retreatY < 1) retreatY = 1;
		switch(_newAction) {
			case ACTION_RETREAT:
				//System.out.println("XXX Agent " + _id + " is retreating at " + Calendar.getInstance().getTimeInMillis());
				_game.getCommandQueue().rightClick(_id, retreatX, retreatY);
				break;
			case ACTION_ATTACK:
				if (_lastState == null || _lastAction != ACTION.ACTION_ATTACK || (_current.getClosest() != -1)) {
					//_game.getCommandQueue().rightClick(_id, _current.getClosest());
					_game.getCommandQueue().attackUnit(_id, _current.getClosest());
					_finishAttack = _game.getGameFrame() + ATTACK_LENGTH;
				}
				break;
		}
		//System.out.println("XXX Agent " + _id + " is " + _action);
		_last = _current;
		
	}

}
