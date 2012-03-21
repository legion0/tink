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

public class StateFull {
	private ArrayList<LinkedHashMap<String, Integer>> _units = new ArrayList<LinkedHashMap<String, Integer>>();
	
	public StateFull(Game game) throws JsonGenerationException, JsonMappingException, IOException {
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
