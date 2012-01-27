package starcraftbot.proxybot.wmes;

import java.util.ArrayList;

public class RegionWME extends WME {

	private int centerX;
	
	private int centerY;
	
	private int id;
	
	private int numPoints;
	private int x[];
	private int y[];
	
	private ArrayList<ChokePointWME> chokePoints = new ArrayList<ChokePointWME>();
	
	/**
	 * Parses the regions.
	 */
	public static ArrayList<RegionWME> getRegions(String regionData) {
		ArrayList<RegionWME> locations = new ArrayList<RegionWME>();
	
		String[] locs = regionData.split(":");
		boolean first = true;
		
		for (String location : locs) {
			if (first) {
				first = false;
				continue;
			}
			
			String[] coords = location.split(";");

			RegionWME region = new RegionWME();
			region.id = Integer.parseInt(coords[0]);
			region.centerX = Integer.parseInt(coords[1]);
			region.centerY = Integer.parseInt(coords[2]);
			region.numPoints = Integer.parseInt(coords[3]);
			region.x = new int[region.numPoints];
			region.y = new int[region.numPoints];
			
			for (int i=0; i<region.numPoints; i++) {
				region.x[i] = Integer.parseInt(coords[5 + 2*i]);
				region.y[i] = Integer.parseInt(coords[6 + 2*i]);
			}
			
			locations.add(region);
		}
		
		return locations;		
	}

	public int getID() {
		return id;
	}
	
	public int getCenterX() {
		return centerX;
	}

	public int getCenterY() {
		return centerY;
	}
	
	public int getNumPoints() {
		return numPoints;
	}
	
	public int[] getPointsX() {
		return x;
	}
	
	public int[] getPointsY() {
		return y;
	}
	
	public ArrayList<ChokePointWME> getChokePoints() {
		return chokePoints;
	}
}
