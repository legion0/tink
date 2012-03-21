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

import learning.Qlearner;
import misc.customDateFormatStamp;
import starcraftbot.proxybot.Game;
import starcraftbot.proxybot.Constants.Order;
import starcraftbot.proxybot.Constants.Race;
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
	
	private boolean _persistGame = true;
	
	private LinkedHashMap<Integer, StateI> _lastStates = new LinkedHashMap<Integer, StateI>(5);
	
	private Qlearner _ql = new Qlearner("db/MarineDB1.txt");

	
	/**
	 * Starts the bot.
	 * 
	 * The bot is now the owner of the current thread.
	 */
	public void start(Game game) {
		
		BufferedWriter bw = null;
		String stamp = new customDateFormatStamp().format(new Date());
		
		if (_persistGame)
			try {
				bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("log" + stamp + ".txt")));
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		int round = 0;
		// run until told to exit
		while (running) {
			try {
				Thread.sleep(1000);
			}
			catch (Exception e) {}
			_enemies = game.getEnemyUnits();
			
			if (_persistGame)
				try {
					bw.write(new StateFull(game).toString() + '\n');
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
				StateI state = null;
				try {
					state = new StateUnitMin(game, unit);
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
				
				if (round > 0) {
					StateI lastState = _lastStates.get(unit.getID());
					Double r = StateUnitMin.reward((StateUnitMin)lastState, a, (StateUnitMin)state);
					_ql.update(lastState, a, state, r);
				}
				_lastStates.put(unit.getID(), state);
				
				if (_persistGame)
					try {
						bw.write(a.name() + ' ');
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				try {
					bw.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				switch(a) {
				case ACTION_RETREAT:
					game.getCommandQueue().rightClick(unit.getID(), 1, 1);
					break;
				case ACTION_ATTACK:
					game.getCommandQueue().rightClick(unit.getID(), getClosestEnemy(unit));
					break;
				}
			}
			
			if (_persistGame)
				try {
					bw.write("\n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			round++;
		}
		
		if (_persistGame)
			try {
				bw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	private int getClosestEnemy(PlayerUnitWME unit) {
		int id = -1;
		double closest = Double.MAX_VALUE;
		
		for (UnitWME e : _enemies) {
			double dx = unit.getX() - e.getX();
			double dy = unit.getY() - e.getY();
			double dist = Math.sqrt(dx*dx + dy*dy); 

			if (dist < closest) {
				id = e.getID();
				closest = dist;
			}
		}
		return id;
	}


	/**
	 * Tell the main thread to quit.
	 */
	public void stop() {
		running = false;
		_ql.persist();
	}
}
