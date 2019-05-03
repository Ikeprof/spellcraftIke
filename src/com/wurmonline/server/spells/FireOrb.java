package com.wurmonline.server.spells;

import com.wurmonline.server.Items;
import com.wurmonline.server.Server;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.behaviours.NoSuchActionException;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.SpellEffects;
import com.wurmonline.server.creatures.ai.PathTile;
import com.wurmonline.server.effects.Effect;
import com.wurmonline.server.effects.EffectFactory;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.ItemBonus;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.zones.Zones;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

public class FireOrb extends ReligiousSpell {

    public FireOrb(SpellcraftSpell spell){
        super(spell.getName(), ModActions.getNextActionId(), spell.getCastTime(), spell.getCost(), spell.getDifficulty(), spell.getFaith(), spell.getCooldown());
        this.targetCreature = true;
        this.offensive = true;
        //this.healing = true;
        //this.enchantment = SpellcraftSpell.FIREORB.getEnchant();
        this.effectdesc = "a low cost fireball DoT.";
        this.description = "damage over time spell";

        ActionEntry actionEntry = ActionEntry.createEntry((short) number, name, "fire orb",
                new int[] {2,3 /* ACTION_TYPE_SPELL */, 36 /* ACTION_TYPE_ALWAYS_USE_ACTIVE_ITEM */,
                        48 /* ACTION_TYPE_ENEMY_ALWAYS */ ,Actions.ACTION_TYPE_IGNORERANGE});
        ModActions.registerAction(actionEntry);
    }


	
    @Override
    void doEffect(Skill castSkill, double power, Creature performer, Creature target) {
            int attSkill = 10067;
            int defSkill = 105;
            SpellEffect eff;
            SpellEffects effs;
            int diameter = (int)Math.max(power, 1.0);
            int tfloorlevel = target.getFloorLevel();
            Set<PathTile> tiles = Zones.explode(target.getTileX(), target.getTileY(), tfloorlevel, true, diameter);
            boolean insideStructure = false;

        try {
            //double testing= (double)target.getStatus().getBattleRatingTypeModifier() ;

            power = Math.abs(Spell.trimPower(performer, performer.getSkills().getSkill(attSkill).skillCheck((double)(1.0f + ItemBonus.getSpellResistBonus(target)) * (target.getSkills().getSkill(defSkill).getKnowledge(0.0) + (double)target.getStatus().getBattleRatingTypeModifier() + (double)(performer.getNumLinks() * 3) ), (double)performer.zoneBonus , false, 0)));
        } catch (NoSuchSkillException ex) {
            Logger.getLogger(FireOrb.class.getName()).log(Level.SEVERE, null, ex);
        }
            int maxLevel = tfloorlevel >= 0 ? 2 : tfloorlevel;
                try {
                    //performer.getCommunicator().sendNormalServerMessage("The Powah!" + power);
                    if ((effs = target.getSpellEffects()) == null) {
                        effs = target.createSpellEffects();
                    }
                    float theChanneling = (float)performer.getChannelingSkill().getKnowledge();
                    byte pos = target.getBody().getRandomWoundPos();
                    float theDistance = Math.max(Math.abs(performer.getTileX() - target.getTileX()), Math.abs(performer.getTileY() - target.getTileY()));
                    theDistance = Math.max(1,theDistance);
                    float debugChan = theChanneling * 130;
                    float debugPower = (float) ((power * 60) + 1000);
                    float theDamage = (float)((debugPower  + debugChan)/1.2);
                    theDamage = (float) (theDamage /(theDistance * 1.5));
                    float armorMod = target.getArmourMod();
                    if (target.isUnique()){                        
                        theDamage = (float) (theDamage *(1/(armorMod * 9)));
                        theChanneling = theChanneling/30;
                    } else {;
                        //theDamage = (float) (theDamage *(1/(armorMod * 5)));
                        
                    }
                    int theDuration = (int) Math.max(30, theChanneling);
                    target.addWoundOfType(performer, (byte)4, pos, true, 1.0f, true, theDamage,0f,0f,true,true);
                    if ((eff = effs.getSpellEffect((byte)150)) == null) {
                        //public SpellEffect(long aOwner, byte aType, float aPower, int aTimeleft, byte effType, byte influenceType, boolean persist)
                        eff = new SpellEffect(target.getWurmId(),(byte)150,(float) theDamage,(int) theDuration ,(byte) 1,(byte) 1,false);
                        effs.addSpellEffect(eff);
                        performer.getCommunicator().sendNormalServerMessage("Your target will burn for a while! " + theDamage );
                        target.addAttacker(performer);
                        target.setTarget(performer.getWurmId(), true);
                        performer.addAttacker(target);
                        if (target.getCurrentTile() != null) {
                            //target.getCurrentTile().sendAttachCreatureEffect(target, (byte)5, (byte)150, (byte)150, (byte)150, (byte)10);
//                            target.getCurrentTile().sendAddQuickTileEffect((byte)71, target.getCurrentTile().getLayer()*5);
//                            target.getCurrentTile().sendAddQuickTileEffect((byte)58, target.getCurrentTile().getLayer()*3);
                            target.isLit = true;
                           

                        }
                    } else if ((double)eff.getPower() > theDamage) {
                        performer.getCommunicator().sendNormalServerMessage("You frown as you fail to improve the Fire Orb.", (byte)3);
                        Server.getInstance().broadCastAction(performer.getNameWithGenus() + " frowns.", performer, 5);
                    } else {
                    if (target != performer) {
                        performer.getCommunicator().sendNormalServerMessage("You succeed in improving the Fire Orb on " + target.getNameWithGenus() + "." + theDamage, (byte)2);
                    }
                    target.getCommunicator().sendAlertServerMessage("The heat around you increases. The pain is excruciating!", (byte)4);
                    eff.setPower((float)theDamage);
                    eff.setTimeleft((int) (int) Math.max(theChanneling * 2,30));
                    target.sendUpdateSpellEffect(eff);
                    target.getCommunicator().sendNormalServerMessage(performer.getNameWithGenus() + " burns you.", (byte)4);
                if (target.getCurrentTile() != null) {
                    target.getCurrentTile().sendAttachCreatureEffect(target, (byte)5, (byte)150, (byte)150, (byte)150, (byte)10);
//                    target.getCurrentTile().sendAddQuickTileEffect((byte)71, target.getCurrentTile().getLayer()*5);
//                    target.getCurrentTile().sendAddQuickTileEffect((byte)58, target.getCurrentTile().getLayer()*3);
                    //insideStructure = true;
                }
                            }
                        } catch (Exception ex) {
            Logger.getLogger(FireOrb.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
                       
                    



    

//    @Override
//    void doNegativeEffect(Skill castSkill, double power, Creature performer, Item target) {
//        this.checkDestroyItem(power, performer, target);
//    }
//}
