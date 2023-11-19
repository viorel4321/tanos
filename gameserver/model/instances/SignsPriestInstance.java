package l2s.gameserver.model.instances;

import java.util.StringTokenizer;

import l2s.gameserver.model.items.ItemInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.SevenSigns;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.tables.ClanTable;
import l2s.gameserver.tables.ItemTable;
import l2s.gameserver.templates.npc.NpcTemplate;

public class SignsPriestInstance extends NpcInstance
{
	private static Logger _log = LoggerFactory.getLogger(SignsPriestInstance.class);

	public SignsPriestInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
	}

	private void showChatWindow(final Player player, final int val, final String suffix, final boolean isDescription)
	{
		player.setLastNpc(this);
		String filename = "seven_signs/";
		filename += isDescription ? "desc_" + val : "signs_" + val;
		filename += suffix != null ? "_" + suffix + ".htm" : ".htm";
		showChatWindow(player, filename, new Object[0]);
	}

	private boolean getPlayerAllyHasCastle(final Player player)
	{
		Clan playerClan = player.getClan();
		if(playerClan == null)
			return false;

		if(!Config.ALT_GAME_REQUIRE_CLAN_CASTLE)
		{
			int allyId = playerClan.getAllyId();
			if(allyId != 0)
			{
				Clan[] clans = ClanTable.getInstance().getClans();
				for(Clan clan : clans)
				{
					if(clan.getAllyId() == allyId && clan.getHasCastle() > 0)
						return true;
				}
			}
		}
		return playerClan.getHasCastle() > 0;
	}

	@Override
	public void onBypassFeedback(final Player player, final String command)
	{
		if(!Config.ALLOW_SEVEN_SIGNS)
			return;

		if((getNpcId() == 31113 || getNpcId() == 31126) && SevenSigns.getInstance().getPlayerCabal(player) == 0)
			return;

		super.onBypassFeedback(player, command);

		if(command.startsWith("SevenSignsDesc"))
		{
			final int val = Integer.parseInt(command.substring(15));
			showChatWindow(player, val, null, true);
		}
		else if(command.startsWith("SevenSigns"))
		{
			int cabal = 0;
			int stoneType = 0;
			ItemInstance ancientAdena = player.getInventory().getItemByItemId(5575);
			final int ancientAdenaAmount = ancientAdena == null ? 0 : ancientAdena.getIntegerLimitedCount();
			int val2 = Integer.parseInt(command.substring(11, 12).trim());
			if(command.length() > 12)
				val2 = Integer.parseInt(command.substring(11, 13).trim());
			if(command.length() > 13)
				try
				{
					cabal = Integer.parseInt(command.substring(14, 15).trim());
				}
				catch(Exception e2)
				{
					try
					{
						cabal = Integer.parseInt(command.substring(13, 14).trim());
					}
					catch(Exception ex)
					{}
				}
			switch(val2)
			{
				case 2:
				{
					if(!player.getInventory().validateCapacity(1))
					{
						player.sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
						return;
					}
					if(500 > player.getAdena())
					{
						player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
						return;
					}
					player.reduceAdena(500L, true);
					player.getInventory().addItem(ItemTable.getInstance().createItem(5707));
					final SystemMessage sm = new SystemMessage(54);
					sm.addItemName(Short.valueOf((short) 5707));
					player.sendPacket(sm);
					break;
				}
				case 3:
				case 8:
				case 10:
				{
					cabal = SevenSigns.getInstance().getPriestCabal(getNpcId());
					this.showChatWindow(player, val2, SevenSigns.getCabalShortName(cabal), false);
					break;
				}
				case 4:
				{
					final int newSeal = Integer.parseInt(command.substring(15));
					final int oldCabal = SevenSigns.getInstance().getPlayerCabal(player);
					if(oldCabal != 0)
					{
						player.sendMessage(new CustomMessage("l2s.gameserver.model.instances.SignsPriestInstance.AlreadyMember").addString(SevenSigns.getCabalName(cabal)));
						return;
					}
					if(player.getClassId().level() == 0)
					{
						player.sendMessage(new CustomMessage("l2s.gameserver.model.instances.SignsPriestInstance.YouAreNewbie"));
						break;
					}
					if(player.getClassId().level() >= 2 && Config.ALT_GAME_REQUIRE_CASTLE_DAWN)
						if(getPlayerAllyHasCastle(player))
						{
							if(cabal == 1)
							{
								player.sendMessage(new CustomMessage("l2s.gameserver.model.instances.SignsPriestInstance.CastleOwning"));
								return;
							}
						}
						else if(cabal == 2)
						{
							boolean allowJoinDawn = false;
							final ItemInstance temp = player.getInventory().findItemByItemId(6388);
							if(temp != null)
							{
								if(player.getInventory().destroyItemByItemId(6388, 1L, true) == null)
									SignsPriestInstance._log.info("SignsPriestInstance[189]: Item not found!!!");
								final SystemMessage sm = new SystemMessage(302);
								sm.addItemName(Short.valueOf((short) 6388));
								player.sendPacket(sm);
								allowJoinDawn = true;
							}
							else if(player.getAdena() >= 50000)
							{
								player.reduceAdena(50000L, true);
								allowJoinDawn = true;
							}
							if(!allowJoinDawn)
							{
								player.sendMessage(new CustomMessage("l2s.gameserver.model.instances.SignsPriestInstance.CastleOwningCertificate"));
								return;
							}
						}
					SevenSigns.getInstance().setPlayerInfo(player, cabal, newSeal);
					if(cabal == 2)
						player.sendPacket(new SystemMessage(1273));
					else
						player.sendPacket(new SystemMessage(1274));
					switch(newSeal)
					{
						case 1:
						{
							player.sendPacket(new SystemMessage(1275));
							break;
						}
						case 2:
						{
							player.sendPacket(new SystemMessage(1276));
							break;
						}
						case 3:
						{
							player.sendPacket(new SystemMessage(1277));
							break;
						}
					}
					this.showChatWindow(player, 4, SevenSigns.getCabalShortName(cabal), false);
					break;
				}
				case 6:
				{
					stoneType = Integer.parseInt(command.substring(13));
					final ItemInstance redStones = player.getInventory().getItemByItemId(6362);
					final long redStoneCount = redStones == null ? 0L : redStones.getIntegerLimitedCount();
					final ItemInstance greenStones = player.getInventory().getItemByItemId(6361);
					final long greenStoneCount = greenStones == null ? 0L : greenStones.getIntegerLimitedCount();
					final ItemInstance blueStones = player.getInventory().getItemByItemId(6360);
					final long blueStoneCount = blueStones == null ? 0L : blueStones.getIntegerLimitedCount();
					long contribScore = SevenSigns.getInstance().getPlayerContribScore(player);
					boolean stonesFound = false;
					if(contribScore == SevenSigns.MAXIMUM_PLAYER_CONTRIB)
					{
						player.sendPacket(new SystemMessage(1279));
						break;
					}
					long redContribCount = 0L;
					long greenContribCount = 0L;
					long blueContribCount = 0L;
					switch(stoneType)
					{
						case 1:
						{
							blueContribCount = (SevenSigns.MAXIMUM_PLAYER_CONTRIB - contribScore) / 3L;
							if(blueContribCount > blueStoneCount)
							{
								blueContribCount = blueStoneCount;
								break;
							}
							break;
						}
						case 2:
						{
							greenContribCount = (SevenSigns.MAXIMUM_PLAYER_CONTRIB - contribScore) / 5L;
							if(greenContribCount > greenStoneCount)
							{
								greenContribCount = greenStoneCount;
								break;
							}
							break;
						}
						case 3:
						{
							redContribCount = (SevenSigns.MAXIMUM_PLAYER_CONTRIB - contribScore) / 10L;
							if(redContribCount > redStoneCount)
							{
								redContribCount = redStoneCount;
								break;
							}
							break;
						}
						case 4:
						{
							long tempContribScore = contribScore;
							redContribCount = (SevenSigns.MAXIMUM_PLAYER_CONTRIB - tempContribScore) / 10L;
							if(redContribCount > redStoneCount)
								redContribCount = redStoneCount;
							tempContribScore += redContribCount * 10L;
							greenContribCount = (SevenSigns.MAXIMUM_PLAYER_CONTRIB - tempContribScore) / 5L;
							if(greenContribCount > greenStoneCount)
								greenContribCount = greenStoneCount;
							tempContribScore += greenContribCount * 5L;
							blueContribCount = (SevenSigns.MAXIMUM_PLAYER_CONTRIB - tempContribScore) / 3L;
							if(blueContribCount > blueStoneCount)
							{
								blueContribCount = blueStoneCount;
								break;
							}
							break;
						}
					}
					if(redContribCount > 0L)
					{
						final ItemInstance temp2 = player.getInventory().findItemByItemId(6362);
						if(temp2 != null && temp2.getIntegerLimitedCount() >= redContribCount)
						{
							player.getInventory().destroyItemByItemId(6362, (int) redContribCount, true);
							stonesFound = true;
						}
					}
					if(greenContribCount > 0L)
					{
						final ItemInstance temp2 = player.getInventory().findItemByItemId(6361);
						if(temp2 != null && temp2.getIntegerLimitedCount() >= greenContribCount)
						{
							player.getInventory().destroyItemByItemId(6361, (int) greenContribCount, true);
							stonesFound = true;
						}
					}
					if(blueContribCount > 0L)
					{
						final ItemInstance temp2 = player.getInventory().findItemByItemId(6360);
						if(temp2 != null && temp2.getIntegerLimitedCount() >= blueContribCount)
						{
							player.getInventory().destroyItemByItemId(6360, (int) blueContribCount, true);
							stonesFound = true;
						}
					}
					if(!stonesFound)
					{
						player.sendMessage(new CustomMessage("l2s.gameserver.model.instances.SignsPriestInstance.DontHaveAnySSType"));
						return;
					}
					contribScore = SevenSigns.getInstance().addPlayerStoneContrib(player, blueContribCount, greenContribCount, redContribCount);
					final SystemMessage sm = new SystemMessage(1267);
					sm.addNumber(Integer.valueOf((int) contribScore));
					player.sendPacket(sm);
					this.showChatWindow(player, 6, null, false);
					break;
				}
				case 7:
				{
					int ancientAdenaConvert = 0;
					try
					{
						ancientAdenaConvert = Integer.parseInt(command.substring(13).trim());
					}
					catch(NumberFormatException e3)
					{
						player.sendMessage(new CustomMessage("common.IntegerAmount"));
						return;
					}
					catch(StringIndexOutOfBoundsException e4)
					{
						player.sendMessage(new CustomMessage("common.IntegerAmount"));
						return;
					}
					if(ancientAdenaAmount < ancientAdenaConvert || ancientAdenaConvert < 1)
					{
						player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
						return;
					}
					player.addAdena(ancientAdenaConvert);
					player.getInventory().destroyItemByItemId(5575, ancientAdenaConvert, true);
					break;
				}
				case 9:
				{
					final int playerCabal = SevenSigns.getInstance().getPlayerCabal(player);
					final int winningCabal = SevenSigns.getInstance().getCabalHighestScore();
					if(!SevenSigns.getInstance().isSealValidationPeriod() || playerCabal != winningCabal)
						break;
					final int ancientAdenaReward = SevenSigns.getInstance().getAncientAdenaReward(player, true);
					if(ancientAdenaReward < 3)
					{
						this.showChatWindow(player, 9, "b", false);
						return;
					}
					ancientAdena = ItemTable.getInstance().createItem(5575);
					ancientAdena.setCount(ancientAdenaReward);
					player.getInventory().addItem(ancientAdena);
					final SystemMessage sm = new SystemMessage(53);
					sm.addNumber(Integer.valueOf(ancientAdenaReward));
					sm.addItemName(Short.valueOf((short) 5575));
					player.sendPacket(sm);
					this.showChatWindow(player, 9, "a", false);
					break;
				}
				case 11:
				{
					try
					{
						final String portInfo = command.substring(14).trim();
						final StringTokenizer st = new StringTokenizer(portInfo);
						final int x = Integer.parseInt(st.nextToken());
						final int y = Integer.parseInt(st.nextToken());
						final int z = Integer.parseInt(st.nextToken());
						final int ancientAdenaCost = Integer.parseInt(st.nextToken());
						if(ancientAdenaCost > 0)
						{
							final ItemInstance temp3 = player.getInventory().findItemByItemId(5575);
							if(temp3 == null || ancientAdenaCost > temp3.getIntegerLimitedCount())
							{
								player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
								return;
							}
							player.getInventory().destroyItemByItemId(5575, ancientAdenaCost, true);
						}
						player.teleToLocation(x, y, z);
					}
					catch(Exception e)
					{
						SignsPriestInstance._log.warn("SevenSigns: Error occurred while teleporting player: " + e);
					}
					break;
				}
				case 17:
				{
					stoneType = Integer.parseInt(command.substring(14));
					int stoneId = 0;
					int stoneCount = 0;
					int stoneValue = 0;
					String stoneColor = null;
					if(stoneType == 4)
					{
						final ItemInstance BlueStoneInstance = player.getInventory().getItemByItemId(6360);
						final int bcount = BlueStoneInstance != null ? BlueStoneInstance.getIntegerLimitedCount() : 0;
						final ItemInstance GreenStoneInstance = player.getInventory().getItemByItemId(6361);
						final int gcount = GreenStoneInstance != null ? GreenStoneInstance.getIntegerLimitedCount() : 0;
						final ItemInstance RedStoneInstance = player.getInventory().getItemByItemId(6362);
						final int rcount = RedStoneInstance != null ? RedStoneInstance.getIntegerLimitedCount() : 0;
						final long ancientAdenaReward2 = SevenSigns.calcAncientAdenaReward(bcount, gcount, rcount);
						if(ancientAdenaReward2 > 0L)
						{
							if(BlueStoneInstance != null)
							{
								player.getInventory().destroyItem(BlueStoneInstance, bcount, true);
								player.sendPacket(new SystemMessage(301).addNumber(Integer.valueOf(bcount)).addItemName(Integer.valueOf(6360)));
							}
							if(GreenStoneInstance != null)
							{
								player.getInventory().destroyItem(GreenStoneInstance, gcount, true);
								player.sendPacket(new SystemMessage(301).addNumber(Integer.valueOf(gcount)).addItemName(Integer.valueOf(6361)));
							}
							if(RedStoneInstance != null)
							{
								player.getInventory().destroyItem(RedStoneInstance, rcount, true);
								player.sendPacket(new SystemMessage(301).addNumber(Integer.valueOf(rcount)).addItemName(Integer.valueOf(6362)));
							}
							ancientAdena = ItemTable.getInstance().createItem(5575);
							ancientAdena.setCount((int) ancientAdenaReward2);
							player.getInventory().addItem(ancientAdena);
							player.sendPacket(new SystemMessage(53).addNumber(Integer.valueOf((int) ancientAdenaReward2)).addItemName(Short.valueOf((short) 5575)));
							break;
						}
						player.sendMessage(new CustomMessage("l2s.gameserver.model.instances.SignsPriestInstance.DontHaveAnySS"));
						break;
					}
					else
					{
						switch(stoneType)
						{
							case 1:
							{
								stoneColor = "blue";
								stoneId = 6360;
								stoneValue = 3;
								break;
							}
							case 2:
							{
								stoneColor = "green";
								stoneId = 6361;
								stoneValue = 5;
								break;
							}
							case 3:
							{
								stoneColor = "red";
								stoneId = 6362;
								stoneValue = 10;
								break;
							}
						}
						final ItemInstance stoneInstance = player.getInventory().getItemByItemId(stoneId);
						if(stoneInstance != null)
							stoneCount = stoneInstance.getIntegerLimitedCount();
						final String path = "seven_signs/signs_17.htm";
						String content = HtmCache.getInstance().getIfExists(path, player);
						if(content != null)
						{
							content = content.replaceAll("%stoneColor%", stoneColor);
							content = content.replaceAll("%stoneValue%", String.valueOf(stoneValue));
							content = content.replaceAll("%stoneCount%", String.valueOf(stoneCount));
							content = content.replaceAll("%stoneItemId%", String.valueOf(stoneId));
							final NpcHtmlMessage html = new NpcHtmlMessage(_objectId);
							html.setHtml(content);
							player.sendPacket(html);
							break;
						}
						SignsPriestInstance._log.warn("Problem with HTML text seven_signs/signs_17.htm: " + path);
						break;
					}
				}
				case 18:
				{
					final int convertStoneId = Integer.parseInt(command.substring(14, 18));
					int convertCount = 0;
					try
					{
						convertCount = Integer.parseInt(command.substring(19).trim());
					}
					catch(Exception NumberFormatException)
					{
						player.sendMessage(new CustomMessage("common.IntegerAmount"));
						break;
					}
					final ItemInstance convertItem = player.getInventory().getItemByItemId(convertStoneId);
					if(convertItem == null)
					{
						player.sendMessage(new CustomMessage("l2s.gameserver.model.instances.SignsPriestInstance.DontHaveAnySSType"));
						break;
					}
					final int totalCount = convertItem.getIntegerLimitedCount();
					long ancientAdenaReward3 = 0L;
					if(convertCount <= totalCount && convertCount > 0)
					{
						switch(convertStoneId)
						{
							case 6360:
							{
								ancientAdenaReward3 = SevenSigns.calcAncientAdenaReward(convertCount, 0L, 0L);
								break;
							}
							case 6361:
							{
								ancientAdenaReward3 = SevenSigns.calcAncientAdenaReward(0L, convertCount, 0L);
								break;
							}
							case 6362:
							{
								ancientAdenaReward3 = SevenSigns.calcAncientAdenaReward(0L, 0L, convertCount);
								break;
							}
						}
						final ItemInstance temp4 = player.getInventory().findItemByItemId(convertStoneId);
						if(temp4 != null && temp4.getIntegerLimitedCount() >= convertCount)
						{
							player.getInventory().destroyItemByItemId(convertStoneId, convertCount, true);
							ancientAdena = ItemTable.getInstance().createItem(5575);
							ancientAdena.setCount((int) ancientAdenaReward3);
							player.getInventory().addItem(ancientAdena);
							player.sendPacket(new SystemMessage(301).addNumber(Integer.valueOf(convertCount)).addItemName(Integer.valueOf(convertStoneId)));
							player.sendPacket(new SystemMessage(53).addNumber(Integer.valueOf((int) ancientAdenaReward3)).addItemName(Short.valueOf((short) 5575)));
						}
						break;
					}
					player.sendMessage(new CustomMessage("l2s.gameserver.model.instances.SignsPriestInstance.DontHaveSSAmount"));
					break;
				}
				case 19:
				{
					final int chosenSeal = Integer.parseInt(command.substring(16));
					final String fileSuffix = SevenSigns.getSealName(chosenSeal, true) + "_" + SevenSigns.getCabalShortName(cabal);
					this.showChatWindow(player, val2, fileSuffix, false);
					break;
				}
				case 20:
				{
					final StringBuffer contentBuffer = new StringBuffer("<html><body><font color=\"LEVEL\">[Seal Status]</font><br>");
					for(int i = 1; i < 4; ++i)
					{
						final int sealOwner = SevenSigns.getInstance().getSealOwner(i);
						if(sealOwner != 0)
							contentBuffer.append("[" + SevenSigns.getSealName(i, false) + ": " + SevenSigns.getCabalName(sealOwner) + "]<br>");
						else
							contentBuffer.append("[" + SevenSigns.getSealName(i, false) + ": Nothingness]<br>");
					}
					contentBuffer.append("<a action=\"bypass -h npc_" + getObjectId() + "_SevenSigns 3 " + cabal + "\">Go back.</a></body></html>");
					final NpcHtmlMessage html2 = new NpcHtmlMessage(_objectId);
					html2.setHtml(contentBuffer.toString());
					player.sendPacket(html2);
					break;
				}
				default:
				{
					this.showChatWindow(player, val2, null, false);
					break;
				}
			}
		}
	}

	@Override
	public void showChatWindow(final Player player, final int val, final Object... replace)
	{
		player.setLastNpc(this);
		final int npcId = getTemplate().npcId;
		String filename = "seven_signs/";
		final int sealAvariceOwner = SevenSigns.getInstance().getSealOwner(1);
		final int sealGnosisOwner = SevenSigns.getInstance().getSealOwner(2);
		final int playerCabal = SevenSigns.getInstance().getPlayerCabal(player);
		final boolean isSealValidationPeriod = SevenSigns.getInstance().isSealValidationPeriod();
		final int compWinner = SevenSigns.getInstance().getCabalHighestScore();

		switch(npcId)
		{
			case 31078:
			case 31079:
			case 31080:
			case 31081:
			case 31082:
			case 31083:
			case 31084:
			case 31168:
			case 31692:
			case 31694:
			case 31997:
			{
				switch(playerCabal)
				{
					case 2:
					{
						if(!isSealValidationPeriod)
						{
							filename += "dawn_priest_1b.htm";
							break;
						}
						if(compWinner != 2)
						{
							filename += "dawn_priest_2b.htm";
							break;
						}
						if(compWinner != sealGnosisOwner)
						{
							filename += "dawn_priest_2c.htm";
							break;
						}
						filename += "dawn_priest_2a.htm";
						break;
					}
					case 1:
					{
						if(isSealValidationPeriod)
						{
							filename += "dawn_priest_3b.htm";
							break;
						}
						filename += "dawn_priest_3a.htm";
						break;
					}
					default:
					{
						if(!isSealValidationPeriod)
						{
							filename += "dawn_priest_1a.htm";
							break;
						}
						if(compWinner == 2)
						{
							filename += "dawn_priest_4.htm";
							break;
						}
						filename += "dawn_priest_2b.htm";
						break;
					}
				}
				break;
			}
			case 31085:
			case 31086:
			case 31087:
			case 31088:
			case 31089:
			case 31090:
			case 31091:
			case 31169:
			case 31693:
			case 31695:
			case 31998:
			{
				switch(playerCabal)
				{
					case 1:
					{
						if(!isSealValidationPeriod)
						{
							filename += "dusk_priest_1b.htm";
							break;
						}
						if(compWinner != 1)
						{
							filename += "dusk_priest_2b.htm";
							break;
						}
						if(compWinner != sealGnosisOwner)
						{
							filename += "dusk_priest_2c.htm";
							break;
						}
						filename += "dusk_priest_2a.htm";
						break;
					}
					case 2:
					{
						if(isSealValidationPeriod)
						{
							filename += "dusk_priest_3b.htm";
							break;
						}
						filename += "dusk_priest_3a.htm";
						break;
					}
					default:
					{
						if(!isSealValidationPeriod)
						{
							filename += "dusk_priest_1a.htm";
							break;
						}
						if(compWinner == 1)
						{
							filename += "dusk_priest_4.htm";
							break;
						}
						filename += "dusk_priest_2b.htm";
						break;
					}
				}
				break;
			}
			case 31092:
			{
				filename += "blkmrkt_1.htm";
				break;
			}
			case 31113:
			{
				switch(compWinner)
				{
					case 2:
					{
						if(playerCabal != compWinner || playerCabal != sealAvariceOwner)
						{
							player.sendPacket(new SystemMessage(1301));
							return;
						}
						break;
					}
					case 1:
					{
						if(playerCabal != compWinner || playerCabal != sealAvariceOwner)
						{
							player.sendPacket(new SystemMessage(1302));
							return;
						}
						break;
					}
				}
				filename += "mammmerch_1.htm";
				break;
			}
			case 31126:
			{
				switch(compWinner)
				{
					case 2:
					{
						if(playerCabal != compWinner || playerCabal != sealGnosisOwner)
						{
							player.sendPacket(new SystemMessage(1301));
							return;
						}
						break;
					}
					case 1:
					{
						if(playerCabal != compWinner || playerCabal != sealGnosisOwner)
						{
							player.sendPacket(new SystemMessage(1302));
							return;
						}
						break;
					}
				}
				filename += "mammblack_1.htm";
				break;
			}
			default:
			{
				filename = getHtmlPath(npcId, val, player);
				break;
			}
		}
		player.sendPacket(new NpcHtmlMessage(player, this, filename, val));
	}
}
