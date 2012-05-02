package states;

import starcraftbot.proxybot.Game;
import starcraftbot.proxybot.wmes.unit.UnitWME;

public class MarineState extends StateFull {
	private int _closest;
//	private TreeMap<String, Integer> _data = new TreeMap<String, Integer>();
	private Double _realDisatance;
	
	private int _hp = -1, _hpLost = 0, _distance = 0;
	
	public MarineState(Game game, int id, int hpLost){
		super(game);
		
		UnitWME unit = game.getUnitByID(id);
		_closest = getClosestEnemy(unit, game);
		_realDisatance = getDistance(id, _closest, game);
		
		_hp = discreteHP(unit.getHitPoints());
		_hpLost = hpLost;
		_distance = discDistance(id,_closest,game);
//		_data.put("hp", _hp);
//		_data.put("underFire", _hpLost);
//		_data.put("distance", _distance);
	}
	
	private int discDistance(int id, int closest, Game game)	{		
		return (int)Math.ceil(_realDisatance/32);
	}
	
	public int getClosest() {
		return _closest;
	}
	
	public int getDistance() {
		return _distance;
	}
	
	public double getRealDistance() {
		return _realDisatance;
	}
	
	public int getHpLost() {
		return _hpLost;
	}
	
//	private int getMapVal(String index) {
//		Integer val = _data.get(index);
//			return val != null ? val : 0;
//	}
	
	public String toString() {
//		try {
//			StringWriter sw = new StringWriter();
//			ObjectMapper om = new ObjectMapper();
//			om.writeValue(sw, _data);
//			return sw.toString();
//		} catch (JsonGenerationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (JsonMappingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		System.err.println("bad toString");
//		return "ERROR";
		return "|hp "+_hp + "|hpLost " + _hpLost + "|distance " + _distance + "|";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + _distance;
		result = prime * result + _hp;
		result = prime * result + _hpLost;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		MarineState other = (MarineState) obj;
		if (_distance != other._distance)
			return false;
		if (_hp != other._hp)
			return false;
		if (_hpLost != other._hpLost)
			return false;
		return true;
	}
}
