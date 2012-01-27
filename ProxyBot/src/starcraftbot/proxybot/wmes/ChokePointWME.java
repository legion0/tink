package starcraftbot.proxybot.wmes;

import java.util.ArrayList;
import java.util.HashMap;
/**
 * Represents a starting location in StarCraft.
 * 
 * Note: x and y are in tile coordinates
 */
public class ChokePointWME extends WME {

	private int x;
	
	private int y;
	
	private int width;
	
	private RegionWME regionA;
	private RegionWME regionB;
		
	/**
	 * Parses the starting locations.
	 */
	public static ArrayList<ChokePointWME> getLocations(String locationData, ArrayList<RegionWME> regions) {
		ArrayList<ChokePointWME> locations = new ArrayList<ChokePointWME>();

		HashMap<Integer, RegionWME> regionMap = new HashMap<Integer, RegionWME>();
		for (RegionWME region : regions) {
			regionMap.put(region.getID(), region);
		}
		
		String[] locs = locationData.split(":");
		boolean first = true;
		
		for (String location : locs) {
			if (first) {
				first = false;
				continue;
			}
			
			String[] coords = location.split(";");

			ChokePointWME loc = new ChokePointWME();
			loc.x = Integer.parseInt(coords[0]);
			loc.y = Integer.parseInt(coords[1]);
			loc.width = Integer.parseInt(coords[2]);
	
			int region1 = Integer.parseInt(coords[3]);
			int region2 = Integer.parseInt(coords[4]);
			loc.regionA = regionMap.get(region1);
			loc.regionB = regionMap.get(region2);
			
			loc.regionA.getChokePoints().add(loc);
			loc.regionB.getChokePoints().add(loc);
			locations.add(loc);
		}
		
		return locations;		
	}
	
	/**
	 * Returns the x coordinate of the starting location (tile coordinates).
	 */
	public int getX() {
		return x;
	}

	/**
	 * Returns the y coordinate of the starting location (tile coordinates).
	 */
	public int getY() {
		return y;
	}

	public int getWidth() {
		return width;
	}
	
	public String toString() {
		return x + "," + y;
	}
}
