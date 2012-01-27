package starcraftbot.proxybot;

import java.util.ArrayList;
import java.util.HashMap;

import starcraftbot.proxybot.Constants.Race;
import starcraftbot.proxybot.command.CommandQueue;
import starcraftbot.proxybot.wmes.BaseLocationWME;
import starcraftbot.proxybot.wmes.ChokePointWME;
import starcraftbot.proxybot.wmes.MapWME;
import starcraftbot.proxybot.wmes.PlayerWME;
import starcraftbot.proxybot.wmes.RegionWME;
import starcraftbot.proxybot.wmes.StartingLocationWME;
import starcraftbot.proxybot.wmes.TechTypeWME;
import starcraftbot.proxybot.wmes.UnitTypeWME;
import starcraftbot.proxybot.wmes.UpgradeTypeWME;
import starcraftbot.proxybot.wmes.unit.AllyUnitWME;
import starcraftbot.proxybot.wmes.unit.EnemyUnitWME;
import starcraftbot.proxybot.wmes.unit.GeyserWME;
import starcraftbot.proxybot.wmes.unit.MineralWME;
import starcraftbot.proxybot.wmes.unit.PlayerUnitWME;
import starcraftbot.proxybot.wmes.unit.UnitWME;
/**
 * StarCraft AI Interface.
 *
 * Maintains StarCraft state and provides hooks for StarCraft commands.
 *
 * Note: all coordinates are specified in tile coordinates.
 */
public class Game {

	/** the bots player ID */
	private int playerID;

	/** the bots race */
	private int playerRace;

	/** player information */
	private PlayerWME player;
	private PlayerWME enemy;

	/** all players */
	private ArrayList<PlayerWME> players;
	private PlayerWME[] playerArray = new PlayerWME[12];

	/** map information */
	private MapWME map;

	/** a list of the starting locations */
	private ArrayList<StartingLocationWME> startingLocations;

	/** a list of the units */
	private ArrayList<UnitWME> units = new ArrayList<UnitWME>();
	private HashMap<Integer, UnitWME> unitMap = new HashMap<Integer, UnitWME>();

	/** StarCraft unit types */
	private HashMap<Integer, UnitTypeWME> unitTypes = UnitTypeWME.getUnitTypeMap();

	/** list of tech types */
	private ArrayList<TechTypeWME> techTypes = TechTypeWME.getTechTypes();

	/** list of upgrade types */
	private ArrayList<UpgradeTypeWME> upgradeTypes = UpgradeTypeWME.getUpgradeTypes();

	/** queue of commands to execute */
	private CommandQueue commandQueue = new CommandQueue();

	/** timestamp of when the game state was last changed */
	private long lastGameUpdate = 0;

	private ArrayList<BaseLocationWME> baseLocations;

	private ArrayList<ChokePointWME> chokePoints;

	private ArrayList<RegionWME> regions;

	int frame = 0;

	private String update;

	/**
	 * Constructs a game object from the initial information sent from StarCraft.
	 *
	 * The game object will not have units until update is called.
	 */
	public Game(String playerData, String locationData, String mapData, String chokesData, String basesData, String regionData) {
    	String[] playerDatas = playerData.split(":");
    	playerID = Integer.parseInt(playerDatas[0].split(";")[1]);
		players = PlayerWME.getPlayers(playerData);

		for (PlayerWME p : players) {
			if (playerID == p.getPlayerID()) {
				player = p;
		    	playerRace = Race.valueOf(p.getRace()).ordinal();
			}
			else if (enemy == null) {
				enemy = p;
			}

			playerArray[p.getPlayerID()] = p;
		}

		map = new MapWME(mapData);
		startingLocations = StartingLocationWME.getLocations(locationData);
		baseLocations = BaseLocationWME.getLocations(basesData);
		regions = RegionWME.getRegions(regionData);
		chokePoints = ChokePointWME.getLocations(chokesData, regions);

		new Thread() {
			public void run() {
				while (true) {
					synchronized(Game.this) {
						try {
							Game.this.wait();
							if (update == null) {
								break;
							}

							updateData(update);
						}
						catch (Exception e) {}
					}
				}
			}
		}.start();
	}

	/**
	 * Wake up the game thread with a null update.
	 */
	public void stop() {
		synchronized(this) {
			update = null;
			this.notify();
		}
	}

	/**
	 * Returns the command queue.
	 */
	public CommandQueue getCommandQueue() {
		return commandQueue;
	}

	public int getGameFrame() {
		return frame;
	}

	public void update(String updateData) {
		this.update = updateData;

		synchronized(this) {
			this.notify();
		}
	}

	/**
	 * Updates the state of the game.
	 */
	public void updateData(String updateData) {
		frame++;
		player.update(updateData, enemy);
		units = UnitWME.getUnits(this, updateData, unitTypes, playerID, playerArray);
		lastGameUpdate = System.currentTimeMillis();

		HashMap<Integer, UnitWME> newMap = new HashMap<Integer, UnitWME>();
		for (UnitWME unit : units) {
			newMap.put(unit.getID(), unit);
		}

		unitMap = newMap;
	}

