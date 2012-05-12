package eisbot.proxy;
/**
 * Example of a Java AI Client that does nothing.
 */
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.TreeMap;

import learning.Parameters;
import learning.Qlearner;

import agents.Aagent;
import agents.MarineAgent;

import states.StateFull;

import eisbot.proxy.model.Unit;
public class MarineClient implements BWAPIEventListener {
	
	private ArrayList<Aagent> _agents = new ArrayList<Aagent>();
	TreeMap<Integer, Integer> _attacked = new TreeMap<Integer, Integer>();
	private static int MAX_GAME_FRAMES = 700;
	private static int games = 0, wins = 0;
	private static long startedTraining = Calendar.getInstance().getTimeInMillis()/1000/60;
	private static int stat_hp_total = 0, stat_hp_player = 0;
	private static int stat_units_total = 0, stat_units_player = 0;
	private static int _round = 0;
	
	private Qlearner _ql = new Qlearner("db/MarineDB5.txt", new Parameters());

	JNIBWAPI bwapi;
	public static void main(String[] args) {
		new MarineClient();
	}
	
	public MarineClient() {
		bwapi = new JNIBWAPI(this);
		bwapi.start();
	} 

	public void connected() {}
	
	public void gameStarted() {
		for (Unit unit : bwapi.getEnemyUnits()) {
			if (unit.getTypeID() == Unit.Type_Marine) {
				_attacked.put(unit.getID(),0);
			}
		}
		for (Unit unit : bwapi.getMyUnits()) {
			if (unit.getTypeID() == Unit.Type_Marine) {
				_agents.add(new MarineAgent(bwapi,_ql,unit.getID(),_attacked));
			}
		}
		System.out.println("XXX Loaded " + _agents.size() + " Agents playing vs " + _attacked.size() + " enemies.");
	}
	public void gameUpdate() 
	{
		try {
//			System.out.println("XXX update frame: " + bwapi.getFrameCount() + " round: " + _round);
			Iterator<Aagent> iti = _agents.iterator();
			while (iti.hasNext()) {
				Aagent agent = iti.next();
				agent.turn();
				if(agent.isDead()){
					iti.remove();
				}
			}
			if(_round > MAX_GAME_FRAMES){
				// TODO: need code for surrender // bwapi.
			}
			_round++;
		} catch (Exception ex) { ex.printStackTrace(); }
	}
	
	void writeStatistics() {
		StateFull finalState = new StateFull(bwapi);
		games++;
		if (finalState.playerTotalHP() > 0)
			wins++;
		int playerHp = finalState.playerTotalHP();
		int enemyHp = finalState.enemyTotalHP();
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
