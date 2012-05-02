package starcraftbot.proxybot.wmes;

import java.util.ArrayList;

import starcraftbot.proxybot.Constants.Race;
import starcraftbot.proxybot.wmes.TechTypeWME.TechType;
import starcraftbot.proxybot.wmes.UpgradeTypeWME.UpgradeType;

/**
 * Stores information about a player
 * 
 * Note: the supply used and supply total variables are double what you would expect, because
 *       small units are represented as 1 supply in StarCraft.
 */
public class PlayerWME extends WME {

	/** the player identifier */
	private int playerID;

	/** current mineral supply */
	private int minerals;
	
	/** current gas supply */
	private int gas;
	
	/** amount of supply used by the player */
	private int supplyUsed;

	/** amount of supply provided by the player */
	private int supplyTotal;
	
	/** the players race */
	private Race race;

	private int raceID;
	
	/** specifies if the player is an ally */
	private boolean ally;

	private boolean[] hasResearched = new boolean[47];
	private boolean[] isResearching = new boolean[47];
	private int[] upgradeLevel = new int[63];
	private boolean[] isUpgrading = new boolean[63];

	/**
	 * Parses the player data.
	 */
	public static ArrayList<PlayerWME> getPlayers(String playerData) {
		ArrayList<PlayerWME> players = new ArrayList<PlayerWME>();
		
		String[] playerDatas = playerData.split(":");
		boolean first = true;
		
		for (String data : playerDatas) {
			if (first) {
				first = false;
				continue;
			}

			String[] attributes = data.split(";");
			PlayerWME player = new PlayerWME();
			player.playerID = Integer.parseInt(attributes[0]);			
			player.race = Race.valueOf(attributes[1]);		
			player.raceID = player.race.ordinal();
			Integer.parseInt(attributes[3]);			
			player.ally = attributes[4].equals("1");						
			players.add(player);
		}		

		return players;
	}

	/**
	 * Updates the players attributes given the command data.
	 * 
	 * Expects a message of the form "status;minerals;gas;supplyUsed;SupplyTotal:..."
	 */
	public void update(String playerData, PlayerWME enemy) {		
		String[] attributes = playerData.split(":")[0].split(";");
		
		minerals = Integer.parseInt(attributes[1]);
		gas = Integer.parseInt(attributes[2]);
		supplyUsed = Integer.parseInt(attributes[3]);
		supplyTotal = Integer.parseInt(attributes[4]);
		String researchUpdate = attributes[5];
		String upgradeUpdate = attributes[6];

		// research
		final int r = hasResearched.length;
		for (int i=0; i<r; i++) {
			hasResearched[i] = Integer.parseInt("" + researchUpdate.charAt(i)) > 0;
		}

		for (int i=0; i<r; i++) {
			isResearching[i] = Integer.parseInt("" + researchUpdate.charAt(r + i)) > 0;
		}
		
		for (int i=0; i<r; i++) {
			enemy.hasResearched[i] = Integer.parseInt("" + researchUpdate.charAt(2*r + i)) > 0;
		}		

		// upgrades
		final int u = upgradeLevel.length;
		for (int i=0; i<u; i++) {
			upgradeLevel[i] = Integer.parseInt("" + upgradeUpdate.charAt(i));
		}
		
		for (int i=0; i<u; i++) {
			isUpgrading[i] = Integer.parseInt("" + upgradeUpdate.charAt(u + i)) > 0;
		}

		for (int i=0; i<u; i++) {
			enemy.upgradeLevel[i] = Integer.parseInt("" + upgradeUpdate.charAt(2*u + i));
		}
	}
	
	public boolean[] getResearched() {
		return hasResearched;
	}

	public int[] getUpgrades() {
		return upgradeLevel;
	}

	public int upgradeLevel(UpgradeType type) {
		return upgradeLevel[type.ordinal()];
	}

	public boolean isUpgrading(UpgradeType type) {
		return isUpgrading[type.ordinal()];
	}

	public boolean hasResearched(TechType type) {
		return hasResearched[type.ordinal()];
	}

	public boolean isResearching(TechType type) {
		return isResearching[type.ordinal()];
	}

	public boolean getUpgradingVehicleWeapons() {
		return isUpgrading[UpgradeType.Terran_Vehicle_Weapons.ordinal()];
	}

	public int getVehicleWeaponsLevel() {
		return upgradeLevel[UpgradeType.Terran_Vehicle_Weapons.ordinal()];
	}

	public boolean getUpgradingVehicleArmor() {
		return isUpgrading[UpgradeType.Terran_Vehicle_Plating.ordinal()];
	}

	public int getVehicleArmorLevel() {
		return upgradeLevel[UpgradeType.Terran_Vehicle_Plating.ordinal()];
	}
	
	public boolean getIsResearchingSiege() {
		return isResearching[TechType.Tank_Siege_Mode.ordinal()];
	}

	public boolean getHasResearchedSiege() {
		return hasResearched[TechType.Tank_Siege_Mode.ordinal()];
	}

	public boolean getIsResearchingMines() {
		return isResearching[TechType.Spider_Mines.ordinal()];
	}

	public boolean getHasResearchedMines() {
		return hasResearched[TechType.Spider_Mines.ordinal()];
	}
	
	/**
	 * Returns if the player is an ally.
	 */
	public boolean isAlly() {
		return ally;
	}
	
	/**
	 * Returns the player's mineral count, only accurate for the bot player.
	 */
	public int getMinerals() {
		return minerals;
	}

	/**
	 * Returns the player's gas count, only accurate for the bot player.
	 */
	public int getGas() {
		return gas;
	}

	/**
	 * Gets the current supply used. (Its double the expected value)
	 */
	public int getSupplyUsed() {
		return supplyUsed;
	}

	/**
	 * Gets the current supply provided . (Its double the expected value)
	 */
	public int getSupplyTotal() {
		return supplyTotal;
	}

	/** 
	 * Returns a unique id for the player.
	 */
	public int getPlayerID() {
		return playerID;
	}

	/**
	 * Returns the players race.
	 */
	public String getRace() {
		return race.toString();
	}
	
	public Race getPlayerRace() {
		return race;
	}
	
	/**
	 * Returns the players race ID.
	 */
	public int getRaceID() {
		return raceID;
	}

	public String toString() {
		return 
			"mins:" + minerals +
			" gas:" + gas +
			" supplyUsed:" + supplyUsed +
			" supplyTotal:" + supplyTotal;
	}
}