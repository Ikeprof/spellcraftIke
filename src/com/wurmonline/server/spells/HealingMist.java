package com.wurmonline.server.spells;

import com.wurmonline.mesh.FoliageAge;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.mesh.TreeData;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.zones.Zones;
import java.awt.geom.Ellipse2D;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.zones.VirtualZone;
import com.wurmonline.server.zones.VolaTile;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

public class HealingMist extends ReligiousSpell {

    public HealingMist(SpellcraftSpell spell){
        super(spell.getName(), ModActions.getNextActionId(), spell.getCastTime(), spell.getCost(), spell.getDifficulty(), spell.getFaith(), spell.getCooldown());
        this.targetCreature = false;
        this.targetTile = true;
        this.offensive = false;
        this.healing = false;
        this.effectdesc = "Soothing, Healing Mist";
        this.description = "heals and sooths our animal friends";

        ActionEntry actionEntry = ActionEntry.createEntry((short) number, name, "Healing Mist",
                new int[] {2,3 /* ACTION_TYPE_SPELL */, 36 /* ACTION_TYPE_ALWAYS_USE_ACTIVE_ITEM */,
                        48 /* ACTION_TYPE_ENEMY_ALWAYS */ ,Actions.ACTION_TYPE_IGNORERANGE});
        ModActions.registerAction(actionEntry);
    }


	
    @Override
    void doEffect(final Skill castSkill, final double power, final Creature performer, final int tilex, final int tiley, final int layer, final int heightOffset) {
        performer.getCommunicator().sendNormalServerMessage("You tend to the animals here.", (byte)2);
        final int sx = Zones.safeTileX(tilex - 5 - performer.getNumLinks());
        final int sy = Zones.safeTileY(tiley - 5 - performer.getNumLinks());
        final int ex = Zones.safeTileX(tilex + 5 + performer.getNumLinks());
        final int ey = Zones.safeTileY(tiley + 5 + performer.getNumLinks());
        for (int x = sx; x < ex; ++x) {
            for (int y = sy; y < ey; ++y) {
                final VolaTile t = Zones.getTileOrNull(x, y, performer.isOnSurface());
                if (t != null) {
                    for (final VirtualZone vz : t.getWatchers()) {
                        vz.getWatcher().getCommunicator().sendAddEffect(performer.getWurmId(), (short)27, performer.getPosX(), performer.getPosY(), performer.getPositionZ(),(byte)(performer.isOnSurface()?0:-1),"fog",10f,0f);
                    }
                    final Creature[] creatures;
                    final Creature[] crets = creatures = t.getCreatures();
                    for (final Creature lCret : creatures) {
                        if (!lCret.isPlayer()) {
                            lCret.setMilked(false);
                            lCret.setLastGroomed(System.currentTimeMillis());
                            lCret.getBody().healFully();
                            performer.getCommunicator().sendNormalServerMessage(lCret.getNameWithGenus() + " now shines with health.");
                            //SpellResist.addSpellResistance(lCret, this.getNumber(), power);
                        }
                    }
                }
            }
        }
    }

    @Override
    void doNegativeEffect(Skill castSkill, double power, Creature performer, Item target) {
        this.checkDestroyItem(power, performer, target);
    }
}
