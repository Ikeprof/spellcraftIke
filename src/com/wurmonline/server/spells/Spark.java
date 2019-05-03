package com.wurmonline.server.spells;

import com.wurmonline.server.Players;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.SpellEffects;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.ItemBonus;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.zones.VirtualZone;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

public class Spark extends ReligiousSpell {

    public Spark(SpellcraftSpell spell){
        super(spell.getName(), ModActions.getNextActionId(), spell.getCastTime(), spell.getCost(), spell.getDifficulty(), spell.getFaith(), spell.getCooldown());
        this.targetCreature = true;
        //this.enchantment = SpellcraftSpell.SPARK.getEnchant();
        this.targetTile = true;
        this.offensive = true;
        this.healing = true;
        this.effectdesc = "a low powered electrical spell.";
        this.description = "shocking!";

        ActionEntry actionEntry = ActionEntry.createEntry((short) number, name, "spark",
                new int[] {2,3 /* ACTION_TYPE_SPELL */, 36 /* ACTION_TYPE_ALWAYS_USE_ACTIVE_ITEM */,
                        48 /* ACTION_TYPE_ENEMY_ALWAYS */ ,Actions.ACTION_TYPE_IGNORERANGE});
        ModActions.registerAction(actionEntry);
    }

//    @Override
//    boolean precondition(Skill castSkill, Creature performer, Item target) {
//        if(!Spark.mayBeEnchanted(target)){
//			EnchantMessageUtil.sendCannotBeEnchantedMessage(performer, target);
//        	return false;
//        }
//        SpellEffect negatingEffect = SpellcraftSpellEffects.hasNegatingEffect(target, SpellcraftSpell.ENDURANCE.getEnchant());
//        if(negatingEffect != null){
//            EnchantMessageUtil.sendNegatingEffectMessage(name, performer, target, negatingEffect);
//            return false;
//        }
//        if(!target.isEnchantableJewelry()){
//            performer.getCommunicator().sendNormalServerMessage(name+" can only be cast on jewelery.");
//            return false;
//        }
//        return true;
//    }
	
  @Override
    void doEffect(Skill castSkill, double power, Creature performer, Creature target) {
            int attSkill = 10067;
            int defSkill = 105;
            SpellEffect eff;
            SpellEffects effs;
        if (performer.isOnSurface()) {
            VolaTile t = Zones.getOrCreateTile(target.getTileX(), target.getTileY(), true);
            if (t.getStructure() == null || t.getStructure().isTypeBridge()) {
                try {
                    if ((effs = target.getSpellEffects()) == null) {
                        effs = target.createSpellEffects();
                    }                    
                    //float damage = 5000.0f + 5000.0f * ((float)power/10);
                    //performer.getCommunicator().sendNormalServerMessage("You call down lightning on " + target.getName() + "!", (byte)2);
                    //target.getCommunicator().sendAlertServerMessage(performer.getName() + "  you!", (byte)4);    
                    float theChanneling = (float)performer.getChannelingSkill().getKnowledge();
                    float theDistance = Math.max(Math.abs(performer.getTileX() - target.getTileX()), Math.abs(performer.getTileY() - target.getTileY()));
                    theDistance = Math.max(1,theDistance);
                    float debugChan = theChanneling * 150;
                    float debugPower = (float) ((power * 100) + 4000);
                    float theDamage = (float)((debugPower  + debugChan));
                    theDamage = (float) (theDamage /(theDistance * .5));
                    float armorMod = target.getArmourMod();
                    if (target.isUnique()) {

                        theDamage = (float) (theDamage *(1/(armorMod * 30)));
                    } else {
                        //theDamage = (float) (theDamage *(1/(armorMod * 3)));
                    }
                    //Zones.flashSpell(target.getTileX(), target.getTileY(), theDamage, performer);
                    if(target.isAlive()){
                        target.addWoundOfType(performer, (byte)9, 0, true, 0.0f, true, theDamage,0f,0f,true,true);
                        if ((eff = effs.getSpellEffect((byte)151)) == null) {
                            //public SpellEffect(long aOwner, byte aType, float aPower, int aTimeleft, byte effType, byte influenceType, boolean persist)
                            int theDuration = (int) (theChanneling/10);
                            eff = new SpellEffect(target.getWurmId(),(byte)151,(float) theDamage,(int) theDuration ,(byte) 1,(byte) 1,false);
                            effs.addSpellEffect(eff);
                            performer.getCommunicator().sendNormalServerMessage("You fill your target with electrical energy " + theDamage );
                            for (final VirtualZone vz : t.getWatchers()) {
                                vz.getWatcher().getCommunicator().sendAddEffect(target.getWurmId(), (short)27, target.getPosX(), target.getPosY(), target.getPositionZ(),(byte)(target.isOnSurface()?0:-1),"tree",theDuration,0f);
                            }
                            target.addAttacker(performer);
                            target.setTarget(performer.getWurmId(), true);
                            performer.addAttacker(target);
                            if (target.getCurrentTile() != null) {
                                //target.getCurrentTile().sendAttachCreatureEffect(target, (byte)5, (byte)150, (byte)150, (byte)150, (byte)10);
        //                            target.getCurrentTile().sendAddQuickTileEffect((byte)71, target.getCurrentTile().getLayer()*5);
        //                            target.getCurrentTile().sendAddQuickTileEffect((byte)58, target.getCurrentTile().getLayer()*3);
                                target.isLit = true;


                            }
                        } else {
                        performer.getCommunicator().sendNormalServerMessage("Your target is still charged.", (byte)3);                                
                        }
                    }

                        //TimeUnit.MILLISECONDS.sleep(200);

                    target.addAttacker(performer);
                    performer.addAttacker(target);
                } catch (Exception ex) {
                    Logger.getLogger(Spark.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                performer.getCommunicator().sendNormalServerMessage("Contains a structure blocking your efforts.", (byte)3);
            }
        } else {
            performer.getCommunicator().sendNormalServerMessage("You need to be above ground to call lightning.", (byte)3);
        }
    }

    @Override
    void doNegativeEffect(Skill castSkill, double power, Creature performer, Item target) {
        this.checkDestroyItem(power, performer, target);
    }
}