	/**
	 * Returns the time when the game state was last updated.
	 */
	public long getLastGameUpdate() {
		return lastGameUpdate;
	}

	/**
	 * Returns a player object for the bot.
	 */
	public PlayerWME getPlayer() {
		return player;
	}
	
	public PlayerWME getEnemy() {
		return enemy;
	}

	/**
	 * Returns the bots race.
	 */
	public int getPlayerRace() {
		return playerRace;
	}

	/**
	 * Returns a PlayerWME with the given PlayerID. Crashes if no such player exists.
	 */
	public PlayerWME getPlayerByID(int id) {
		return playerArray[id];
	}

	/**
	 * Returns the Map data.
	 */
	public MapWME getMap() {
		return map;
	}

	/**
	 * Returns a map of the tech types indexed by ID.
	 */
	public HashMap<Integer, UnitTypeWME> getUnitTypes() {
		return unitTypes;
	}

	/**
	 * Returns the starting locations.
	 */
	public ArrayList<StartingLocationWME> getStartingLocations() {
		return startingLocations;
	}

	/**
	 * Returns the player's starting location.
	 */
	public StartingLocationWME getPlayerStart() {
		int i;
		ArrayList<PlayerUnitWME> playerUnits = this.getPlayerUnits();
		ArrayList<StartingLocationWME> startingLocations = this.getStartingLocations();

		for (PlayerUnitWME unit:playerUnits) {
			if (unit.getIsCenter()) {
				i = 0;
				for (StartingLocationWME start:startingLocations) {
					if (start.getX() == unit.getX() && start.getY() == unit.getY()) {
						return start;
					}
					i++;
				}
			}
		}
		return null;
	}

	/**
	 * Returns the enemy's starting location, or null if it's unknown.
	 * If there are multiple enemy sides, it returns the start location of an arbitrary enemy.
	 */
	public StartingLocationWME getEnemyStart() {
		int i;
		ArrayList<EnemyUnitWME> enemyUnits = this.getEnemyUnits();
		ArrayList<StartingLocationWME> startingLocations = this.getStartingLocations();

		for (EnemyUnitWME unit:enemyUnits) {
			if (unit.getIsCenter()) {
				i = 0;
				for (StartingLocationWME start:startingLocations) {
					if (start.getX() == unit.getX() && start.getY() == unit.getY()) {
						return start;
					}
					i++;
				}
			}
		}
		return null;
	}

	/**
	 * Gets all units
	 */
	public ArrayList<UnitWME> getUnits() {
		return units;
	}

	public ArrayList<ChokePointWME> getChokePoints() {
		return chokePoints;
	}

	public ArrayList<RegionWME> getRegions() {
		return regions;
	}

	public ArrayList<BaseLocationWME> getBaseLocations() {
		return baseLocations;
	}

	/**
	 * Returns a list of the bots units.
	 */
	public ArrayList<PlayerUnitWME> getPlayerUnits() {
		ArrayList<PlayerUnitWME> playerUnits = new ArrayList<PlayerUnitWME>();
		for (UnitWME unit : units) {
			if (unit instanceof PlayerUnitWME) {
				playerUnits.add((PlayerUnitWME)unit);
			}
		}

		return playerUnits;
	}

	public UnitWME getUnitByID(int id) {
		return unitMap.get(id);
	}

	/**
	 * Returns a list of enemy units.
	 */
	public ArrayList<EnemyUnitWME> getEnemyUnits() {
		ArrayList<EnemyUnitWME> enemyUnits = new ArrayList<EnemyUnitWME>();
		for (UnitWME unit : units) {
			if (unit instanceof EnemyUnitWME) {
				enemyUnits.add((EnemyUnitWME)unit);
			}
		}

		return enemyUnits;
	}

	/**
	 * Returns a list of allied units.
	 */
	public ArrayList<AllyUnitWME> getAllyUnits() {
		ArrayList<AllyUnitWME> allyUnits = new ArrayList<AllyUnitWME>();
		for (UnitWME unit : units) {
			if (unit instanceof AllyUnitWME) {
				allyUnits.add((AllyUnitWME)unit);
			}
		}

		return allyUnits;
	}

	/**
	 * Returns the mineral patches.
	 */
	public ArrayList<MineralWME> getMinerals() {
		ArrayList<MineralWME> minerals= new ArrayList<MineralWME>();
		for (UnitWME unit : units) {
			if (unit instanceof MineralWME) {
				minerals.add((MineralWME)unit);
			}
		}

		return minerals;
	}

	/**
	 * Returns the list of geysers.
	 */
	public ArrayList<GeyserWME> getGeysers() {
		ArrayList<GeyserWME> gas = new ArrayList<GeyserWME>();
		for (UnitWME unit : units) {
			if (unit instanceof GeyserWME) {
				gas.add((GeyserWME)unit);
			}
		}

		return gas;
	}

	/**
	 * Returns the tech types.
	 */
	public ArrayList<TechTypeWME> getTechTypes() {
		return techTypes;
	}

	/**
	 * Returns the upgrade types.
	 */
	public ArrayList<UpgradeTypeWME> getUpgradeTypes() {
		return upgradeTypes;
	}
}
