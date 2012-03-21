package states;

import starcraftbot.proxybot.Game;
import starcraftbot.proxybot.wmes.unit.PlayerUnitWME;

public class StateUnitFull implements StateI {
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + _hp;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StateUnitFull other = (StateUnitFull) obj;
		if (_hp != other._hp)
			return false;
		return true;
	}
	private int _hp;
	public StateUnitFull(Game game, PlayerUnitWME unit) {
		_hp = unit.getHitPoints();
	}
}
