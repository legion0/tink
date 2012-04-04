package starcraftbot.proxybot.bot;

import javax.swing.JPanel;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;

import actions.ActionI.ACTION;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import learning.Qlearner;
import misc.customDateFormatStamp;
import starcraftbot.proxybot.Game;
import starcraftbot.proxybot.Constants.Order;
import starcraftbot.proxybot.Constants.Race;
import starcraftbot.proxybot.ProxyBot;
import starcraftbot.proxybot.wmes.UnitTypeWME;
import starcraftbot.proxybot.wmes.UnitTypeWME.UnitType;
import starcraftbot.proxybot.wmes.unit.EnemyUnitWME;
import starcraftbot.proxybot.wmes.unit.PlayerUnitWME;
import starcraftbot.proxybot.wmes.unit.UnitWME;
import states.StateFull;
import states.StateI;
import states.StateUnitMin;
/**
 * Example implementation of the StarCraftBot.
 * 
 * This build will tell workers to mine, build additional workers,
 * and build additional supply units.
 */
public class MarineMeleeBot implements StarCraftBot {

	/** specifies that the agent is running */
	boolean running = true;
	
	public JPanel getPanel() {
		return null;
	}
	
	private List<EnemyUnitWME> _enemies;
	
	private static Double REWARD_DEATH = -100.0;
	
	private boolean _persistGame = true;
	
	private LinkedHashMap<Integer, StateUnitMin> _lastStates = new LinkedHashMap<Integer, StateUnitMin>(5);
	private LinkedHashMap<Integer, ACTION> _lastAction = new LinkedHashMap<Integer, ACTION>(5);
	private LinkedHashMap<Integer, Integer> _lastHitFrame = new LinkedHashMap<Integer, Integer>(5);
	
	private static int games = 0, wins = 0;
	
	
	private Qlearner _ql = new Qlearner("db/MarineDB4.txt");

	
	/**
	 * Starts the bot.
	 * 
	 * The bot is now the owner of the current thread.
	 */
	public void start(Game game) {
		
		BufferedWriter bw = null;
		StringBuffer sb = null;
		String stamp = new customDateFormatStamp().format(new Date());
		
		if (_persistGame) {
			sb = new StringBuffer();
		}
		int round = 0;
		// run until told to exit
		int lastGameFrame = 0;
		while (running) {
			/*try {
				Thread.sleep(6*(ProxyBot.gameSpeed+1));
			}
			catch (Exception e) {}*/
			synchronized (game) {
				try {
					game.wait();
				} catch (InterruptedException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
					break;
				}
				if (game.getGameFrame() - lastGameFrame < 7)
					continue;
				lastGameFrame = game.getGameFrame();
				//System.out.println(game.getGameFrame());
				
				_enemies = game.getEnemyUnits();
				
				if (_persistGame)
					try {
						sb.append(new StateFull(game).toString() + '\n');
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
				
				for (PlayerUnitWME unit : game.getPlayerUnits()) {
					StateUnitMin lastState = _lastStates.get(unit.getID());
					StateUnitMin state = null;
					try {
//						Integer oldHitFrame = _lastHitFrame.get(unit.getID());
//						oldHitFrame = oldHitFrame == null ? -10 : oldHitFrame;
//						int newHitFrame = game.getGameFrame();
						state = new StateUnitMin(game, unit, false);
						if (lastState != null && state.getHitPoints() != lastState.getHitPoints())
							state = new StateUnitMin(game, unit, true);
					} catch (JsonGenerationException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (JsonMappingException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					ACTION a = _ql.getAction(state);
					if (lastState != null) {
						Double r = StateUnitMin.reward((StateUnitMin)lastState, a, (StateUnitMin)state);
						r -= round*0.01;
						_ql.update(lastState, a, state, r);
					}
					_lastStates.put(unit.getID(), state);
					_lastAction.put(unit.getID(), a);
					
					if (_persistGame) {
						sb.append(a.name() + ' ');
					}
					StateUnitMin.perfomAction(a, unit, game);
				}
				
				ArrayList<Integer> toDelete = new ArrayList<Integer>();
				reverseDeathScan: for (Entry<Integer, StateUnitMin> entry : _lastStates.entrySet()) {
					int unitId = entry.getKey();
					for (PlayerUnitWME unit : game.getPlayerUnits()) {
						if (unit.getID() == unitId)
							continue reverseDeathScan;
					}
					// unit is dead
					toDelete.add(entry.getKey());
					StateUnitMin lastState = _lastStates.get(unitId);
					ACTION lastAction = _lastAction.get(unitId);
					double reward = -(double)(10*lastState.enemyTotalHP());
					System.out.println("Unit " + unitId + " had dies with reward " + reward);
					_ql.update(lastState, lastAction, null, reward);
				}
				for (int id : toDelete) {
					_lastStates.remove(id);
				}
				
				
				if (_persistGame) {
						sb.append("\n");
				}
				round++;
			}
		}
		games++;
		if (game.getEnemyUnits().size() == 0)
			wins++;
		System.out.println("Rounds: " + round + " Games: " + games + " Ratio: " + (wins/(double)games));
		StateUnitMin.EOG();
		_ql.persist();
		
		if (_persistGame)
			try {
				bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("logs/log" + stamp + ".txt")));
				bw.write(sb.toString());
				bw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}


	/**
	 * Tell the main thread to quit.
	 */
	public void stop() {
		running = false;
	}
}
