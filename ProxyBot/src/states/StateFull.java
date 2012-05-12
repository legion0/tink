package states;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import eisbot.proxy.JNIBWAPI;
import eisbot.proxy.model.Unit;

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
	private JNIBWAPI _game;
	
	public StateFull(JNIBWAPI game) {
		_game = game;
		for (Unit u : game.getAllUnits()) {
			LinkedHashMap<String, Integer> unit = new LinkedHashMap<String, Integer>(5);
			unit.put("pid", u.getPlayerID());
			unit.put("id", u.getID());
			unit.put("hp", u.getHitPoints());
			unit.put("x", u.getX());
			unit.put("y", u.getY());
			_units.add(unit);
		}
	}
	
	public int enemyTotalHP() {
		int total = 0;
		for (Map<String, Integer> unit : _units) {
			if (unit.get("pid") == _game.getEnemies().get(0).getID()) {
				total += discreteHP(unit.get("hp"));
			}
		}
		return total;
	}
	
	public int playerTotalHP() {
		int total = 0;
		for (Map<String, Integer> unit : _units) {
			if (unit.get("pid") == _game.getSelf().getID()) {
				total += discreteHP(unit.get("hp"));
			}
		}
		return total;
	}
	
	public int getUnitHP(int id) {
		for (Map<String, Integer> unit : _units) {
			if (unit.get("id") == id) {
				return discreteHP(unit.get("hp"));
			}
		}
		return 0;
	}
	
	public int enemyUnitCount() {
		int total = 0;
		for (Map<String, Integer> unit : _units) {
			if (unit.get("pid") == _game.getEnemies().get(0).getID()) {
				total++;
			}
		}
		return total;
	}
	
	public int playerUnitCount() {
		int total = 0;
		for (Map<String, Integer> unit : _units) {
			if (unit.get("pid") == _game.getSelf().getID()) {
				total++;
			}
		}
		return total;
	}
	
	public static int discreteHP(int hp) {
		return (int)Math.ceil(hp/6.0); // 1-7 hp for marines
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
	
	public static int getClosestEnemy(Unit unit, JNIBWAPI game) {
		int id = -1;
		double closest = Double.MAX_VALUE;
		List<Unit> enemies = game.getEnemyUnits();
		for (Unit e : enemies) {
			double dist = getDistance(unit, e, game);
			if (dist < closest) {
				id = e.getID();
				closest = dist;
			}
		}
		return id;
	}
	
	public static int getRetreatY(Unit unit, JNIBWAPI game) {
		int ret = 1;
		double closest = Double.MAX_VALUE;
		List<Unit> enemies = game.getEnemyUnits();
		for (Unit e : enemies) {
			double dist = getDistance(unit, e, game);
			if (dist < closest) {
				if(e.getY()>unit.getY()){
					ret = unit.getY() - 10 * 32;
				} else {
					ret = unit.getY() + 10 * 32;
				}
			}
		}
		if (ret < 1)
			ret = 1;
		if (ret > 1000)
			ret = 1000;
		return ret;
	}
	
	public static double getDistance(int unit1, int unit2, JNIBWAPI game) {
		Unit u1 = game.getUnit(unit1);
		Unit u2 = game.getUnit(unit2);
		return getDistance(u1, u2, game);
	}
	
	public static double getDistance(Unit u1, Unit u2, JNIBWAPI game) {
		if (u1 == null || u2 == null)
			return -1;
		return distance(u1.getX(), u1.getY(), u2.getX(), u2.getY());
	}
	
	protected static double distance(int x1, int y1, int x2, int y2) {
		double xDistSquare = (x1-x2)*(x1-x2);
		double yDistSquare = (y1-y2)*(y1-y2);
		double distance = Math.sqrt(xDistSquare+yDistSquare);
		return distance;
	}
	
}
