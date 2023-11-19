package l2s.gameserver.scripts;

import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.utils.Strings;

public final class Events
{
	public static boolean onAction(final Player player, final GameObject obj, final boolean shift)
	{
		if(!shift)
		{
			final Scripts.ScriptClassAndMethod handler = Scripts.onAction.get(obj.getL2ClassShortName());
			return handler != null && Strings.parseBoolean(Scripts.getInstance().callScripts(player, handler.className, handler.methodName, new Object[] {
					player,
					obj }));
		}
		if(player.getVarBoolean("noShift"))
			return false;
		Scripts.ScriptClassAndMethod handler = Scripts.onActionShift.get(obj.getL2ClassShortName());
		if(handler == null && obj.isNpc())
			handler = Scripts.onActionShift.get("NpcInstance");
		if(handler == null && obj.isSummon())
			handler = Scripts.onActionShift.get("L2SummonInstance");
		if(handler == null && obj.isPet())
			handler = Scripts.onActionShift.get("PetInstance");
		return handler != null && Strings.parseBoolean(Scripts.getInstance().callScripts(player, handler.className, handler.methodName, new Object[] {
				player,
				obj }));
	}
}
