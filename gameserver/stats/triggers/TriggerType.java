package l2s.gameserver.stats.triggers;

/**
* @author VISTALL
* @date 15:05/22.01.2011
*/
public enum TriggerType
{
	ADD, // Срабатывает при добавлении скилла.
	ATTACK,
	RECEIVE_DAMAGE,
	CRIT,
	OFFENSIVE_SKILL_USE,
	OFFENSIVE_PHYSICAL_SKILL_USE,
	OFFENSIVE_MAGICAL_SKILL_USE,
	SUPPORT_SKILL_USE,
	SUPPORT_PHYSICAL_SKILL_USE,
	SUPPORT_MAGICAL_SKILL_USE,
	UNDER_MISSED_ATTACK,
	DIE,
	IDLE, // Срабатывает каждый раз через определенное время. В качестве таймера используется время отката умения, к которому привязан триггер.
	ON_REVIVE,	// Срабатывает при воскрешении персонажа.
	ON_START_EFFECT, // Срабатывает при старте эффекта.
	ON_EXIT_EFFECT, // Срабатывает по завершению эффекта (любым способом: время вышло, принудительно и т.д.).
	ON_FINISH_EFFECT, // Срабатывает по завершению времени действия эффекта.
	ON_START_CAST, // Срабатывает при начале каста.
	ON_TICK_CAST, // Срабатывает во время тика каста.
	ON_END_CAST, // Срабатывает после завершения времени каста скилла.
	ON_FINISH_CAST, // Срабатывает после успешного использования скилла (учитывая c coolTime).
	ON_ENTER_WORLD, // Срабатывает при входе в игру.
	ON_KILL,	// Срабатывает при убийстве противника.
	ON_CAST_SKILL;	// Срабатывает при касте определенного скилла.
}
