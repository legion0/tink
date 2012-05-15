package agents;

import java.util.TreeMap;

import eisbot.proxy.JNIBWAPI;
import eisbot.proxy.model.Unit;
import states.StateFull;

public class AttackReflexAgent {

	private static final int MAX_EHP = 20;
	private static final int MIN_EHP = -2;
	private static final int GOOD_MIN_EHP = -3;

	public static int getTarget(JNIBWAPI game, Unit attacker, TreeMap<Integer, Integer> attacked) {

		// r = closest enemy, rehp= MAX_HP
		int ret = StateFull.getClosestEnemy(attacker, game);
		int retEHP = MAX_EHP;
		double retDistance = StateFull.getDistance(attacker.getID(), ret, game);
		// for each enemy e
		for (Unit enemy: game.getEnemyUnits()) {
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
	
	public static int getDragoonTarget(JNIBWAPI game, Unit attacker, TreeMap<Integer, Integer> attacked) {

		// r = closest enemy, rehp= MAX_HP
		int ret = StateFull.getClosestEnemy(attacker, game);
		int retEHP = MAX_EHP;
		double retDistance = StateFull.getDistance(attacker.getID(), ret, game);
		// for each enemy e
		for (Unit enemy: game.getEnemyUnits()) {
			double enemyDistance = StateFull.getDistance(enemy, attacker, game);
			int enemyHP = StateFull.dragoonHP(enemy.getHitPoints());
			int enemyShield = StateFull.dragoonShield(enemy.getShield());
			int enemyEHP = enemyHP + enemyShield - attacked.get(enemy.getID());
			
			//	if inrange(e) && ehp(e)<rehp && ehp(e)>MIN_EHP
			if (DragoonAgent.isInRange(enemyDistance)) {
				if (enemyEHP>GOOD_MIN_EHP) {
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
