package eisbot.proxy;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.TreeMap;

import learning.Parameters;
import learning.Qlearner;

import agents.Aagent;
import agents.DragoonAgent;
import agents.MarineAgent;

import states.StateFull;

import eisbot.proxy.model.Unit;
import eisbot.proxy.types.UnitType;
public class MarineClient implements BWAPIEventListener {
	
	private ArrayList<Aagent> _agents = new ArrayList<Aagent>();
	TreeMap<Integer, Integer> _attacked = new TreeMap<Integer, Integer>();
	private static int MAX_GAME_FRAMES = 3000;
	private static int games = 0, wins = 0;
	private static long startedTraining = Calendar.getInstance().getTimeInMillis()/1000/60;
	private static int stat_hp_total = 0, stat_hp_player = 0;
	private static int stat_units_total = 0, stat_units_player = 0;
	private static int _round = 0;
	private static boolean _dontSave = false;
	private Qlearner _ql = new Qlearner("db/DragoonDB.txt", new Parameters());

	JNIBWAPI bwapi;
	public static void main(String[] args) {
		new MarineClient();
	}
	
	public MarineClient() {
		bwapi = new JNIBWAPI(this);
		bwapi.drawTargets(true);
		bwapi.start();
	} 

	public void connected() {}
	
	public void gameStarted() {
		_agents = new ArrayList<Aagent>();
		_attacked = new TreeMap<Integer, Integer>();
//		_ql = new Qlearner("db/twirl db.txt", new Parameters());
		_ql = new Qlearner("db/DragoonDB.txt", new Parameters());
//		bwapi.setGameSpeed(5);		
		_round =0;
		_dontSave = false;
		for (Unit unit : bwapi.getEnemyUnits()) {
			if (unit.getTypeID() == UnitType.UnitTypes.Terran_Marine.ordinal()) {				
				_attacked.put(unit.getID(),0);
			}
			if (unit.getTypeID() == UnitType.UnitTypes.Protoss_Dragoon.ordinal()) {				
				_attacked.put(unit.getID(),0);
			}			
		}
		for (Unit unit : bwapi.getMyUnits()) {
			if (unit.getTypeID() == UnitType.UnitTypes.Terran_Marine.ordinal()) {
				_agents.add(new MarineAgent(bwapi,_ql,unit.getID(),_attacked));
			}
			if (unit.getTypeID() == UnitType.UnitTypes.Protoss_Dragoon.ordinal()) {
				_agents.add(new DragoonAgent(bwapi,_ql,unit.getID(),_attacked));
			}			
		}
		System.out.println("XXX Loaded " + _agents.size() + " Agents playing vs " + _attacked.size() + " enemies.");
	}

	public void gameUpdate() 
	{
//		if(_agents.size() ==0){
//			gameStarted();
//		}
		try {
//			System.out.println("XXX update frame: " + bwapi.getFrameCount() + " round: " + _round);
			Iterator<Aagent> iti = _agents.iterator();
			if(_round > MAX_GAME_FRAMES){
				_dontSave = true;
				for (Unit unit : bwapi.getMyUnits()) {
//					if (unit.getTypeID() == Unit.Type_Marine) {
					if (unit.getTypeID() == UnitType.UnitTypes.Protoss_Dragoon.ordinal() ||
							unit.getTypeID() == UnitType.UnitTypes.Terran_Marine.ordinal()) {
						int enemyID = StateFull.getClosestEnemy(unit,bwapi);
						bwapi.attack(unit.getID(), enemyID);
					}
				}				
			} else {
				while (iti.hasNext()) {
					Aagent agent = iti.next();
					agent.turn();
					if(agent.isDead()){
						iti.remove();
					}
				}
			}
			_round++;
		} catch (Exception ex) { ex.printStackTrace(); }
	}
	
	void writeStatistics() {
		StateFull finalState = new StateFull(bwapi);
		games++;
		if (finalState.playerTotalHP() > 0) {
			wins++;
		}
//		int playerHp = finalState.playerTotalHP();
//		int enemyHp = finalState.enemyTotalHP();
		int playerHp = finalState.DragoonPlayerTotalHP();
		int enemyHp = finalState.DragoonEnemyTotalHP();
		int totalHp = playerHp + enemyHp;
		int playerUnits = finalState.playerUnitCount();
		int enemyUnits = finalState.enemyUnitCount();
		int totalUnits = playerUnits + enemyUnits;
		stat_hp_player += playerHp;
		stat_hp_total += totalHp;
		stat_units_player += playerUnits;
		stat_units_total += totalUnits;
		long elapsed = Calendar.getInstance().getTimeInMillis()/1000/60 - startedTraining;
		System.out.println("This Game Rounds: " + _round + ", hp: " + playerHp + "/" + enemyHp + ", units: " + playerUnits + "/" + enemyUnits);
		System.out.println("Overall Games: " + games + "(" + elapsed + "min)" + " | Ratios: Wins: " + (wins/(double)games) + ", hp: " + (stat_hp_player/(double)stat_hp_total) + " units: " + (stat_units_player/(double)stat_units_total));
	}
	
	public void gameEnded() {
		System.out.println("gameEnded");
		if(_dontSave)
			return;
		writeStatistics();
		_ql.persist();
	}
	
	public void matchEnded(boolean winner) {
		System.out.println("matchEnded " + winner);
	}
	
	public void keyPressed(int keyCode) {}
	public void nukeDetect(int x, int y) { }
	public void nukeDetect() { }
	public void playerLeft(int id) { }
	public void unitCreate(int unitID) { }
	public void unitDestroy(int unitID) { }
	public void unitDiscover(int unitID) { }
	public void unitEvade(int unitID) { }
	public void unitHide(int unitID) { }
	public void unitMorph(int unitID) { }
	public void unitShow(int unitID) { }
}
