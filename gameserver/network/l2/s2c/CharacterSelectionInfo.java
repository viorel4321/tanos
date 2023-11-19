package l2s.gameserver.network.l2.s2c;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import l2s.commons.ban.BanBindType;
import l2s.gameserver.instancemanager.GameBanManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbcp.DbUtils;
import l2s.gameserver.Config;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.instancemanager.PlayerManager;
import l2s.gameserver.model.CharSelectInfo;
import l2s.gameserver.tables.CharTemplateTable;
import l2s.gameserver.templates.player.PlayerTemplate;
import l2s.gameserver.utils.AutoBan;

public class CharacterSelectionInfo extends L2GameServerPacket
{
	private static Logger _log;
	private String _loginName;
	private int _sessionId;
	private CharSelectInfo[] _characterPackages;

	public CharacterSelectionInfo(final String loginName, final int sessionId)
	{
		_sessionId = sessionId;
		_loginName = loginName;
		_characterPackages = loadCharacterSelectInfo(loginName);
	}

	public CharSelectInfo[] getCharInfo()
	{
		return _characterPackages;
	}

	@Override
	protected final void writeImpl()
	{
		final int size = _characterPackages != null ? _characterPackages.length : 0;
		writeC(19);
		writeD(size);
		long lastAccess = -1L;
		int lastUsed = -1;
		for(int i = 0; i < size; ++i)
			if(lastAccess < _characterPackages[i].getLastAccess() && _characterPackages[i].getDeleteTimer() <= 0)
			{
				lastAccess = _characterPackages[i].getLastAccess();
				lastUsed = i;
			}
		for(int i = 0; i < size; ++i)
		{
			final CharSelectInfo charInfoPackage = _characterPackages[i];
			final int[] augment = charInfoPackage.getPaperdollVariationsId(7);
			writeS(charInfoPackage.getName());
			writeD(charInfoPackage.getCharId());
			writeS(_loginName);
			writeD(_sessionId);
			writeD(charInfoPackage.getClanId());
			writeD(0);
			writeD(charInfoPackage.getSex());
			writeD(charInfoPackage.getRace());
			writeD(charInfoPackage.getBaseClassId());
			writeD(1);
			writeD(0);
			writeD(0);
			writeD(0);
			writeF(charInfoPackage.getCurrentHp());
			writeF(charInfoPackage.getCurrentMp());
			writeD(charInfoPackage.getSp());
			writeQ(charInfoPackage.getExp());
			writeD(charInfoPackage.getLevel());
			writeD(charInfoPackage.getKarma());
			writeD(0);
			writeD(0);
			writeD(0);
			writeD(0);
			writeD(0);
			writeD(0);
			writeD(0);
			writeD(0);
			writeD(0);
			writeD(charInfoPackage.getPaperdollObjectId(0));
			writeD(charInfoPackage.getPaperdollObjectId(2));
			writeD(charInfoPackage.getPaperdollObjectId(1));
			writeD(charInfoPackage.getPaperdollObjectId(3));
			writeD(charInfoPackage.getPaperdollObjectId(5));
			writeD(charInfoPackage.getPaperdollObjectId(4));
			writeD(charInfoPackage.getPaperdollObjectId(6));
			writeD(charInfoPackage.getPaperdollObjectId(7));
			writeD(charInfoPackage.getPaperdollObjectId(8));
			writeD(charInfoPackage.getPaperdollObjectId(9));
			writeD(charInfoPackage.getPaperdollObjectId(10));
			writeD(charInfoPackage.getPaperdollObjectId(11));
			writeD(charInfoPackage.getPaperdollObjectId(12));
			writeD(charInfoPackage.getPaperdollObjectId(13));
			writeD(charInfoPackage.getPaperdollObjectId(7));
			writeD(charInfoPackage.getPaperdollObjectId(15));
			writeD(charInfoPackage.getPaperdollObjectId(16));
			writeD(charInfoPackage.getPaperdollItemId(0));
			writeD(charInfoPackage.getPaperdollItemId(2));
			writeD(charInfoPackage.getPaperdollItemId(1));
			writeD(charInfoPackage.getPaperdollItemId(3));
			writeD(charInfoPackage.getPaperdollItemId(5));
			writeD(charInfoPackage.getPaperdollItemId(4));
			writeD(charInfoPackage.getPaperdollItemId(6));
			writeD(charInfoPackage.getPaperdollItemId(7));
			writeD(charInfoPackage.getPaperdollItemId(8));
			writeD(charInfoPackage.getPaperdollItemId(9));
			writeD(charInfoPackage.getPaperdollItemId(10));
			writeD(charInfoPackage.getPaperdollItemId(11));
			writeD(charInfoPackage.getPaperdollItemId(12));
			writeD(charInfoPackage.getPaperdollItemId(13));
			writeD(charInfoPackage.getPaperdollItemId(7));
			writeD(charInfoPackage.getPaperdollItemId(15));
			writeD(charInfoPackage.getPaperdollItemId(16));
			writeD(charInfoPackage.getHairStyle());
			writeD(charInfoPackage.getHairColor());
			writeD(charInfoPackage.getFace());
			writeF(charInfoPackage.getMaxHp());
			writeF(charInfoPackage.getMaxMp());
			if(GameBanManager.getInstance().isBanned(BanBindType.PLAYER, charInfoPackage.getObjectId()))
				writeD(-1);
			else
				writeD(charInfoPackage.getAccessLevel() > -100 ? charInfoPackage.getDeleteTimer() : -1);
			writeD(charInfoPackage.getClassId());
			writeD(i == lastUsed ? 1 : 0);
			writeC(Math.min(charInfoPackage.getEnchantEffect(), 127));
			writeH(augment[0]);
			writeH(augment[1]);
		}
	}

