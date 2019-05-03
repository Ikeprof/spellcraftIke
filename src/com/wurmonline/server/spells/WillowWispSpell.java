package com.wurmonline.server.spells;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemSpellEffects;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import static mod.sin.spellcraft.Ikecode.playerToWispMap;
import static mod.sin.spellcraft.Ikecode.wispToPlayerMap;

import mod.sin.spellcraft.SpellcraftSpellEffects;

import org.gotti.wurmunlimited.mods.creatures.WillowWisp;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

public class WillowWispSpell extends ReligiousSpell {

    public WillowWispSpell(SpellcraftSpell spell){
        super(spell.getName(), ModActions.getNextActionId(), spell.getCastTime(), spell.getCost(), spell.getDifficulty(), spell.getFaith(), spell.getCooldown());
        this.targetCreature = true;
        this.targetTile = true;
        this.offensive = false;
        this.healing = false;
        this.effectdesc = "a friendly curous ball of light";
        this.description = "a friendly curous ball of light!";

        ActionEntry actionEntry = ActionEntry.createEntry((short) number, name, "Willow Wisp",
                new int[] { 2 /* ACTION_TYPE_SPELL */, 36 /* ACTION_TYPE_ALWAYS_USE_ACTIVE_ITEM */ ,Actions.ACTION_TYPE_IGNORERANGE});
        ModActions.registerAction(actionEntry);
    }


	
    @Override
    void doEffect(Skill castSkill, double power, Creature performer, int tilex, int tiley, int layer, int heightOffset) {
        this.createWW(performer, power);
    }

    @Override
    void doEffect(Skill castSkill, double power, Creature performer, Item target) {
        this.createWW(performer, power);
    }

    @Override
    void doEffect(Skill castSkill, double power, Creature performer, Creature target) {
        this.createWW(performer, power);
    }

    void createWW(Creature performer, double power) {
            Creature token2 = null;          
        try {
            token2 = Creature.doNew(WillowWisp.templateId, (float)performer.getPosX(), (float)performer.getPosY(), 0f,performer.getLayer(), "", (byte)ThreadLocalRandom.current().nextInt(0,2));

        } catch (Exception ex) {
            Logger.getLogger(WillowWispSpell.class.getName()).log(Level.SEVERE, null, ex);
        }
            Long existingWisp = playerToWispMap.put(performer.getWurmId(), token2.getWurmId());
            if(existingWisp != null){
                wispToPlayerMap.remove(existingWisp);
                try {
                    Creature wispToKill = Creatures.getInstance().getCreature(existingWisp);
                    wispToKill.die(true, "replacement");
                } catch (NoSuchCreatureException ex) {
                    Logger.getLogger(WillowWispSpell.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            wispToPlayerMap.put(token2.getWurmId(),performer.getWurmId());
            performer.getCommunicator().sendNormalServerMessage("A glowing spirit appears.", (byte)2);
            

    }
}
