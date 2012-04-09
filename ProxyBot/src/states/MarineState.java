package states;

import java.io.IOException;
import java.io.StringWriter;
import java.util.TreeMap;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import starcraftbot.proxybot.Game;
import starcraftbot.proxybot.wmes.unit.UnitWME;

public class MarineState extends StateFull {
	private int _closest;
	private TreeMap<String, Integer> _data = new TreeMap<String, Integer>();
	private Double _realDisatance;
	public MarineState(Game game, int id, int hpLost){
		super(game);
		
		UnitWME unit = game.getUnitByID(id);
		_closest = getClosestEnemy(unit, game);
		_realDisatance = getDistance(id, _closest, game);
		
		_data.put("hp", discreteHP(unit.getHitPoints()));
		_data.put("underFire", hpLost);
		_data.put("distance", discDistance(id,_closest,game));
	}
	
	private int discDistance(int id, int closest, Game game)	{		
		return (int)Math.ceil(_realDisatance/32);
	}
	
	public int getClosest() {
		return _closest;
	}
	
	public int getDistance() {
		return getMapVal("distance");
	}
	
	public double getRealDistance() {
		return _realDisatance;
	}
	
	public int getHpLost() {
		return getMapVal("underFire");
	}
	
	private int getMapVal(String index) {
		Integer val = _data.get(index);
			return val != null ? val : 0;
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
		System.err.println("bad toString");
		return "ERROR";
	}
	
	public boolean equals(Object obj) {
		return this.toString().equals(obj.toString());
	}
}
