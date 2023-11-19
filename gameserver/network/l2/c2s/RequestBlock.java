package l2s.gameserver.network.l2.c2s;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.SystemMessage;

public class RequestBlock extends L2GameClientPacket
{
	private static final Logger _log;
	private static final int BLOCK = 0;
	private static final int UNBLOCK = 1;
	private static final int BLOCKLIST = 2;
	private static final int ALLBLOCK = 3;
	private static final int ALLUNBLOCK = 4;
	private Integer _type;
	private String targetName;

	public RequestBlock()
	{
		targetName = null;
	}

	@Override
	protected void readImpl()
	{
		_type = readD();
		if(_type == 0 || _type == 1)
			targetName = readS(16);
	}

	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		switch(_type)
		{
			case 0:
			{
				activeChar.addToBlockList(targetName);
				break;
			}
			case 1:
			{
				activeChar.removeFromBlockList(targetName);
				break;
			}
			case 2:
			{
				final Collection<String> blockList = activeChar.getBlockList();
				if(blockList != null)
				{
					activeChar.sendPacket(new SystemMessage(613));
					for(final String name : blockList)
						activeChar.sendMessage(name);
					activeChar.sendPacket(new SystemMessage(490));
					break;
				}
				break;
			}
			case 3:
			{
				activeChar.setBlockAll(true);
				activeChar.sendPacket(new SystemMessage(961));
				activeChar.sendEtcStatusUpdate();
				break;
			}
			case 4:
			{
				activeChar.setBlockAll(false);
				activeChar.sendPacket(new SystemMessage(962));
				activeChar.sendEtcStatusUpdate();
				break;
			}
			default:
			{
				RequestBlock._log.info("Unknown 0x0a block type: " + _type);
				break;
			}
		}
	}

	static
	{
		_log = LoggerFactory.getLogger(RequestBlock.class);
	}
}
