package learning;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
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
	private static Double _epsilon = 0.03, _gamma = 0.9;
	
	public Double alpha(StateI s, ACTION a) {
		return 0.1;
		//String index2 = s.toString()+"|"+a.toString();
		
//		String index2 = s.toString();
//		return 1/(1+getMapVal(index2));
	}
	
	public Qlearner(String filePath) {
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
		if (Math.random() > _epsilon) {
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
	
	public void update(StateI s, ACTION a, StateI s2, Double r) {
		StateAction index = new StateAction(s, a);
		Double val = getMapVal(index);
		Double newVal = 0.0;
		Double alph = alpha(s, a);
		if (s2 != null)
			newVal = (1-alph)*val + alph*(r+_gamma*getValue(s2));
		else
			newVal = (1-alph)*val + alph*r;
		if (Math.abs(val) < PRECISION && Math.abs(newVal) < PRECISION)
			newVal = 0.0;
		_qMap.put(index.toString(), newVal);
		//String index2 = s.toString()+"|"+a.toString();
		String index2 = s.toString();
		_qMap.put(index2, getMapVal(index2)+1);
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
		if (_filePath != null)
			try {
				ObjectMapper om = new ObjectMapper();
				om.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
				om.writeValue(new File(_filePath), _qMap);
				System.out.println("Persisting map with size: " + size());
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
