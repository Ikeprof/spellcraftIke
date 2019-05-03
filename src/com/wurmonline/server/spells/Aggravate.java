package com.wurmonline.server.spells;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.zones.VolaTile;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;
import org.gotti.wurmunlimited.mods.creatures.Doggo;

public class Aggravate extends ReligiousSpell {

    public Aggravate(SpellcraftSpell spell){
        super(spell.getName(), ModActions.getNextActionId(), spell.getCastTime(), spell.getCost(), spell.getDifficulty(), spell.getFaith(), spell.getCooldown());
        //this.targetCreature = true;
        //this.targetItem = true;
        this.targetTile = true;
        this.offensive = false;
        this.healing = false;
        this.effectdesc = "gains the attention of local creatures";
        this.description = "annoying";

        ActionEntry actionEntry = ActionEntry.createEntry((short) number, name, "aggravate",
                new int[] {2,3 /* ACTION_TYPE_SPELL */, 36 /* ACTION_TYPE_ALWAYS_USE_ACTIVE_ITEM */,
                        48 /* ACTION_TYPE_ENEMY_ALWAYS */ ,Actions.ACTION_TYPE_IGNORERANGE});
        ModActions.registerAction(actionEntry);
    }


	
    @Override
    void doEffect(Skill castSkill, double power, Creature performer, int tilex, int tiley, int layer, int heightOffset) {
        performer.getCommunicator().sendNormalServerMessage("You annoy local creatures");
                                
                            int x,y;
                            int sx = Zones.safeTileX(tilex - 4);
                            int sy = Zones.safeTileY(tiley - 4);
                            int ex = Zones.safeTileX(tilex + 4);
                            int ey = Zones.safeTileY(tiley + 4);

                            //int modifyHunger = performer.getStatus().modifyHunger(65000, 0.0f);
                                
//                                performer.findFood();
//                                performer.fleeCounter = (byte) 0;
//                                performer.performer = 1;
//                                performer.poll();
//                                performer.opportunityAttackCounter = 1;
                                for (x = sx; x <= ex; ++x) {
                                    for (y = sy; y <= ey; ++y) {

                                        VolaTile t = Zones.getTileOrNull(x, y, performer.isOnSurface());
                                        if (t == null){
                                            continue;
                                        }
                                        Creature[] crets2 = t.getCreatures();
                                        if (crets2.length > 0) {
                                            for(int z = 0; z<crets2.length; z++){

                                                if( !crets2[z].isHitched() && !crets2[z].getName().contains("guard") && !crets2[z].getName().contains("wisp") && !crets2[z].getName().contains("templar") && crets2[z].getLeader() == null && !crets2[z].isRidden() && !crets2[z].isDominated() && !crets2[z].isPlayer() && crets2[z].getTemplateId() != Doggo.templateId){

                                               
                                                        crets2[z].setOpponent(performer);
                                                        crets2[z].setTarget(performer.getWurmId(), true);


                                                        

                                                    
                                                    //performer.addAttacker(crets2[z]);

                                                }
                                            }
                                        }
                                        
                                        
                                    }
                                }

				logger.info("Player "+performer.getName()+" taunts ");

				
    }


}
