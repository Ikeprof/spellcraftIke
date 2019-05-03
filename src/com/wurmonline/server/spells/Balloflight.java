package com.wurmonline.server.spells;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemSpellEffects;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import mod.sin.spellcraft.SpellcraftSpellEffects;

import org.gotti.wurmunlimited.modsupport.actions.ModActions;

public class Balloflight extends ReligiousSpell {

    public Balloflight(SpellcraftSpell spell){
        super(spell.getName(), ModActions.getNextActionId(), spell.getCastTime(), spell.getCost(), spell.getDifficulty(), spell.getFaith(), spell.getCooldown());
        this.targetCreature = true;
        //this.enchantment = SpellcraftSpell.SPARK.getEnchant();
        this.targetTile = true;
        this.offensive = true;
        this.healing = true;
        this.effectdesc = "places a small source of light";
        this.description = "a small source of light!";

        ActionEntry actionEntry = ActionEntry.createEntry((short) number, name, "Ball of Light",
                new int[] { 2 /* ACTION_TYPE_SPELL */, 36 /* ACTION_TYPE_ALWAYS_USE_ACTIVE_ITEM */ ,Actions.ACTION_TYPE_IGNORERANGE});
        ModActions.registerAction(actionEntry);
    }


	
    @Override
    void doEffect(Skill castSkill, double power, Creature performer, int tilex, int tiley, int layer, int heightOffset) {
        this.createToken(performer, power);
    }

    @Override
    void doEffect(Skill castSkill, double power, Creature performer, Item target) {
        this.createToken(performer, power);
    }

    @Override
    void doEffect(Skill castSkill, double power, Creature performer, Creature target) {
        this.createToken(performer, power);
    }

    void createToken(Creature performer, double power) {
        try {
            Item token2 = ItemFactory.createItem(649, (float)Math.min(50.0, power), performer.getName());
            performer.dropItem(token2);
            //performer.getCurrentTile().addLightSource(token2);
            //performer.getInventory().insertItem(token2);
            performer.getCommunicator().sendNormalServerMessage("It becomes brighter.", (byte)2);
        }
        catch (NoSuchTemplateException token2) {
        }
        catch (FailedException token2) {
            // empty catch block
        }
    }
}
