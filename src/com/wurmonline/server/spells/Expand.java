package com.wurmonline.server.spells;

import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.skills.Skill;

import org.gotti.wurmunlimited.modsupport.actions.ModActions;

public class Expand extends ItemEnchantment {
    public Expand(SpellcraftSpell spell){
        super(spell.getName(), ModActions.getNextActionId(), spell.getCastTime(), spell.getCost(), spell.getDifficulty(), spell.getFaith(), spell.getCooldown());
        this.targetItem = true;
        this.enchantment = spell.getEnchant();
        this.effectdesc = "has a larger capacity.";
        this.description = "increases capacity";

        ActionEntry actionEntry = ActionEntry.createEntry((short) number, name, "enchanting",
                new int[] { 2 /* ACTION_TYPE_SPELL */, 36 /* ACTION_TYPE_ALWAYS_USE_ACTIVE_ITEM */,
                        48 /* ACTION_TYPE_ENEMY_ALWAYS */ });
        ModActions.registerAction(actionEntry);
    }

	public static boolean isValidContainer(Item target){
        return target.isHollow() /*&& !target.isMailBox()*/ /*&& !target.isSpringFilled()*/;
    }

    @Override
    boolean precondition(Skill castSkill, Creature performer, Item target) {
        if(!Expand.isValidContainer(target)){
			
        	return false;
        }
        SpellEffect negatingEffect = EnchantUtil.hasNegatingEffect(target, this.getEnchantment());
        if (negatingEffect != null) {
            EnchantUtil.sendNegatingEffectMessage(this.getName(), performer, target, negatingEffect);
            return false;
        } else {
            return true;
        }
    }

    @Override
    void doNegativeEffect(Skill castSkill, double power, Creature performer, Item target) {
        // Do nothing to prevent destruction of the item.
    }
}