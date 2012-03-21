package states;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import starcraftbot.proxybot.Game;
import starcraftbot.proxybot.wmes.unit.UnitWME;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class StateFull implements StateI {
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_units == null) ? 0 : _units.hashCode());
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
		StateFull other = (StateFull) obj;
		if (_units == null) {
			if (other._units != null)
				return false;
		} else if (!_units.equals(other._units))
			return false;
		return true;
	}

	private ArrayList<LinkedHashMap<String, Integer>> _units = new ArrayList<LinkedHashMap<String, Integer>>();
	private Game _game;
	
	public StateFull(Game game) throws JsonGenerationException, JsonMappingException, IOException {
		_game = game;
		for (UnitWME u : game.getUnits()) {
			LinkedHashMap<String, Integer> unit = new LinkedHashMap<String, Integer>(5);
			unit.put("pid", u.getPlayerID());
			unit.put("id", u.getID());
			unit.put("hp", u.getHitPoints());
			unit.put("x", u.getX());
			unit.put("y", u.getY());
			_units.add(unit);
		}
	}
	
	public String toString() {
		StringWriter sw = new StringWriter();
		ObjectMapper om = new ObjectMapper();
		try {
			om.writeValue(sw, _units);
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
		return sw.toString();
	}
	
}
