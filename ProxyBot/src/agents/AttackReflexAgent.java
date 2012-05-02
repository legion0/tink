package agents;

import java.util.TreeMap;

import starcraftbot.proxybot.Game;
import starcraftbot.proxybot.wmes.unit.EnemyUnitWME;
import starcraftbot.proxybot.wmes.unit.UnitWME;
import states.StateFull;

public class AttackReflexAgent {

	private static final int MAX_EHP = 8;
	private static final int MIN_EHP = -1;

	public static int getTarget(Game game, UnitWME attacker, TreeMap<Integer, Integer> attacked) {

		// r = closest enemy, rehp= MAX_HP
		int ret = StateFull.getClosestEnemy(attacker, game);
		int retEHP = MAX_EHP;
		double retDistance = StateFull.getDistance(attacker.getID(), ret, game);
		// for each enemy e
		for (EnemyUnitWME enemy: game.getEnemyUnits()) {
			double enemyDistance = StateFull.getDistance(enemy, attacker, game);
			int enemyEHP = StateFull.discreteHP(enemy.getHitPoints()) - attacked.get(enemy.getID());
			
			//	if inrange(e) && ehp(e)<rehp && ehp(e)>MIN_EHP
			if (MarineAgent.isInRange(enemyDistance)) {
				if (enemyEHP>MIN_EHP) {
					if (enemyEHP<retEHP || 
							(enemyEHP==retEHP && enemyDistance<retDistance)) {
						ret = enemy.getID();
						retEHP = enemyEHP;
						retDistance = enemyDistance;
					}
				}
			}
		}
		return ret;
	}
}
