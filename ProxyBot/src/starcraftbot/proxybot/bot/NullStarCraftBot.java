package starcraftbot.proxybot.bot;

import javax.swing.JPanel;

import starcraftbot.proxybot.Game;
/**
 * Empty implementation of the StarCraft bot interface.
 */
public class NullStarCraftBot implements StarCraftBot {

	public void start(Game game) {
	}

	public void stop() {
	}
	
	public JPanel getPanel() {
		return null;
	}
}
