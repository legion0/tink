package states;

import java.io.IOException;
import java.io.StringWriter;
import java.util.TreeMap;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import eisbot.proxy.JNIBWAPI;
import eisbot.proxy.model.Unit;

import actions.ActionI.ACTION;

public class StateUnitMin extends StateFull {
	private TreeMap<String, Integer> _data = new TreeMap<String, Integer>();
	private static TreeMap<Integer, String> _lastOrders = new TreeMap<Integer, String>();
	
	public StateUnitMin(JNIBWAPI game, Unit unit, boolean underFire) throws JsonGenerationException, JsonMappingException, IOException {
		super(game);
		_data.put("hp", discreteHP(unit.getHitPoints()));
		int closest = getClosestEnemy(unit, game);
		_data.put("underFire", underFire? 1 : 0);
		
		_data.put("distance", (int)getDistance(unit.getID(), closest, game));
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
	
	public int getHitPoints() {
		return getMapVal("hp");
	}

	public static Double reward(StateUnitMin s, ACTION a, StateUnitMin s2) {
		if (s.toString().equals(s2.toString()))
			System.out.println(a.toString() + ": " + s.toString());
		
		Double r = 0.0;
		/*switch(a) {
		case ACTION_ATTACK:
			r = (s2.getMapVal("enemyHP") < s.getMapVal("enemyHP"))? 1.0 : 0.0;
			break;
		case ACTION_RETREAT:
			r = 0.0;
			break;
		}*/
		//int hp = s.getMapVal("hp");
		//int teamHP = s.getMapVal("teamHP") + hp;
		//int enemyHP = s.getMapVal("enemyHP");
		//int enemyHP = s.enemyTotalHP();
		//int hp2 = s2.getMapVal("hp");
		//int teamHP2 = s2.getMapVal("teamHP") + hp2;
		//int enemyHP2 = s2.getMapVal("enemyHP");
		//int enemyHP2 = s2.enemyTotalHP();
		//r += (double)((teamHP2 - teamHP) + (enemyHP - enemyHP2));
		//r += (double)((hp2 - hp) - (enemyHP2 - enemyHP));
		
		/*if (a == ACTION.ACTION_RETREAT)
			r -= 1;*/
		
		if (s._data.get("distance") <= 6 && a == ACTION.ACTION_ATTACK)
			r+= 1;
		
		return r;
	}
	
	private int getMapVal(String index) {
		Integer val = _data.get(index);
			return val != null ? val : 0;
	}

	public static void perfomAction(ACTION a, Unit unit, JNIBWAPI game) {
		String oldOrder = _lastOrders.get(unit.getID());
		int closest = getClosestEnemy(unit, game);
		
		String newOrder = "";
		switch (a) {
		case ACTION_RETREAT:
			newOrder = "" + unit.getOrderID()  + "ACTION_RETREAT";
			break;
		case ACTION_ATTACK:
			newOrder = "" + unit.getOrderID()  + "ACTION_ATTACK" + closest;
			break;
		}
		
		if (oldOrder != null) {
			if (oldOrder.equals(newOrder))
				return;
		}
		_lastOrders.put(unit.getID(), newOrder);
		
		switch(a) {
		case ACTION_RETREAT:
			game.rightClick(unit.getID(), 1, 1);
			break;
		case ACTION_ATTACK:
			if (closest != -1)
				game.rightClick(unit.getID(), closest);
			break;
		}
		//System.out.println(a.toString() + " " + unit.getID());
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
		/*if (this == obj)
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
		return true;*/
		return this.toString().equals(obj.toString());
	}

	public static void EOG() {
		_lastOrders.clear();
		
	}
}
