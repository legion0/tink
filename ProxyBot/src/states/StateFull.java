package states;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import starcraftbot.proxybot.Game;
import starcraftbot.proxybot.wmes.unit.EnemyUnitWME;
import starcraftbot.proxybot.wmes.unit.PlayerUnitWME;
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
	
	protected static int getClosestEnemy(PlayerUnitWME unit, Game game) {
		int id = -1;
		double closest = Double.MAX_VALUE;
		List<EnemyUnitWME> enemies = game.getEnemyUnits();
		for (UnitWME e : enemies) {
			double dx = unit.getX() - e.getX();
			double dy = unit.getY() - e.getY();
			double dist = Math.sqrt(dx*dx + dy*dy); 

			if (dist < closest) {
				id = e.getID();
				closest = dist;
			}
		}
		return id;
	}
	
	protected static int getDistance(int unit1, int unit2, Game game) {
		UnitWME u1 = game.getUnitByID(unit1);
		UnitWME u2 = game.getUnitByID(unit2);
		if (u1 == null || u2 == null)
			return -1;
		double xDistSquare = (u1.getX()-u2.getX())*(u1.getX()-u2.getX());
		double yDistSquare = (u1.getY()-u2.getY())*(u1.getY()-u2.getY());
		double distance = Math.sqrt(xDistSquare+yDistSquare);
		return (int)Math.ceil(distance);
	}
	
}