	public static CharSelectInfo[] loadCharacterSelectInfo(final String loginName)
	{
		final List<CharSelectInfo> characterList = new ArrayList<CharSelectInfo>();
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM characters AS c LEFT JOIN character_subclasses AS cs ON (c.obj_Id=cs.char_obj_id AND cs.active=1) WHERE account_name=? LIMIT 7");
			statement.setString(1, loginName);
			rset = statement.executeQuery();
			while(rset.next())
			{
				final CharSelectInfo charInfopackage = restoreChar(rset);
				if(charInfopackage != null)
					characterList.add(charInfopackage);
			}
		}
		catch(Exception e)
		{
			CharacterSelectionInfo._log.error("could not restore charinfo:", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return characterList.toArray(new CharSelectInfo[characterList.size()]);
	}

	private static int restoreBaseClassId(final int objId)
	{
		int classId = 0;
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT class_id FROM character_subclasses WHERE char_obj_id=? AND isBase=1");
			statement.setInt(1, objId);
			rset = statement.executeQuery();
			while(rset.next())
				classId = rset.getInt("class_id");
		}
		catch(Exception e)
		{
			CharacterSelectionInfo._log.error("could not restore base class id:", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return classId;
	}

	private static CharSelectInfo restoreChar(final ResultSet chardata)
	{
		CharSelectInfo charInfopackage = null;
		try
		{
			final int objectId = chardata.getInt("obj_Id");
			int baseClassId;
			final int classid = baseClassId = chardata.getInt("class_id");
			final boolean useBaseClass = chardata.getInt("isBase") > 0;
			if(!useBaseClass)
				baseClassId = restoreBaseClassId(objectId);
			final boolean female = chardata.getInt("sex") == 1;
			final PlayerTemplate templ = CharTemplateTable.getInstance().getTemplate(baseClassId, female);
			if(templ == null)
			{
				CharacterSelectionInfo._log.warn("restoreChar fail | templ == null | objectId: " + objectId + " | classid: " + baseClassId + " | female: " + female);
				return null;
			}
			final String name = chardata.getString("char_name");
			charInfopackage = new CharSelectInfo(objectId, name);
			charInfopackage.setLevel(chardata.getInt("level"));
			charInfopackage.setMaxHp(chardata.getInt("maxHp"));
			charInfopackage.setCurrentHp(chardata.getDouble("curHp"));
			charInfopackage.setMaxMp(chardata.getInt("maxMp"));
			charInfopackage.setCurrentMp(chardata.getDouble("curMp"));
			charInfopackage.setFace(chardata.getInt("face"));
			charInfopackage.setHairStyle(chardata.getInt("hairstyle"));
			charInfopackage.setHairColor(chardata.getInt("haircolor"));
			charInfopackage.setSex(female ? 1 : 0);
			charInfopackage.setExp(chardata.getLong("exp"));
			charInfopackage.setSp(chardata.getInt("sp"));
			charInfopackage.setClanId(chardata.getInt("clanid"));
			charInfopackage.setKarma(chardata.getInt("karma"));
			charInfopackage.setRace(templ.race.ordinal());
			charInfopackage.setClassId(classid);
			charInfopackage.setBaseClassId(baseClassId);
			long deletetime = chardata.getLong("deletetime");
			int deletedays = 0;
			if(Config.DELETE_DAYS > 0)
				if(deletetime > 0L)
				{
					deletetime = (int) (System.currentTimeMillis() / 1000L - deletetime);
					deletedays = (int) (deletetime / 3600L / 24L);
					if(deletedays >= Config.DELETE_DAYS)
					{
						PlayerManager.deleteFromClan(objectId, charInfopackage.getClanId());
						PlayerManager.deleteCharByObjId(objectId);
						return null;
					}
					deletetime = Config.DELETE_DAYS * 3600 * 24 - deletetime;
				}
				else
					deletetime = 0L;
			charInfopackage.setDeleteTimer((int) deletetime);
			charInfopackage.setLastAccess(chardata.getLong("lastAccess") * 1000L);
			charInfopackage.setAccessLevel(chardata.getInt("accesslevel"));
			if(charInfopackage.getAccessLevel() < 0 && !AutoBan.isBanned(objectId))
				charInfopackage.setAccessLevel(0);
		}
		catch(Exception e)
		{
			CharacterSelectionInfo._log.error("", e);
		}
		return charInfopackage;
	}

	static
	{
		CharacterSelectionInfo._log = LoggerFactory.getLogger(CharacterSelectionInfo.class);
	}
}
