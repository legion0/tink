package starcraftbot.proxybot.bot;

import javax.swing.JPanel;

import agents.Aagent;
import agents.MarineAgent;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import learning.Parameters;
import learning.Qlearner;
import starcraftbot.proxybot.Game;
import starcraftbot.proxybot.wmes.unit.EnemyUnitWME;
import starcraftbot.proxybot.wmes.unit.PlayerUnitWME;
import states.StateFull;
import states.StateUnitMin;
/**
 * Example implementation of the StarCraftBot.
 * 
 * This build will tell workers to mine, build additional workers,
 * and build additional supply units.
 */


public class MarineMeleeBot implements StarCraftBot {

	/** specifies that the agent is running */
	private boolean _running = true;
	private boolean _stopped = false;
	private Game _game;
	public JPanel getPanel() {
		return null;
	}
	
	private ArrayList<Aagent> _agents = new ArrayList<Aagent>();
	private static int MAX_GAME_FRAMES = 700;
	private static int games = 0, wins = 0;
	private static int stat_hp_total = 0, stat_hp_player = 0;
	private static int stat_units_total = 0, stat_units_player = 0;
	
	private Qlearner _ql = new Qlearner("db/MarineDB5.txt", new Parameters());

	
	/**
	 * Starts the bot.
	 * 
	 * The bot is now the owner of the current thread.
	 */
	public void start(Game game) {
		int round = 0;
		synchronized (game) {
			if(_running == false) {
				return;
			}
			_game = game;
			TreeMap<Integer, Integer> attacked = new TreeMap<Integer, Integer>();
			try {
				while (game.getGameFrame() < 5) {
					game.wait();
				}
			} catch (InterruptedException e2) {
				e2.printStackTrace();
			}
			for (EnemyUnitWME enemy : game.getEnemyUnits()) {
				attacked.put(enemy.getID(),0);
			}
			for (PlayerUnitWME unit : game.getPlayerUnits()) {
				_agents.add(new MarineAgent(game,_ql,unit.getID(),attacked));
			}
			System.out.println("XXX Loaded " + _agents.size() + " Agents playing vs " + game.getEnemyUnits().size() + " enemies.");
			boolean fail = false;
			while (_running) {			
				//System.out.println("XXX Starting round " + round);
				Iterator<Aagent> iti = _agents.iterator();
				while (iti.hasNext()) {
					Aagent agent = iti.next();
					//System.out.println("XXX Controling agent " + agent.getID());
					agent.turn();
					if(agent.isDead()){
						iti.remove();
					}
				}
				
				round++;
//				System.out.println(game.getCommandQueue().size());
				if(round>MAX_GAME_FRAMES){
					fail = true;
					game.getCommandQueue().leaveGame();					
				}
					
				try {
					game.wait();
				} catch (InterruptedException e2) {
					e2.printStackTrace();
					break;
				}							
			}
			if(fail) {
				_stopped = true;			
				return;
			}
			
			writeStatistics(game,round);
			_stopped = true;		
			_ql.persist();
		}
	}
	
	void writeStatistics(Game game, int round) {
		StateFull finalState = new StateFull(game);
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
		System.out.println("This Game Rounds: " + round + ", hp: " + playerHp + "/" + enemyHp + ", units: " + playerUnits + "/" + enemyUnits);
		System.out.println("Overall Games: " + games + " | Ratios: Wins: " + (wins/(double)games) + ", hp: " + (stat_hp_player/(double)stat_hp_total) + " units: " + (stat_units_player/(double)stat_units_total));
		StateUnitMin.EOG();
	}
	/**
	 * Tell the main thread to quit.
	 */
	public void stop() {
		_running = false;
		if(_game == null) {
			return;
		}		
		while(!_stopped) {		
			synchronized (_game) {			
				_game.notify();
			}
			Thread.yield();
		}
	}
}
