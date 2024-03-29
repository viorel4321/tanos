package l2s.gameserver.network.l2.c2s;

import org.apache.commons.lang3.tuple.Pair;

import l2s.gameserver.listener.actor.player.OnAnswerListener;
import l2s.gameserver.model.Player;

public class DlgAnswer extends L2GameClientPacket
{
	private int _answer;
	private int _requestId;

	@Override
	protected void readImpl()
	{
		readD();
		_answer = readD();
		_requestId = readD();
	}

	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if(activeChar.isKeyBlocked())
		{
			activeChar.sendActionFailed();
			return;
		}
		final Pair<Integer, OnAnswerListener> entry = activeChar.getAskListener(true);
		if(entry == null || entry.getKey() != _requestId)
			return;
		final OnAnswerListener listener = entry.getValue();
		if(_answer == 1)
			listener.sayYes();
		else
			listener.sayNo();
	}
}
