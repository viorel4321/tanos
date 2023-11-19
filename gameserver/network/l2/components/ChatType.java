package l2s.gameserver.network.l2.components;

/**
 * @author VISTALL
 * @date  12:48/29.12.2010
 */
public enum ChatType
{
	ALL, //0
	SHOUT, //1    !
	TELL, //2    "
	PARTY, //3   #
	CLAN, //4    @
	GM, //5
	PETITION_PLAYER, //6   used for petition
	PETITION_GM, //7   * used for petition
	TRADE, //8  +
	ALLIANCE, //9   $
	ANNOUNCEMENT, //10
	SYSTEM_MESSAGE, //11
	FRIENDTELL,
	MSNCHAT,
	PARTY_ROOM, //14
	COMMANDCHANNEL_ALL, //15 ``
	COMMANDCHANNEL_COMMANDER, //16  `
	HERO_VOICE, //17 %
	CRITICAL_ANNOUNCE; //18

	public static final ChatType[] VALUES = values();
}
