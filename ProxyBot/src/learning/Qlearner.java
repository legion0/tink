package learning;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.security.acl.LastOwnerException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.*;
import actions.ActionI;
import actions.ActionI.ACTION;


import states.StateI;

public class Qlearner {
	
	private static final Double PRECISION = 0.00001;
	
	private String _filePath = null;
	
	TreeMap<String, Double> _qMap = new TreeMap<String, Double>();
	//private static Double _epsilon = 0.0, _gamma = 0.0;
	
	
	private StateI firstState = null;
	private ArrayList<StateI> stateList1 = new ArrayList<StateI>(3072);
	private ArrayList<StateI> stateList2 = new ArrayList<StateI>(3072);
	private ArrayList<ACTION> actionList = new ArrayList<ACTION>(3072);
	private ArrayList<Double> rewardList = new ArrayList<Double>(3072);
	

	private Parameters _params; 
	
	public Qlearner(String filePath, Parameters params) {
		_params = params;
		_filePath = filePath;
		try {
			ObjectMapper JSON = new ObjectMapper();
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
			String strLine, str, dblStr;
			Double dbl = 0.0;
			while ((strLine = br.readLine()) != null) {
				if (strLine.length() < 2)
					continue;
				str = JSON.readValue(strLine.substring(0, strLine.lastIndexOf(':')).trim(), String.class);
				dblStr = strLine.substring(strLine.lastIndexOf(':')+1,strLine.length()).trim();
				if (dblStr.endsWith(","))
					dblStr = dblStr.substring(0, dblStr.length()-1);
				dbl = Double.parseDouble(dblStr);
				if (Math.abs(dbl) < PRECISION)
					continue;
				_qMap.put(str, dbl);
			}
			br.close();
		} catch (Exception e) {e.printStackTrace(); System.exit(-1);}
	}
	
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
		if (Math.random() > _params.epsilon()) {
			action = getPolicy(s);
			//System.out.println("P: " + getValue(s) + " " + action.toString() + " | " + s.toString());
			if (action == ACTION.ACTION_RETREAT) {
				System.out.println("P: " + getValue(s) + " " + s.toString() + " " + getMapVal(new StateAction(s, ACTION.ACTION_ATTACK)));				
			}
		}
		else {
			action = ActionI.getRandom();
			//System.out.println("R: " + action.toString() + " | " + s.toString());
		}
		return action;
	}
	
	public void update2(StateI s, ACTION a, StateI s2, Double r) {
		StateAction index = new StateAction(s, a);
		Double val = getMapVal(index);
		Double newVal = 0.0;
		Double alph = _params.alpha();
		if (s2 != null)
			newVal = (1-alph)*val + alph*(r+_params.gamma()*getValue(s2));
		else
			newVal = (1-alph)*val + alph*r;
		if (Math.abs(val) < PRECISION && Math.abs(newVal) < PRECISION)
			newVal = 0.0;
		_qMap.put(index.toString(), newVal);
	}
	
	public void update(StateI s, ACTION a, StateI s2, Double r) {
		stateList1.add(s);
		actionList.add(a);
		stateList2.add(s2);
		rewardList.add(r);
	}
	
	private Double getMapVal(StateAction index) {
		return getMapVal(index.toString());
	}
	
	private Double getMapVal(String index) {
		Double val = _qMap.get(index.toString());
			return val != null ? val : 0;
	}
	
	public int size() {
		return _qMap.size();
	}
	
	public synchronized void persist() {
		System.out.println("XXX list size is " + rewardList.size());
		for (int i = rewardList.size()-1; i >= 0; i--) {
			update2(stateList1.get(i), actionList.get(i), stateList2.get(i), rewardList.get(i));
		}
		
		if (_filePath != null)
			try {
				System.out.print("Persisting map with size: " + size());
				ObjectMapper om = new ObjectMapper();
				om.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
				om.writeValue(new File(_filePath), _qMap);
				System.out.println(" Done.");
			} catch (Exception e) {e.printStackTrace();}
	}
	
	/*
	 * State Action Pair class ... blah blah blah ...
	 * */
	private static class StateAction {
//		LinkedHashMap<String, String> _data = new LinkedHashMap<String, String>();
		
		private StateI _state = null;
		private ACTION _action = null;
		
		public StateAction(StateI s, ACTION a) {
			_state = s;
			_action = a;
//			_data.put("state", _state.toString());
//			_data.put("action", _action.toString());
		}
		
		public String toString() {
//			try {
//				StringWriter sw = new StringWriter();
//				ObjectMapper om = new ObjectMapper();
//				om.writeValue(sw, _data);
//				return sw.toString();
//			} catch (JsonGenerationException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (JsonMappingException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			return "ERROR";
			return _state.toString() + '|' + _action.toString();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((_action == null) ? 0 : _action.hashCode());
			result = prime * result
					+ ((_state == null) ? 0 : _state.hashCode());
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
			if (_action != other._action)
				return false;
			if (_state == null) {
				if (other._state != null)
					return false;
			} else if (!_state.equals(other._state))
				return false;
			return true;
		}
	}
}
