package learning;

import java.util.HashMap;

import actions.ActionI;
import actions.ActionI.ACTION;


import states.StateI;

public class Qlearner {
	
	HashMap<StateAction, Double> _qMap = new HashMap<StateAction, Double>();
	private static Double _epsilon = 0.5;
	private static Double _alpha = 0.2;
	private static Double _gamma = 0.9;
	
	public Qlearner() {}
	
	public Double getQValue(StateI s, ACTION a) {
		return getMapVal(new StateAction(s, a));
	}
	
	public Double getValue(StateI s) {
		Double maxVal = Double.NEGATIVE_INFINITY;
		Double val;
		for (ACTION a : ACTION.values())
			if ((val = getMapVal(new StateAction(s, a))) > maxVal)
				maxVal = val;
		return maxVal;
	}
	
	public ACTION getPolicy(StateI s) {
		ACTION maxAction = null;
		Double maxVal = Double.NEGATIVE_INFINITY;
		Double val;
		for (ACTION a : ACTION.values())
			if ((val = getMapVal(new StateAction(s, a))) > maxVal) {
				maxAction = a;
				maxVal = val;
			}
		return maxAction;
		/* TODO randomize over best choices */
	}
	
	public ACTION getAction(StateI s) {
		ACTION action;
		if (Math.random() > _epsilon)
			action = getPolicy(s);
		else
			action = ActionI.getRandom();
		return action;
	}
	
	public void update(StateI s, ACTION a, StateI s2, Double r) {
		StateAction index = new StateAction(s, a);
		Double val = getMapVal(index);
		Double newVal = (1-_alpha)*val + _alpha*(r+_gamma*getValue(s2));
		_qMap.put(index, newVal);
	}
	
	private Double getMapVal(StateAction index) {
		Double val = _qMap.get(index);
			return val != null ? val : 0;
	}
	
	/*
	 * State Action Pair class ... blah blah blah ...
	 * */
	private static class StateAction {
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((_a == null) ? 0 : _a.hashCode());
			result = prime * result + ((_s == null) ? 0 : _s.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			StateAction other = (StateAction) obj;
			if (_a != other._a)
				return false;
			if (_s == null) {
				if (other._s != null)
					return false;
			} else if (!_s.equals(other._s))
				return false;
			return true;
		}
		private StateI _s;
		private ACTION _a;
		public StateAction(StateI s, ACTION a) {
			_s = s;
			_a = a;
		}
	}			
			
}
