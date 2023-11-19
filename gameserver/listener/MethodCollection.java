package l2s.gameserver.listener;

public interface MethodCollection
{
	public static final String ReduceCurrentHp = "Creature.ReduceCurrentHp";
	public static final String ZoneObjectEnter = "Zone.onZoneEnter";
	public static final String ZoneObjectLeave = "Zone.onZoneLeave";
	public static final String AbstractAInotifyEvent = "AbstractAI.notifyEvent";
	public static final String AbstractAIsetIntention = "AbstractAI.setIntention";
	public static final String onStartAttack = "Creature.doAttack";
	public static final String onStartCast = "Creature.doCast";
	public static final String onStartAltCast = "Creature.altUseSkill";
	public static final String OnAttacked = "Creature.onHitTimer";
	public static final String onDecay = "Creature.onDecay";
	public static final String doDie = "Creature.doDie";
	public static final String onKill = "Creature.doDie.KillerNotifier";
}
