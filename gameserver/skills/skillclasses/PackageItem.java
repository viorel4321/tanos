package l2s.gameserver.skills.skillclasses;

import l2s.commons.util.Rnd;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.templates.StatsSet;

import java.util.Set;

public class PackageItem extends Skill {
    private double _aqChance = 0.0099921;
    private double _orfenChance = 0.00888888;
    private double _nelfChance = 0.4343553;
    private double _adenaChance = 90;

    public PackageItem(final StatsSet set)
    {
        super(set);
    }

    @Override
    public void onEndCast(final Creature activeChar, final Set<Creature> targets)
    {
        if(Rnd.chance(_aqChance)){
            activeChar.getPlayer().getInventory().addItem(6660,1);
            activeChar.sendPacket(SystemMessage.obtainItems(6660, 1, 0));
        }
        if(Rnd.chance(_orfenChance)){
            activeChar.getPlayer().getInventory().addItem(6661,1);
            activeChar.sendPacket(SystemMessage.obtainItems(6661, 1, 0));
        }
        if(Rnd.chance(_nelfChance)){
            activeChar.getPlayer().getInventory().addItem(5799,1);
            activeChar.sendPacket(SystemMessage.obtainItems(5799, 1, 0));
        }

        if(Rnd.chance(_adenaChance)){
            int count = Rnd.get(1000, 10000);
            activeChar.getPlayer().getInventory().addItem(57, count);
            activeChar.sendPacket(SystemMessage.obtainItems(57, count, 0));
        }
        else{
            activeChar.getPlayer().getInventory().addItem(4037,10);
            activeChar.sendPacket(SystemMessage.obtainItems(4037, 10, 0));
        }


    }


}
