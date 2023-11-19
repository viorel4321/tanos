package l2s.gameserver.network.l2;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import l2s.commons.net.nio.impl.IClientFactory;
import l2s.commons.net.nio.impl.IMMOExecutor;
import l2s.commons.net.nio.impl.IPacketHandler;
import l2s.commons.net.nio.impl.MMOConnection;
import l2s.commons.net.nio.impl.ReceivablePacket;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.network.l2.PacketFloodProtector.ActionType;
import l2s.gameserver.network.l2.c2s.*;
import l2s.gameserver.utils.Log;

public final class GamePacketHandler implements IPacketHandler<GameClient>, IClientFactory<GameClient>, IMMOExecutor<GameClient>
{
	@Override
	public ReceivablePacket<GameClient> handlePacket(final ByteBuffer buf, final GameClient client)
	{
		final int id = client._reader != null ? client._reader.read(buf) : buf.get() & 0xFF;
		ReceivablePacket<GameClient> msg = null;
		try
		{
			switch(client.getState())
			{
				case CONNECTED:
				{
					switch(id)
					{
						case 0:
						{
							msg = new ProtocolVersion();
							break;
						}
						case 8:
						{
							msg = new AuthLogin();
							break;
						}
						case 202:
						{
							msg = new GameGuardReply();
							break;
						}
						default:
						{
							client.onUnknownPacket();
							break;
						}
					}
					break;
				}
				case AUTHED:
				{
					if(Config.PACKET_FLOOD_PROTECTOR)
					{
						final PacketFloodProtector.ActionType act = client.checkPacket(id);
						try
						{
							switch(act)
							{
								case log:
								{
									Log.addLog("FP: pkt(0x" + Integer.toHexString(id) + ") logg " + client, "floodprotect");
									break;
								}
								case drop_log:
								{
									Log.addLog("FP: pkt(0x" + Integer.toHexString(id) + ") drop " + client, "floodprotect");
									return new DummyPacket();
								}
								case drop:
								{
									return new DummyPacket();
								}
							}
						}
						catch(Exception e)
						{
							return new DummyPacket();
						}
					}
					switch(id)
					{
						case 9:
						{
							msg = new Logout();
							break;
						}
						case 11:
						{
							msg = new CharacterCreate();
							break;
						}
						case 12:
						{
							msg = new CharacterDelete();
							break;
						}
						case 13:
						{
							msg = new CharacterSelected();
							break;
						}
						case 14:
						{
							msg = new NewCharacter();
							break;
						}
						case 98:
						{
							msg = new CharacterRestore();
							break;
						}
						case 104:
						{
							msg = new RequestPledgeCrest();
							break;
						}
						case 202:
						{
							msg = new GameGuardReply();
							break;
						}
						default:
						{
							client.onUnknownPacket();
							break;
						}
					}
					break;
				}
				case ENTER_GAME:
				{
					if(Config.PACKET_FLOOD_PROTECTOR)
					{
						final PacketFloodProtector.ActionType act = client.checkPacket(id);
						try
						{
							switch(act)
							{
								case log:
								{
									if(!client.getActiveChar().isLogoutStarted())
									{
										Log.addLog("FP: pkt(0x" + Integer.toHexString(id) + ") logg " + client, "floodprotect");
										break;
									}
									break;
								}
								case drop_log:
								{
									if(!client.getActiveChar().isLogoutStarted())
										Log.addLog("FP: pkt(0x" + Integer.toHexString(id) + ") drop " + client, "floodprotect");
									return new DummyPacket();
								}
								case kick_log:
								{
									if(!client.getActiveChar().isLogoutStarted())
									{
										Log.addLog("FP: pkt(0x" + Integer.toHexString(id) + ") kick " + client, "floodprotect");
										client.getActiveChar().kick(true);
									}
									return new DummyPacket();
								}
								case drop:
								{
									return new DummyPacket();
								}
							}
						}
						catch(Exception e)
						{
							return new DummyPacket();
						}
					}
					switch(id)
					{
						case 9:
						{
							msg = new Logout();
							break;
						}
						case 3:
						{
							msg = new EnterWorld();
							break;
						}
						case 168:
						{
							break;
						}
						case 202:
						{
							msg = new GameGuardReply();
							break;
						}
						case 208:
						{
							final int id2 = buf.getShort() & 0xFFFF;
							if(Config.PACKET_FLOOD_PROTECTOR)
							{
								final PacketFloodProtector.ActionType act2 = client.checkPacket(id << 8 | id2);
								try
								{
									switch(act2)
									{
										case log:
										{
											if(!client.getActiveChar().isLogoutStarted())
											{
												Log.addLog("FP: pkt(0x" + Integer.toHexString(id << 8 | id2) + ") logg " + client, "floodprotect");
												break;
											}
											break;
										}
										case drop_log:
										{
											if(!client.getActiveChar().isLogoutStarted())
												Log.addLog("FP: pkt(0x" + Integer.toHexString(id << 8 | id2) + ") drop " + client, "floodprotect");
											return new DummyPacket();
										}
										case kick_log:
										{
											if(!client.getActiveChar().isLogoutStarted())
											{
												Log.addLog("FP: pkt(0x" + Integer.toHexString(id << 8 | id2) + ") kick " + client, "floodprotect");
												client.getActiveChar().kick(true);
											}
											return new DummyPacket();
										}
										case drop:
										{
											return new DummyPacket();
										}
									}
								}
								catch(Exception e2)
								{
									return new DummyPacket();
								}
							}
							switch(id2)
							{
								case 8:
								{
									msg = new RequestManorList();
									break;
								}
								default:
								{
									client.onUnknownPacket();
									break;
								}
							}
							break;
						}
						default:
						{
							client.onUnknownPacket();
							break;
						}
					}
					break;
				}
				case IN_GAME:
				{
					if(Config.PACKET_FLOOD_PROTECTOR)
					{
						final PacketFloodProtector.ActionType act = client.checkPacket(id);
						try
						{
							switch(act)
							{
								case log:
								{
									if(!client.getActiveChar().isLogoutStarted())
									{
										Log.addLog("FP: pkt(0x" + Integer.toHexString(id) + ") logg " + client, "floodprotect");
										break;
									}
									break;
								}
								case drop_log:
								{
									if(!client.getActiveChar().isLogoutStarted())
										Log.addLog("FP: pkt(0x" + Integer.toHexString(id) + ") drop " + client, "floodprotect");
									return new DummyPacket();
								}
								case kick_log:
								{
									if(!client.getActiveChar().isLogoutStarted())
									{
										Log.addLog("FP: pkt(0x" + Integer.toHexString(id) + ") kick " + client, "floodprotect");
										client.getActiveChar().kick(true);
									}
									return new DummyPacket();
								}
								case drop:
								{
									return new DummyPacket();
								}
							}
						}
						catch(Exception e)
						{
							return new DummyPacket();
						}
					}
					switch(id)
					{
						case 1:
						{
							msg = new MoveBackwardToLocation();
							break;
						}
						case 3:
						{
							msg = new EnterWorld();
							break;
						}
						case 4:
						{
							msg = new Action();
							break;
						}
						case 9:
						{
							msg = new Logout();
							break;
						}
						case 10:
						{
							msg = new AttackRequest();
							break;
						}
						case 13:
						{
							break;
						}
						case 15:
						{
							msg = new RequestItemList();
							break;
						}
						case 16:
						{
							break;
						}
						case 17:
						{
							msg = new RequestUnEquipItem();
							break;
						}
						case 18:
						{
							msg = new RequestDropItem();
							break;
						}
						case 20:
						{
							msg = new UseItem();
							break;
						}
						case 21:
						{
							msg = new TradeRequest();
							break;
						}
						case 22:
						{
							msg = new AddTradeItem();
							break;
						}
						case 23:
						{
							msg = new TradeDone();
							break;
						}
						case 26:
						{
							msg = new DummyPacket();
							break;
						}
						case 27:
						{
							msg = new RequestSocialAction();
							break;
						}
						case 28:
						{
							msg = new ChangeMoveType2();
							break;
						}
						case 29:
						{
							msg = new ChangeWaitType2();
							break;
						}
						case 30:
						{
							msg = new RequestSellItem();
							break;
						}
						case 31:
						{
							msg = new RequestBuyItem();
							break;
						}
						case 32:
						{
							msg = new RequestLinkHtml();
							break;
						}
						case 33:
						{
							msg = new RequestBypassToServer();
							break;
						}
						case 34:
						{
							msg = new RequestBBSwrite();
							break;
						}
						case 35:
						{
							msg = new DummyPacket();
							break;
						}
						case 36:
						{
							msg = new RequestJoinPledge();
							break;
						}
						case 37:
						{
							msg = new RequestAnswerJoinPledge();
							break;
						}
						case 38:
						{
							msg = new RequestWithdrawalPledge();
							break;
						}
						case 39:
						{
							msg = new RequestOustPledgeMember();
							break;
						}
						case 40:
						{
							break;
						}
						case 41:
						{
							msg = new RequestJoinParty();
							break;
						}
						case 42:
						{
							msg = new RequestAnswerJoinParty();
							break;
						}
						case 43:
						{
							msg = new RequestWithDrawalParty();
							break;
						}
						case 44:
						{
							msg = new RequestOustPartyMember();
							break;
						}
						case 45:
						{
							break;
						}
						case 46:
						{
							msg = new DummyPacket();
							break;
						}
						case 47:
						{
							msg = new RequestMagicSkillUse();
							break;
						}
						case 48:
						{
							msg = new Appearing();
							break;
						}
						case 49:
						{
							if(Config.ALLOW_WAREHOUSE)
							{
								msg = new SendWareHouseDepositList();
								break;
							}
							break;
						}
						case 50:
						{
							if(Config.ALLOW_WAREHOUSE)
							{
								msg = new SendWareHouseWithDrawList();
								break;
							}
							break;
						}
						case 51:
						{
							msg = new RequestShortCutReg();
							break;
						}
						case 52:
						{
							msg = new DummyPacket();
							break;
						}
						case 53:
						{
							msg = new RequestShortCutDel();
							break;
						}
						case 54:
						{
							msg = new CannotMoveAnymore();
							break;
						}
						case 55:
						{
							msg = new RequestTargetCanceld();
							break;
						}
						case 56:
						{
							msg = new Say2C();
							break;
						}
						case 60:
						{
							msg = new RequestPledgeMemberList();
							break;
						}
						case 62:
						{
							msg = new DummyPacket();
							break;
						}
						case 63:
						{
							msg = new RequestSkillList();
							break;
						}
						case 65:
						{
							break;
						}
						case 66:
						{
							msg = new RequestGetOnVehicle();
							break;
						}
						case 67:
						{
							msg = new RequestGetOffVehicle();
							break;
						}
						case 68:
						{
							msg = new AnswerTradeRequest();
							break;
						}
						case 69:
						{
							msg = new RequestActionUse();
							break;
						}
						case 70:
						{
							msg = new RequestRestart();
							break;
						}
						case 71:
						{
							msg = new RequestSiegeInfo();
							break;
						}
						case 72:
						{
							msg = new ValidatePosition();
							break;
						}
						case 73:
						{
							break;
						}
						case 74:
						{
							msg = new StartRotating();
							break;
						}
						case 75:
						{
							msg = new FinishRotating();
							break;
						}
						case 77:
						{
							msg = new RequestStartPledgeWar();
							break;
						}
						case 78:
						{
							break;
						}
						case 79:
						{
							msg = new RequestStopPledgeWar();
							break;
						}
						case 80:
						{
							break;
						}
						case 81:
						{
							break;
						}
						case 82:
						{
							break;
						}
						case 83:
						{
							msg = new RequestSetPledgeCrest();
							break;
						}
						case 85:
						{
							msg = new RequestGiveNickName();
							break;
						}
						case 87:
						{
							msg = new RequestShowBoard();
							break;
						}
						case 88:
						{
							msg = new RequestEnchantItem();
							break;
						}
						case 89:
						{
							msg = new RequestDestroyItem();
							break;
						}
						case 91:
						{
							msg = new SendBypassBuildCmd();
							break;
						}
						case 92:
						{
							msg = new RequestMoveToLocationInVehicle();
							break;
						}
						case 93:
						{
							msg = new CannotMoveAnymoreInVehicle();
							break;
						}
						case 94:
						{
							msg = new RequestFriendInvite();
							break;
						}
						case 95:
						{
							msg = new RequestAnswerFriendInvite();
							break;
						}
						case 96:
						{
							msg = new RequestFriendList();
							break;
						}
						case 97:
						{
							msg = new RequestFriendDel();
							break;
						}
						case 99:
						{
							msg = new RequestQuestList();
							break;
						}
						case 100:
						{
							msg = new RequestQuestAbort();
							break;
						}
						case 102:
						{
							msg = new RequestPledgeInfo();
							break;
						}
						case 103:
						{
							break;
						}
						case 104:
						{
							msg = new RequestPledgeCrest();
							break;
						}
						case 105:
						{
							break;
						}
						case 106:
						{
							break;
						}
						case 107:
						{
							msg = new RequestAquireSkillInfo();
							break;
						}
						case 108:
						{
							msg = new RequestAquireSkill();
							break;
						}
						case 109:
						{
							msg = new RequestRestartPoint();
							break;
						}
						case 110:
						{
							msg = new RequestGMCommand();
							break;
						}
						case 111:
						{
							msg = new RequestPartyMatchConfig();
							break;
						}
						case 112:
						{
							msg = new RequestPartyMatchList();
							break;
						}
						case 113:
						{
							msg = new RequestPartyMatchDetail();
							break;
						}
						case 114:
						{
							msg = new RequestCrystallizeItem();
							break;
						}
						case 115:
						{
							msg = new RequestPrivateStoreManageSell();
							break;
						}
						case 116:
						{
							msg = new SetPrivateStoreListSell();
							break;
						}
						case 117:
						{
							break;
						}
						case 118:
						{
							msg = new RequestPrivateStoreQuitSell();
							break;
						}
						case 119:
						{
							msg = new SetPrivateStoreMsgSell();
							break;
						}
						case 120:
						{
							break;
						}
						case 121:
						{
							msg = new RequestPrivateStoreBuy();
							break;
						}
						case 122:
						{
							break;
						}
						case 123:
						{
							msg = new RequestTutorialLinkHtml();
							break;
						}
						case 124:
						{
							msg = new RequestTutorialPassCmdToServer();
							break;
						}
						case 125:
						{
							msg = new RequestTutorialQuestionMark();
							break;
						}
						case 126:
						{
							msg = new RequestTutorialClientEvent();
							break;
						}
						case 127:
						{
							msg = new RequestPetition();
							break;
						}
						case 128:
						{
							msg = new RequestPetitionCancel();
							break;
						}
						case 129:
						{
							msg = new RequestGmList();
							break;
						}
						case 130:
						{
							msg = new RequestJoinAlly();
							break;
						}
						case 131:
						{
							msg = new RequestAnswerJoinAlly();
							break;
						}
						case 132:
						{
							msg = new AllyLeave();
							break;
						}
						case 133:
						{
							msg = new AllyDismiss();
							break;
						}
						case 134:
						{
							msg = new RequestDismissAlly();
							break;
						}
						case 135:
						{
							msg = new RequestSetAllyCrest();
							break;
						}
						case 136:
						{
							msg = new RequestAllyCrest();
							break;
						}
						case 137:
						{
							msg = new RequestChangePetName();
							break;
						}
						case 138:
						{
							msg = new RequestPetUseItem();
							break;
						}
						case 139:
						{
							msg = new RequestGiveItemToPet();
							break;
						}
						case 140:
						{
							msg = new RequestGetItemFromPet();
							break;
						}
						case 142:
						{
							msg = new RequestAllyInfo();
							break;
						}
						case 143:
						{
							msg = new RequestPetGetItem();
							break;
						}
						case 144:
						{
							msg = new RequestPrivateStoreManageBuy();
							break;
						}
						case 145:
						{
							msg = new SetPrivateStoreListBuy();
							break;
						}
						case 146:
						{
							break;
						}
						case 147:
						{
							msg = new RequestPrivateStoreQuitBuy();
							break;
						}
						case 148:
						{
							msg = new SetPrivateStoreMsgBuy();
							break;
						}
						case 149:
						{
							break;
						}
						case 150:
						{
							msg = new RequestPrivateStoreSell();
							break;
						}
						case 151:
						{
							break;
						}
						case 152:
						{
							break;
						}
						case 153:
						{
							break;
						}
						case 154:
						{
							break;
						}
						case 155:
						{
							break;
						}
						case 156:
						{
							break;
						}
						case 157:
						{
							break;
						}
						case 158:
						{
							msg = new RequestPackageSendableItemList();
							break;
						}
						case 159:
						{
							msg = new RequestPackageSend();
							break;
						}
						case 160:
						{
							msg = new RequestBlock();
							break;
						}
						case 161:
						{
							break;
						}
						case 162:
						{
							msg = new RequestCastleSiegeAttackerList();
							break;
						}
						case 163:
						{
							msg = new RequestCastleSiegeDefenderList();
							break;
						}
						case 164:
						{
							msg = new RequestJoinSiege();
							break;
						}
						case 165:
						{
							msg = new RequestConfirmSiegeWaitingList();
							break;
						}
						case 166:
						{
							msg = new RequestSetCastleSiegeTime();
							break;
						}
						case 167:
						{
							msg = new RequestMultiSellChoose();
							break;
						}
						case 168:
						{
							break;
						}
						case 170:
						{
							msg = new BypassUserCmd();
							break;
						}
						case 171:
						{
							msg = new SnoopQuit();
							break;
						}
						case 172:
						{
							msg = new RequestRecipeBookOpen();
							break;
						}
						case 173:
						{
							msg = new RequestRecipeBookDestroy();
							break;
						}
						case 174:
						{
							msg = new RequestRecipeItemMakeInfo();
							break;
						}
						case 175:
						{
							msg = new RequestRecipeItemMakeSelf();
							break;
						}
						case 176:
						{
							msg = new RequestRecipeShopManageList();
							break;
						}
						case 177:
						{
							msg = new RequestRecipeShopMessageSet();
							break;
						}
						case 178:
						{
							msg = new RequestRecipeShopListSet();
							break;
						}
						case 179:
						{
							msg = new RequestRecipeShopManageQuit();
							break;
						}
						case 180:
						{
							msg = new RequestRecipeShopManageCancel();
							break;
						}
						case 181:
						{
							msg = new RequestRecipeShopMakeInfo();
							break;
						}
						case 182:
						{
							msg = new RequestRecipeShopMakeItem();
							break;
						}
						case 183:
						{
							msg = new RequestRecipeShopManagePrev();
							break;
						}
						case 184:
						{
							msg = new ObserverReturn();
							break;
						}
						case 185:
						{
							msg = new RequestEvaluate();
							break;
						}
						case 186:
						{
							msg = new RequestHennaList();
							break;
						}
						case 187:
						{
							msg = new RequestHennaItemInfo();
							break;
						}
						case 188:
						{
							msg = new RequestHennaEquip();
							break;
						}
						case 189:
						{
							msg = new RequestHennaUnequipList();
							break;
						}
						case 190:
						{
							msg = new RequestHennaUnequipInfo();
							break;
						}
						case 191:
						{
							msg = new RequestHennaUnequip();
							break;
						}
						case 192:
						{
							msg = new RequestPledgePower();
							break;
						}
						case 193:
						{
							msg = new RequestMakeMacro();
							break;
						}
						case 194:
						{
							msg = new RequestDeleteMacro();
							break;
						}
						case 195:
						{
							msg = new RequestBuyProcure();
							break;
						}
						case 196:
						{
							msg = new RequestBuySeed();
							break;
						}
						case 197:
						{
							msg = new DlgAnswer();
							break;
						}
						case 198:
						{
							msg = new RequestPreviewItem();
							break;
						}
						case 199:
						{
							msg = new RequestSSQStatus();
							break;
						}
						case 202:
						{
							msg = new GameGuardReply();
							break;
						}
						case 204:
						{
							msg = new RequestSendFriendMsg();
							break;
						}
						case 205:
						{
							msg = new RequestShowMiniMap();
							break;
						}
						case 206:
						{
							break;
						}
						case 207:
						{
							msg = new RequestRecordInfo();
							break;
						}
						case 208:
						{
							int id3 = -1;
							if(buf.remaining() < 2)
							{
								Log.addLog("Client: " + client.toString() + " sent a 0xd0 without the second opcode.", "floodprotect");
								break;
							}
							id3 = buf.getShort() & 0xFFFF;
							if(Config.PACKET_FLOOD_PROTECTOR)
							{
								final PacketFloodProtector.ActionType act2 = client.checkPacket(id << 8 | id3);
								try
								{
									switch(act2)
									{
										case log:
										{
											Log.addLog("FP: pkt(0x" + Integer.toHexString(id << 8 | id3) + ") logg " + client, "floodprotect");
											break;
										}
										case drop_log:
										{
											Log.addLog("FP: pkt(0x" + Integer.toHexString(id << 8 | id3) + ") drop " + client, "floodprotect");
											return new DummyPacket();
										}
										case drop:
										{
											return new DummyPacket();
										}
									}
								}
								catch(Exception e2)
								{
									return new DummyPacket();
								}
							}
							switch(id3)
							{
								case 1:
								{
									msg = new RequestOustFromPartyRoom();
									break;
								}
								case 2:
								{
									msg = new RequestDismissPartyRoom();
									break;
								}
								case 3:
								{
									msg = new RequestWithdrawPartyRoom();
									break;
								}
								case 4:
								{
									msg = new RequestChangePartyLeader();
									break;
								}
								case 5:
								{
									msg = new RequestAutoSoulShot();
									break;
								}
								case 6:
								{
									msg = new RequestExEnchantSkillInfo();
									break;
								}
								case 7:
								{
									msg = new RequestExEnchantSkill();
									break;
								}
								case 8:
								{
									msg = new RequestManorList();
									break;
								}
								case 9:
								{
									msg = new RequestProcureCropList();
									break;
								}
								case 10:
								{
									msg = new RequestSetSeed();
									break;
								}
								case 11:
								{
									msg = new RequestSetCrop();
									break;
								}
								case 12:
								{
									msg = new RequestWriteHeroWords();
									break;
								}
								case 13:
								{
									msg = new RequestExMPCCAskJoin();
									break;
								}
								case 14:
								{
									msg = new RequestExMPCCAcceptJoin();
									break;
								}
								case 15:
								{
									msg = new RequestExMPCCExit();
									break;
								}
								case 16:
								{
									msg = new RequestPledgeCrestLarge();
									break;
								}
								case 17:
								{
									msg = new RequestSetPledgeCrestLarge();
									break;
								}
								case 18:
								{
									msg = new RequestOlympiadObserverEnd();
									break;
								}
								case 19:
								{
									msg = new RequestOlympiadMatchList();
									break;
								}
								case 20:
								{
									msg = new RequestAskJoinPartyRoom();
									break;
								}
								case 21:
								{
									msg = new AnswerJoinPartyRoom();
									break;
								}
								case 22:
								{
									msg = new RequestListPartyMatchingWaitingRoom();
									break;
								}
								case 23:
								{
									msg = new RequestExitPartyMatchingWaitingRoom();
									break;
								}
								case 24:
								{
									msg = new RequestGetBossRecord();
									break;
								}
								case 25:
								{
									msg = new RequestPledgeSetAcademyMaster();
									break;
								}
								case 26:
								{
									msg = new RequestPledgePowerGradeList();
									break;
								}
								case 27:
								{
									msg = new RequestPledgeMemberPowerInfo();
									break;
								}
								case 28:
								{
									msg = new RequestPledgeSetMemberPowerGrade();
									break;
								}
								case 29:
								{
									msg = new RequestPledgeMemberInfo();
									break;
								}
								case 30:
								{
									msg = new RequestPledgeWarList();
									break;
								}
								case 31:
								{
									msg = new RequestExFishRanking();
									break;
								}
								case 32:
								{
									msg = new RequestPCCafeCouponUse();
									break;
								}
								case 34:
								{
									msg = new RequestCursedWeaponList();
									break;
								}
								case 35:
								{
									msg = new RequestCursedWeaponLocation();
									break;
								}
								case 36:
								{
									msg = new RequestPledgeReorganizeMember();
									break;
								}
								case 38:
								{
									msg = new RequestExMPCCShowPartyMembersInfo();
									break;
								}
								case 39:
								{
									msg = new RequestDuelStart();
									break;
								}
								case 40:
								{
									msg = new RequestDuelAnswerStart();
									break;
								}
								case 41:
								{
									msg = new RequestConfirmTargetItem();
									break;
								}
								case 42:
								{
									msg = new RequestConfirmRefinerItem();
									break;
								}
								case 43:
								{
									msg = new RequestConfirmGemStone();
									break;
								}
								case 44:
								{
									msg = new RequestRefine();
									break;
								}
								case 45:
								{
									msg = new RequestConfirmCancelItem();
									break;
								}
								case 46:
								{
									msg = new RequestRefineCancel();
									break;
								}
								case 47:
								{
									msg = new RequestExMagicSkillUseGround();
									break;
								}
								case 48:
								{
									msg = new RequestDuelSurrender();
									break;
								}
								default:
								{
									client.onUnknownPacket();
									break;
								}
							}
							break;
						}
						default:
						{
							client.onUnknownPacket();
							break;
						}
					}
					break;
				}
			}
		}
		catch(BufferUnderflowException e3)
		{
			client.onPacketReadFail();
		}
		if(msg == null)
			msg = new DummyPacket();
		return msg;
	}

	@Override
	public GameClient create(final MMOConnection<GameClient> con)
	{
		return new GameClient(con);
	}

	@Override
	public void execute(final Runnable r)
	{
		ThreadPoolManager.getInstance().execute(r);
	}
}
