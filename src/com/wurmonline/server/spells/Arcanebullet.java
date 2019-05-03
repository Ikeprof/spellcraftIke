package com.wurmonline.server.spells;

import com.wurmonline.server.WurmId;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.ItemBonus;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.zones.VolaTile;
import java.util.logging.Level;

import org.gotti.wurmunlimited.modsupport.actions.ModActions;

public class Arcanebullet extends DamageSpell {

    public Arcanebullet(SpellcraftSpell spell){
        super(spell.getName(), ModActions.getNextActionId(), spell.getCastTime(), spell.getCost(), spell.getDifficulty(), spell.getFaith(), spell.getCooldown());
        this.targetCreature = true;
        this.targetTile = true;
        this.offensive = true;
        
        //this.healing = true;
        this.effectdesc = "a low powered damage/stun spell";
        this.description = "Arcane spell with stun component";
        ActionEntry actionEntry = ActionEntry.createEntry((short) number, name, "Arcane Bullet",
                new int[] { 2,3 /* ACTION_TYPE_SPELL */,36 /* ACTION_TYPE_ALWAYS_USE_ACTIVE_ITEM */,
                48 /* ACTION_TYPE_ENEMY_ALWAYS */ ,Actions.ACTION_TYPE_IGNORERANGE});
        ModActions.registerAction(actionEntry);
    }

    @Override
    boolean precondition(Skill castSkill, Creature performer, Item target) {
        return true;
    }	
    @Override
    void doEffect(Skill castSkill, double power, Creature performer, Creature target) {
            int attSkill = 10067;
            int defSkill = 105;
            try {
                power = Math.abs(Spell.trimPower(performer, performer.getSkills().getSkill(attSkill).skillCheck((double)(1.0f + ItemBonus.getSpellResistBonus(target)) * (target.getSkills().getSkill(defSkill).getKnowledge(0.0) + (double)target.getStatus().getBattleRatingTypeModifier() + (double)(performer.getNumLinks() * 3) ), (double)performer.zoneBonus , false, 0)));                
                VolaTile t = performer.getCurrentTile();
                float theChanneling = (float)performer.getChannelingSkill().getKnowledge();
                        float theDamage = 0;
                        long shardId = WurmId.getNextTempItemId();
                        if (t != null) {                            
                            t.sendProjectile(shardId, (byte)4, "model.resource.brick", "Marble Brick", (byte)0, performer.getPosX(), performer.getPosY(), performer.getPositionZ() + performer.getAltOffZ(), performer.getStatus().getRotation(), (byte)performer.getLayer(), (int)target.getPosX(), (int)target.getPosY(), target.getPositionZ() + target.getAltOffZ(), performer.getWurmId(), target.getWurmId(), 0.0f, 0.0f);
                        }    
                        byte pos = 34;
                        float theDistance = Math.max(Math.abs(performer.getTileX() - target.getTileX()), Math.abs(performer.getTileY() - target.getTileY()));
                        theDamage = (float)((70f * power * theChanneling/Math.max((theDistance)*3,5)));
                        float armorMod = target.getArmourMod();
                        if (target.isUnique()) { 
                            theDamage = (float) (theDamage *(1/(armorMod * 25)));
                        } else {
                            theDamage = (float) (theDamage * (1/armorMod) + ((60000/theChanneling)));
                        }
                        if(target.isAlive()){
                            target.addWoundOfType(performer, (byte)8, pos, true, 1.0f, true, theDamage,0f,0f,true,true);                            
                            if (!target.isUnique()) target.getStatus().setStunned(3);
                    }
                    target.addAttacker(performer);
                    performer.addAttacker(target);                
            }
            catch (Exception exe) {
                logger.log(Level.WARNING, exe.getMessage(), exe);
            }
        
    }

    @Override
    void doNegativeEffect(Skill castSkill, double power, Creature performer, Item target) {
        this.checkDestroyItem(power, performer, target);
    }
}
