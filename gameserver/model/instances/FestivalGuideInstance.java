package l2s.gameserver.model.instances;

import java.util.Calendar;
import java.util.List;

import l2s.gameserver.Config;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.SevenSigns;
import l2s.gameserver.model.entity.SevenSignsFestival.SevenSignsFestival;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.npc.NpcTemplate;

public final class FestivalGuideInstance extends NpcInstance
{
	protected int _festivalType;
	protected int _festivalOracle;

	public FestivalGuideInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
		switch(getNpcId())
		{
			case 31127:
			case 31132:
			{
				_festivalType = 0;
				_festivalOracle = 2;
				break;
			}
			case 31128:
			case 31133:
			{
				_festivalType = 1;
				_festivalOracle = 2;
				break;
			}
			case 31129:
			case 31134:
			{
				_festivalType = 2;
				_festivalOracle = 2;
				break;
			}
			case 31130:
			case 31135:
			{
				_festivalType = 3;
				_festivalOracle = 2;
				break;
			}
			case 31131:
			case 31136:
			{
				_festivalType = 4;
				_festivalOracle = 2;
				break;
			}
			case 31137:
			case 31142:
			{
				_festivalType = 0;
				_festivalOracle = 1;
				break;
			}
			case 31138:
			case 31143:
			{
				_festivalType = 1;
				_festivalOracle = 1;
				break;
			}
			case 31139:
			case 31144:
			{
				_festivalType = 2;
				_festivalOracle = 1;
				break;
			}
			case 31140:
			case 31145:
			{
				_festivalType = 3;
				_festivalOracle = 1;
				break;
			}
			case 31141:
			case 31146:
			{
				_festivalType = 4;
				_festivalOracle = 1;
				break;
			}
		}
		if(getNpcId() == 31127)
			SevenSignsFestival.getInstance().setDawnChat(this);
		if(getNpcId() == 31137)
			SevenSignsFestival.getInstance().setDuskChat(this);
	}

	@Override
	public void onBypassFeedback(final Player player, final String command)
	{
		if(!Config.ALLOW_SEVEN_SIGNS)
			return;
		if(SevenSigns.getInstance().getPlayerCabal(player) == 0)
			return;
		if(command.startsWith("FestivalDesc"))
		{
			final int val = Integer.parseInt(command.substring(13));
			this.showChatWindow(player, val, null, true);
		}
		else if(command.startsWith("Festival"))
		{
			final Party playerParty = player.getParty();
			final int val2 = Integer.parseInt(command.substring(9, 10));
			switch(val2)
			{
				case 1:
				{
					if(SevenSigns.getInstance().isSealValidationPeriod())
					{
						this.showChatWindow(player, 2, "a", false);
						return;
					}
					if(SevenSignsFestival.getInstance().isFestivalInitialized())
					{
						player.sendMessage(new CustomMessage("l2s.gameserver.model.instances.FestivalGuideInstance.InProgress"));
						return;
					}
					if(playerParty == null)
					{
						this.showChatWindow(player, 2, "b", false);
						return;
					}
					if(!playerParty.isLeader(player))
					{
						this.showChatWindow(player, 2, "c", false);
						return;
					}
					if(playerParty.getMemberCount() < Config.FESTIVAL_MIN_PARTY_SIZE)
					{
						this.showChatWindow(player, 2, "b", false);
						return;
					}
					if(playerParty.getLevel() > SevenSignsFestival.getMaxLevelForFestival(_festivalType))
					{
						this.showChatWindow(player, 2, "d", false);
						return;
					}
					if(player.isFestivalParticipant())
					{
						SevenSignsFestival.getInstance().setParticipants(_festivalOracle, _festivalType, playerParty);
						this.showChatWindow(player, 2, "f", false);
						return;
					}
					this.showChatWindow(player, 1, null, false);
					break;
				}
				case 2:
				{
					final int stoneType = Integer.parseInt(command.substring(11));
					int stonesNeeded = 0;
					switch(stoneType)
					{
						case 6360:
						{
							stonesNeeded = 4500;
							break;
						}
						case 6361:
						{
							stonesNeeded = 2700;
							break;
						}
						case 6362:
						{
							stonesNeeded = 1350;
							break;
						}
					}
					final ItemInstance sealStoneInst = player.getInventory().findItemByItemId(stoneType);
					int stoneCount = 0;
					if(sealStoneInst != null)
						stoneCount = sealStoneInst.getIntegerLimitedCount();
					if(stoneCount < stonesNeeded)
					{
						player.sendMessage(new CustomMessage("l2s.gameserver.model.instances.FestivalGuideInstance.NotEnoughSSType"));
						return;
					}
					player.getInventory().destroyItem(sealStoneInst, stonesNeeded, true);
					final SystemMessage sm = new SystemMessage(301);
					sm.addNumber(Integer.valueOf(stonesNeeded));
					sm.addItemName(Integer.valueOf(stoneType));
					player.sendPacket(sm);
					SevenSignsFestival.getInstance().setParticipants(_festivalOracle, _festivalType, playerParty);
					SevenSignsFestival.getInstance().addAccumulatedBonus(_festivalType, stoneType, stonesNeeded);
					this.showChatWindow(player, 2, "e", false);
					break;
				}
				case 3:
				{
					if(SevenSigns.getInstance().isSealValidationPeriod())
					{
						this.showChatWindow(player, 3, "a", false);
						return;
					}
					if(SevenSignsFestival.getInstance().isFestivalInProgress())
					{
						player.sendMessage(new CustomMessage("l2s.gameserver.model.instances.FestivalGuideInstance.InProgressPoints"));
						return;
					}
					if(playerParty == null)
					{
						this.showChatWindow(player, 3, "b", false);
						return;
					}
					final List<Player> prevParticipants = SevenSignsFestival.getInstance().getPreviousParticipants(_festivalOracle, _festivalType);
					if(prevParticipants == null)
						return;
					if(!prevParticipants.contains(player))
					{
						this.showChatWindow(player, 3, "b", false);
						return;
					}
					if(player.getObjectId() != prevParticipants.get(0).getObjectId())
					{
						this.showChatWindow(player, 3, "b", false);
						return;
					}
					final ItemInstance bloodOfferings = player.getInventory().findItemByItemId(5901);
					if(bloodOfferings == null)
					{
						player.sendMessage(new CustomMessage("l2s.gameserver.model.instances.FestivalGuideInstance.BloodOfferings"));
						return;
					}
					final int offeringCount = bloodOfferings.getIntegerLimitedCount();
					final int offeringScore = offeringCount * 5;
					final boolean isHighestScore = SevenSignsFestival.getInstance().setFinalScore(player, _festivalOracle, _festivalType, offeringScore);
					player.getInventory().destroyItem(bloodOfferings, offeringCount, true);
					player.sendPacket(new SystemMessage(1267).addNumber(Integer.valueOf(offeringScore)));
					if(isHighestScore)
					{
						this.showChatWindow(player, 3, "c", false);
						break;
					}
					this.showChatWindow(player, 3, "d", false);
					break;
				}
				case 4:
				{
					final StringBuffer strBuffer = new StringBuffer("<html><body>Festival Guide:<br>These are the top scores of the week, for the ");
					final StatsSet dawnData = SevenSignsFestival.getInstance().getHighestScoreData(2, _festivalType);
					final StatsSet duskData = SevenSignsFestival.getInstance().getHighestScoreData(1, _festivalType);
					final StatsSet overallData = SevenSignsFestival.getInstance().getOverallHighestScoreData(_festivalType);
					final int dawnScore = dawnData.getInteger("score");
					final int duskScore = duskData.getInteger("score");
					int overallScore = 0;
					if(overallData != null)
						overallScore = overallData.getInteger("score");
					strBuffer.append(SevenSignsFestival.getFestivalName(_festivalType) + " festival.<br>");
					if(dawnScore > 0)
						strBuffer.append("Dawn: " + calculateDate(dawnData.getString("date")) + ". Score " + dawnScore + "<br>" + dawnData.getString("members") + "<br>");
					else
						strBuffer.append("Dawn: No record exists. Score 0<br>");
					if(duskScore > 0)
						strBuffer.append("Dusk: " + calculateDate(duskData.getString("date")) + ". Score " + duskScore + "<br>" + duskData.getString("members") + "<br>");
					else
						strBuffer.append("Dusk: No record exists. Score 0<br>");
					if(overallScore > 0 && overallData != null)
					{
						String cabalStr = "Children of Dusk";
						if(overallData.getInteger("cabal") == 2)
							cabalStr = "Children of Dawn";
						strBuffer.append("Consecutive top scores: " + calculateDate(overallData.getString("date")) + ". Score " + overallScore + "<br>Affilated side: " + cabalStr + "<br>" + overallData.getString("members") + "<br>");
					}
					else
						strBuffer.append("Consecutive top scores: No record exists. Score 0<br>");
					strBuffer.append("<a action=\"bypass -h npc_" + getObjectId() + "_Chat 0\">Go back.</a></body></html>");
					final NpcHtmlMessage html = new NpcHtmlMessage(_objectId);
					html.setHtml(strBuffer.toString());
					player.sendPacket(html);
					break;
				}
				case 8:
				{
					if(playerParty == null)
						return;
					if(!SevenSignsFestival.getInstance().isFestivalInProgress())
						return;
					if(!playerParty.isLeader(player))
					{
						this.showChatWindow(player, 8, "a", false);
						break;
					}
					if(SevenSignsFestival.getInstance().increaseChallenge(_festivalOracle, _festivalType))
					{
						this.showChatWindow(player, 8, "b", false);
						break;
					}
					this.showChatWindow(player, 8, "c", false);
					break;
				}
				case 9:
				{
					if(playerParty == null)
						return;
					if(playerParty.isLeader(player))
					{
						SevenSignsFestival.getInstance().updateParticipants(player, null);
						break;
					}
					if(playerParty.getMemberCount() > Config.FESTIVAL_MIN_PARTY_SIZE)
					{
						SevenSignsFestival.getInstance().updateParticipants(player, playerParty);
						playerParty.removePartyMember(player, true);
						break;
					}
					player.sendMessage("Only partyleader can leave festival, if minmum party member is reached.");
					break;
				}
				case 0:
				{
					if(!SevenSigns.getInstance().isSealValidationPeriod())
					{
						player.sendMessage(new CustomMessage("l2s.gameserver.model.instances.FestivalGuideInstance.Bonuses"));
						return;
					}
					if(SevenSignsFestival.getInstance().distribAccumulatedBonus(player) > 0)
					{
						this.showChatWindow(player, 0, "a", false);
						break;
					}
					this.showChatWindow(player, 0, "b", false);
					break;
				}
				default:
				{
					this.showChatWindow(player, val2, null, false);
					break;
				}
			}
		}
		else
			super.onBypassFeedback(player, command);
	}

	private void showChatWindow(final Player player, final int val, final String suffix, final boolean isDescription)
	{
		player.setLastNpc(this);
		String filename = "seven_signs/festival/";
		filename += isDescription ? "desc_" : "festival_";
		filename += suffix != null ? val + suffix + ".htm" : val + ".htm";
		final NpcHtmlMessage html = new NpcHtmlMessage(_objectId);
		html.setFile(filename);
		html.replace("%festivalType%", SevenSignsFestival.getFestivalName(_festivalType));
		html.replace("%cycleMins%", String.valueOf(SevenSignsFestival.getInstance().getMinsToNextCycle()));
		if(val == 5)
			html.replace("%statsTable%", getStatsTable());
		if(val == 6)
			html.replace("%bonusTable%", getBonusTable());
		player.sendPacket(html);
		player.sendActionFailed();
	}

	@Override
	public void showChatWindow(final Player player, final int val, final Object... replace)
	{
		String filename = "seven_signs/";
		switch(getNpcId())
		{
			case 31127:
			case 31128:
			case 31129:
			case 31130:
			case 31131:
			{
				filename += "festival/dawn_guide.htm";
				break;
			}
			case 31137:
			case 31138:
			case 31139:
			case 31140:
			case 31141:
			{
				filename += "festival/dusk_guide.htm";
				break;
			}
			case 31132:
			case 31133:
			case 31134:
			case 31135:
			case 31136:
			case 31142:
			case 31143:
			case 31144:
			case 31145:
			case 31146:
			{
				filename += "festival/festival_witch.htm";
				break;
			}
			default:
			{
				filename = getHtmlPath(getNpcId(), val, player);
				break;
			}
		}
		player.sendPacket(new NpcHtmlMessage(player, this, filename, val));
	}

	private String getStatsTable()
	{
		final StringBuffer tableHtml = new StringBuffer();
		for(int i = 0; i < 5; ++i)
		{
			final int dawnScore = SevenSignsFestival.getInstance().getHighestScore(2, i);
			final int duskScore = SevenSignsFestival.getInstance().getHighestScore(1, i);
			final String festivalName = SevenSignsFestival.getFestivalName(i);
			String winningCabal = "Children of Dusk";
			if(dawnScore > duskScore)
				winningCabal = "Children of Dawn";
			else if(dawnScore == duskScore)
				winningCabal = "None";
			tableHtml.append("<tr><td width=\"100\" align=\"center\">" + festivalName + "</td><td align=\"center\" width=\"35\">" + duskScore + "</td><td align=\"center\" width=\"35\">" + dawnScore + "</td><td align=\"center\" width=\"130\">" + winningCabal + "</td></tr>");
		}
		return tableHtml.toString();
	}

	private String getBonusTable()
	{
		final StringBuffer tableHtml = new StringBuffer();
		for(int i = 0; i < 5; ++i)
		{
			final int accumScore = SevenSignsFestival.getInstance().getAccumulatedBonus(i);
			final String festivalName = SevenSignsFestival.getFestivalName(i);
			tableHtml.append("<tr><td align=\"center\" width=\"150\">" + festivalName + "</td><td align=\"center\" width=\"150\">" + accumScore + "</td></tr>");
		}
		return tableHtml.toString();
	}

	private String calculateDate(final String milliFromEpoch)
	{
		final long numMillis = Long.valueOf(milliFromEpoch);
		final Calendar calCalc = Calendar.getInstance();
		calCalc.setTimeInMillis(numMillis);
		return calCalc.get(1) + "/" + calCalc.get(2) + "/" + calCalc.get(5);
	}
}
