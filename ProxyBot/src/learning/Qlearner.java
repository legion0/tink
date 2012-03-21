package learning;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.*;
import actions.ActionI;
import actions.ActionI.ACTION;


import states.StateI;

public class Qlearner {
	
	private String _filePath = null;
	
	LinkedHashMap<String, Double> _qMap = new LinkedHashMap<String, Double>();
	private static Double _epsilon = 0.5;
	private static Double _alpha = 0.2;
	private static Double _gamma = 0.9;
	
	public Qlearner(String filePath) {
		_filePath = filePath;
		try {
			ObjectMapper om = new ObjectMapper();
			_qMap = om.readValue(new File(_filePath), LinkedHashMap.class);
		} catch (Exception e) {e.printStackTrace();}
	}
	
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
		_qMap.put(index.toString(), newVal);
	}
	
	private Double getMapVal(StateAction index) {
		Double val = _qMap.get(index.toString());
			return val != null ? val : 0;
	}
	
	public void persist() {
		if (_filePath != null)
			try {
				ObjectMapper om = new ObjectMapper();
				om.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
				om.writeValue(new File(_filePath), _qMap);
			} catch (Exception e) {e.printStackTrace();}
	}
	
	/*
	 * State Action Pair class ... blah blah blah ...
	 * */
	private static class StateAction {
		LinkedHashMap<String, String> _data = new LinkedHashMap<String, String>();
		
		public StateAction(StateI s, ACTION a) {
			_data.put("state", s.toString());
			_data.put("action", a.toString());
		}
		
		public String toString() {
			try {
				StringWriter sw = new StringWriter();
				ObjectMapper om = new ObjectMapper();
				om.writeValue(sw, _data);
				return sw.toString();
			} catch (JsonGenerationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return "ERROR";
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((_data == null) ? 0 : _data.hashCode());
			return result;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			StateAction other = (StateAction) obj;
			if (_data == null) {
				if (other._data != null)
					return false;
			} else if (!_data.equals(other._data))
				return false;
			return true;
		}
	}
}
