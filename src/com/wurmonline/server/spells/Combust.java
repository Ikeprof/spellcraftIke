package com.wurmonline.server.spells;

import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.effects.Effect;
import com.wurmonline.server.effects.EffectFactory;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.skills.Skill;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

public class Combust extends ReligiousSpell {
    public Combust(SpellcraftSpell spell){
        super(spell.getName(), ModActions.getNextActionId(), spell.getCastTime(), spell.getCost(), spell.getDifficulty(), spell.getFaith(), spell.getCooldown());
        this.targetItem = true;
        //this.enchantment = spell.getEnchant();
        this.effectdesc = "Light and Fuel with magic.";
        this.description = "Light and Fuel with Magic";

        ActionEntry actionEntry = ActionEntry.createEntry((short) number, name, "enchanting",
                new int[] { 2 ,23, 36,Actions.ACTION_TYPE_IGNORERANGE});
        ModActions.registerAction(actionEntry);
    }

    @Override
    boolean precondition(Skill castSkill, Creature performer, Item target) {
if (target.getTemplateId()== 180 || target.getTemplateId()== 178 || target.getTemplateId()== 1023 || target.getTemplateId()== 1028 || target.getTemplateId()== 889 || target.getTemplateId()== 37 || target.getTemplateId()== 841 || target.getTemplateId()== 842 || target.getTemplateId()== 1178 || target.getTemplateId()== 385){        
        return true;
    } else{
    performer.getCommunicator().sendNormalServerMessage("This spell will not work on that.", (byte) 3);
    return false;
}
    }	
	@Override
    void doEffect(Skill castSkill, double power, Creature performer, Item target) {
        
            if (target.getTemplateId() == 385){
                target.setDamage((float)100);                       
                return;    
            }
            short PowerTemp = (short) Math.abs((short) ((short) power * 400));
            target.setTemperature((short) Math.min(PowerTemp,(short)30000));
            
            Effect effect = EffectFactory.getInstance().createFire(target.getWurmId(), target.getPosX(), target.getPosY(), target.getPosZ(), performer.isOnSurface());
            target.addEffect(effect);
    }
}
