package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.network.l2.s2c.NewCharacterSuccess;
import l2s.gameserver.tables.CharTemplateTable;

public class NewCharacter extends L2GameClientPacket
{
	@Override
	public void readImpl()
	{}

	@Override
	public void runImpl()
	{
		final NewCharacterSuccess ct = new NewCharacterSuccess();
		ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.Fighter, false));
		ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.Mage, false));
		ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.ElvenFighter, false));
		ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.ElvenMage, false));
		ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.DarkFighter, false));
		ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.DarkMage, false));
		ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.OrcFighter, false));
		ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.OrcMage, false));
		ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.DwarvenFighter, false));
		this.sendPacket(ct);
	}
}
