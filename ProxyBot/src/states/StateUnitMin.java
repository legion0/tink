package states;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import actions.ActionI.ACTION;

import starcraftbot.proxybot.Game;
import starcraftbot.proxybot.wmes.unit.PlayerUnitWME;
import starcraftbot.proxybot.wmes.unit.UnitWME;

public class StateUnitMin extends StateFull {
	private LinkedHashMap<String, Integer> _data = new LinkedHashMap<String, Integer>(3);
	
	public StateUnitMin(Game game, PlayerUnitWME unit) throws JsonGenerationException, JsonMappingException, IOException {
		super(game);
		_data.put("hp", discreteHP(unit.getHitPoints()));
		for (UnitWME u : game.getUnits()) {
			if (u.getPlayerID() == unit.getPlayerID() && u.getID() != unit.getID())
				_data.put("teamHP", getMapVal("teamHP") + discreteHP(u.getHitPoints()));
			else if (u.getPlayerID() != unit.getPlayerID())
				_data.put("enemyHP", getMapVal("enemyHP") + discreteHP(u.getHitPoints()));
		}
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

	private static int discreteHP(int hp) {
		return (int)Math.ceil(hp/10.0); // 1-4 hp for marines
	}
	
	public static Double reward(StateUnitMin s, ACTION a, StateUnitMin s2) {
		Double r = 0.0;
		switch(a) {
		case ACTION_ATTACK:
			r = (s2.getMapVal("enemyHP") < s.getMapVal("enemyHP"))? 1.0 : 0.0;
			break;
		case ACTION_RETREAT:
			r = 0.0;
			break;
		}
		return r;
	}
	
	private int getMapVal(String index) {
		Integer val = _data.get(index);
			return val != null ? val : 0;
	}

	
	
	
	
	
	
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
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
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		StateUnitMin other = (StateUnitMin) obj;
		if (_data == null) {
			if (other._data != null)
				return false;
		} else if (!_data.equals(other._data))
			return false;
		return true;
	}
}
