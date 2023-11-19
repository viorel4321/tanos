package l2s.gameserver.network.l2.s2c;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.SevenSigns;
import l2s.gameserver.model.entity.SevenSignsFestival.SevenSignsFestival;
import l2s.gameserver.templates.StatsSet;

public class SSQStatus extends L2GameServerPacket
{
	private static Logger _log = LoggerFactory.getLogger(SSQStatus.class);

	private Player _player;
	private int _page;
	private int period;

	public SSQStatus(final Player player, final int recordPage)
	{
		_player = player;
		_page = recordPage;
		period = SevenSigns.getInstance().getCurrentPeriod();
	}

	@Override
	protected final void writeImpl()
	{
		if(!Config.ALLOW_SEVEN_SIGNS)
			return;

		writeC(245);
		writeC(_page);
		writeC(period);
		switch(_page)
		{
			case 1:
			{
				writeD(SevenSigns.getInstance().getCurrentCycle());
				switch(period)
				{
					case 0:
					{
						writeD(1183);
						break;
					}
					case 1:
					{
						writeD(1176);
						break;
					}
					case 2:
					{
						writeD(1184);
						break;
					}
					case 3:
					{
						writeD(1177);
						break;
					}
				}
				switch(period)
				{
					case 0:
					case 2:
					{
						writeD(1287);
						break;
					}
					case 1:
					case 3:
					{
						writeD(1286);
						break;
					}
				}
				writeC(SevenSigns.getInstance().getPlayerCabal(_player));
				writeC(SevenSigns.getInstance().getPlayerSeal(_player));
				writeD(SevenSigns.getInstance().getPlayerStoneContrib(_player));
				writeD(SevenSigns.getInstance().getPlayerAdenaCollect(_player));
				final long dawnStoneScore = SevenSigns.getInstance().getCurrentStoneScore(2);
				final long dawnFestivalScore = SevenSigns.getInstance().getCurrentFestivalScore(2);
				final long dawnTotalScore = dawnStoneScore + dawnFestivalScore;
				final long duskStoneScore = SevenSigns.getInstance().getCurrentStoneScore(1);
				final long duskFestivalScore = SevenSigns.getInstance().getCurrentFestivalScore(1);
				final long duskTotalScore = duskStoneScore + duskFestivalScore;
				long totalStoneScore = duskStoneScore + dawnStoneScore;
				totalStoneScore = totalStoneScore == 0L ? 1L : totalStoneScore;
				final long duskStoneScoreProp = duskStoneScore * 500L / totalStoneScore;
				final long dawnStoneScoreProp = dawnStoneScore * 500L / totalStoneScore;
				long totalOverallScore = duskTotalScore + dawnTotalScore;
				totalOverallScore = totalOverallScore == 0L ? 1L : totalOverallScore;
				final long dawnPercent = dawnTotalScore * 100L / totalOverallScore;
				final long duskPercent = duskTotalScore * 100L / totalOverallScore;
				if(Config.DEBUG)
				{
					SSQStatus._log.info("Dusk Stone Score: " + duskStoneScore + " - Dawn Stone Score: " + dawnStoneScore);
					SSQStatus._log.info("Dusk Festival Score: " + duskFestivalScore + " - Dawn Festival Score: " + dawnFestivalScore);
					SSQStatus._log.info("Dusk Score: " + duskTotalScore + " - Dawn Score: " + dawnTotalScore);
					SSQStatus._log.info("Overall Score: " + totalOverallScore);
					SSQStatus._log.info("");
					SSQStatus._log.info("Dusk Prop: " + duskStoneScore / totalStoneScore * 500L + " - Dawn Prop: " + dawnStoneScore / totalStoneScore * 500L);
					SSQStatus._log.info("Dusk %: " + duskPercent + " - Dawn %: " + dawnPercent);
				}
				writeD((int) duskStoneScoreProp);
				writeD((int) duskFestivalScore);
				writeD((int) duskStoneScoreProp + (int) duskFestivalScore);
				writeC((int) duskPercent);
				writeD((int) dawnStoneScoreProp);
				writeD((int) dawnFestivalScore);
				writeD((int) dawnStoneScoreProp + (int) dawnFestivalScore);
				writeC((int) dawnPercent);
				break;
			}
			case 2:
			{
				if(SevenSigns.getInstance().isSealValidationPeriod())
					writeH(0);
				else
					writeH(1);
				writeC(5);
				for(int i = 0; i < 5; ++i)
				{
					writeC(i + 1);
					writeD(SevenSignsFestival.FESTIVAL_LEVEL_SCORES[i]);
					final int duskScore = SevenSignsFestival.getInstance().getHighestScore(1, i);
					final int dawnScore = SevenSignsFestival.getInstance().getHighestScore(2, i);
					writeD(duskScore);
					StatsSet highScoreData = SevenSignsFestival.getInstance().getHighestScoreData(1, i);
					String[] partyMembers = highScoreData.getString("members").split(",");
					if(partyMembers != null)
					{
						writeC(partyMembers.length);
						for(final String partyMember : partyMembers)
							writeS(partyMember);
					}
					else
						writeC(0);
					writeD(dawnScore);
					highScoreData = SevenSignsFestival.getInstance().getHighestScoreData(2, i);
					partyMembers = highScoreData.getString("members").split(",");
					if(partyMembers != null)
					{
						writeC(partyMembers.length);
						for(final String partyMember : partyMembers)
							writeS(partyMember);
					}
					else
						writeC(0);
				}
				break;
			}
			case 3:
			{
				writeC(10);
				writeC(35);
				writeC(3);
				int totalDawnProportion = 1;
				int totalDuskProportion = 1;
				for(int j = 1; j <= 3; ++j)
				{
					totalDawnProportion += SevenSigns.getInstance().getSealProportion(j, 2);
					totalDuskProportion += SevenSigns.getInstance().getSealProportion(j, 1);
				}
				totalDawnProportion = Math.max(1, totalDawnProportion);
				totalDuskProportion = Math.max(1, totalDuskProportion);
				for(int j = 1; j <= 3; ++j)
				{
					final int dawnProportion = SevenSigns.getInstance().getSealProportion(j, 2);
					final int duskProportion = SevenSigns.getInstance().getSealProportion(j, 1);
					writeC(j);
					writeC(SevenSigns.getInstance().getSealOwner(j));
					writeC(duskProportion * 100 / totalDuskProportion);
					writeC(dawnProportion * 100 / totalDawnProportion);
				}
				break;
			}
			case 4:
			{
				writeC(SevenSigns.getInstance().getCabalHighestScore());
				writeC(3);
				for(int j = 1; j < 4; ++j)
				{
					final int dawnProportion = SevenSigns.getInstance().getSealProportion(j, 2);
					final int duskProportion = SevenSigns.getInstance().getSealProportion(j, 1);
					final int totalProportion = dawnProportion + duskProportion;
					final int sealOwner = SevenSigns.getInstance().getSealOwner(j);
					writeC(j);
					writeC(sealOwner);
					if(sealOwner != 0)
					{
						if(totalProportion >= 10)
							writeH(1289);
						else
							writeH(1291);
					}
					else if(totalProportion >= 35)
						writeH(1290);
					else
						writeH(1292);
					if(SevenSigns.getInstance().isSealValidationPeriod())
						writeH(1);
					else
						writeH(0);
				}
				break;
			}
		}
	}
}
