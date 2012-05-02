package agents;

import java.util.TreeMap;

import actions.ActionI.ACTION;
import learning.Qlearner;
import starcraftbot.proxybot.Game;
import starcraftbot.proxybot.wmes.unit.UnitWME;
import states.MarineState;
import states.StateI;

public class MarineAgent extends Aagent {
	private static final int ATTACK_LENGTH = 2;
	//private static final int MARINE_FIRE_REACH = 192;
	//private static final int MARINE_FIRE_REACH = 132;
	private static final int MARINE_RETREAT_RANGE = 132;
	private static final int MARINE_FIRE_REACH = 152;
	
	MarineState _last = null;
	MarineState _current = null;
	private int _finishAttack = 0;
	private int _lastTarget = -1;
	
	private TreeMap<Integer, Integer> _attacked;
	
	public MarineAgent(Game game, Qlearner qlearn, int id, TreeMap<Integer, Integer> attacked) {
		super(game, qlearn , id);
		_attacked = attacked;
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
			UnitWME unit = _game.getUnitByID(_lastTarget);
			if(unit == null) {
				return true;
			}
//			return _game.getGameFrame() >= _finishAttack || _current.getHpLost() > 0;	
			return _game.getGameFrame() >= _finishAttack;
		} else if(_lastAction == ACTION.ACTION_RETREAT) {
			//System.out.println("XXX Agent " + _id + " has finished retreating at " + Calendar.getInstance().getTimeInMillis());
//			return true;
			if (_current.getRealDistance() > MARINE_RETREAT_RANGE) {
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

	public static boolean isInRange(double distance) {
		return distance <= MARINE_FIRE_REACH;
	}
	
	@Override
	protected Double getReward(StateI last, ACTION action, StateI current) {
		if(action == ACTION.ACTION_ATTACK) {
			if(isInRange(_current.getRealDistance())) {
				return 1.0;
			}
		}
		return 0.0;
	}
	
	@Override
	protected Double rewardDeath() {
		if(_lastTarget!=-1) {
			_attacked.put(_lastTarget, _attacked.get(_lastTarget)-1);
			_lastTarget=-1;			
		}
		
		double enemyHP = _newState.enemyTotalHP();
		//return -enemyHP/13.0;
		return -enemyHP/5;
	}

	@Override
	protected void preformAction() {		
		UnitWME unit = _game.getUnitByID(_id);
		int retreatX = unit.getRealX();
		int retreatY = unit.getRealY() - 10*32;
		if (retreatY < 1) retreatY = 1;
		if(_lastTarget!=-1) {
			_attacked.put(_lastTarget, _attacked.get(_lastTarget)-1);
		}
		switch(_newAction) {
			case ACTION_RETREAT:
				_lastTarget = -1;
				//System.out.println("XXX Agent " + _id + " is retreating at " + Calendar.getInstance().getTimeInMillis());
				_game.getCommandQueue().rightClick(_id, retreatX, retreatY);
				break;
			case ACTION_ATTACK:
				int target = AttackReflexAgent.getTarget(_game,unit,_attacked);
				if (target!=_lastTarget) {
					if(target == -1) {
						return;
					}
					_lastTarget = target;
					_game.getCommandQueue().attackUnit(_id, target);
					_finishAttack = _game.getGameFrame() + ATTACK_LENGTH;
				}
				_attacked.put(target, _attacked.get(target)+1);
				break;
		}
		//System.out.println("XXX Agent " + _id + " is " + _action);
		_last = _current;
		
	}

}
