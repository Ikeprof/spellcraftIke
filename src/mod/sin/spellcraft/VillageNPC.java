/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mod.sin.spellcraft;

/**
 *
 * @author Ike
 */
import static com.sun.javafx.util.Utils.contains;
import com.wurmonline.server.Message;
import java.util.logging.Logger;
import com.wurmonline.server.Server;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.behaviours.BehaviourDispatcher;
import com.wurmonline.server.behaviours.BehaviourDispatcher.RequestParam;
import com.wurmonline.server.behaviours.Vehicle;
import com.wurmonline.server.behaviours.VehicleBehaviour;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.MountAction;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.creatures.ai.Path;
import com.wurmonline.server.creatures.ai.PathTile;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.VolaTile;

import com.wurmonline.server.zones.Zones;
import java.util.ArrayList;
import java.util.List;
import static mod.sin.spellcraft.Guardian.logger;
import static mod.sin.spellcraft.Ikecode.checkNPCvalid;
import static mod.sin.spellcraft.Ikecode.interactNPC;

public class VillageNPC {
    public static Creature host = null;
    protected static final Logger logger = Logger.getLogger(Guardian.class.getName());

	
	//public static StaticPathFinder pf = new StaticPathfinder();
	// 0 - move, 1 - teleport, 2 - light oven, 3 - recall rune
    public static void ProcessIt() throws NoSuchCreatureException, Exception{
        if (System.currentTimeMillis() > checkNPCvalid + 600000 ) {
            checkNPCvalid = System.currentTimeMillis();
            for (Village v : Villages.getVillages()) { 
                npcManage(v);
            }
        }

        if(System.currentTimeMillis() > interactNPC + 15000){
            interactNPC = System.currentTimeMillis();
            for (Village v : Villages.getVillages()) {
                List<Creature> villNPCs = getNPCS(v);
                for(int i = 0; i <villNPCs.size();i++){
                    if(WurmCalendar.isNight()){
                        if(villNPCs.get(i).getSkills().getSkillOrLearn(1014).getKnowledge()!=11){
                            String[] theName = villNPCs.get(i).getName().split(" ",2);
                            findTheBed(villNPCs.get(i),v);                            
                        } else{
                            //is sleeping (technically invisible)
                        }
                    } else {
                        Creature theNPC = villNPCs.get(i);
                        if(theNPC.getSkills().getSkill(1014).getKnowledge()<10f){
                            theNPC.setSkill(1014, 50f);
                        }
                        if(theNPC.getSkills().getSkill(1014).getKnowledge()==11f){
                            theNPC.setVisibleToPlayers(true);
                            theNPC.setSkill(1014, 40);
                        }
                        if(theNPC.getSkills().getSkill(1014).getKnowledge()==30f){
                            //do random stuff
                        }
                        if(theNPC.getSkills().getSkill(1014).getKnowledge()==40f){
                            findTheChair(villNPCs.get(i),v);
                            theNPC.setSkill(1014, 50f);
                        }
                        if(theNPC.getSkills().getSkill(1014).getKnowledge()==50f){
                            findTheChair(villNPCs.get(i),v);
                        }
                        if(theNPC.getSkills().getSkill(1014).getKnowledge()==51f){
                            theNPC.setSkill(1014, 50f);
                        }
                        
                    
                    }    
                }
            } 
        }
            
            
        
    }
 
public static void npcManage(Village v)throws Exception{
    boolean cookBed,wizardBed,vetBed,blacksmithBed,healerBed,farmerBed = false;
    boolean cookExists,wizardExists,vetExists,blacksmithExists,healerExists,farmerExists = false;
    long cookID,wizardID,vetID,blacksmithID,healerID,farmerID;   
            int startX = v.getStartX();
            int startY = v.getStartY();
            int endX = v.getEndX();
            int endY = v.getEndY();
            int x,y;
            cookBed=wizardBed=vetBed=blacksmithBed=healerBed=farmerBed = false;
            cookExists=wizardExists=vetExists=blacksmithExists=healerExists=farmerExists = false;
            cookID=wizardID=vetID=blacksmithID=healerID=farmerID = 0;
            //check for each NPC
            //put sections below into their own routines
            
            for (x = startX; x <= endX; ++x) {
                for (y = startY; y <= endY; ++y) {
                    VolaTile t = Zones.getTileOrNull(x, y, true);
                    if (t == null){
                        continue;
                    }
                    Item[] theItems = t.getItems();
                    Creature[] crets = t.getCreatures();
                    if (theItems.length > 0){
                        for(int z = 0;z<theItems.length;z++){
                            if(theItems[z].getTemplate().getTemplateId() == 484 || theItems[z].getTemplate().getTemplateId() == 890){
                                String theName = theItems[z].getDescription();
                                if(theName.contains("Cook")) cookBed = true;
                                if(theName.contains("Vet")) vetBed = true;                             
                                if(theName.contains("Blacksmith")) blacksmithBed = true;
                                if(theName.contains("Healer")) healerBed = true;
                                if(theName.contains("Wizard")) wizardBed = true; 
                                if(theName.contains("Farmer")) farmerBed = true; 
                            }
                        }
                    }
                }
            }
            //get the village's NPC's
            List<Creature> villNPCs = getNPCS(v);
            for(int i = 0; i <villNPCs.size();i++){
                String theNPCName = villNPCs.get(i).getName();
                if(theNPCName.contains("Cook of ")) cookExists = true;
                if(theNPCName.contains("Vet of ")) vetExists = true;
                if(theNPCName.contains("Blacksmith of ")) blacksmithExists = true;
                if(theNPCName.contains("Healer of ")) healerExists = true;
                if(theNPCName.contains("Wizard of ")) wizardExists = true;
                if(theNPCName.contains("Farmer of ")) farmerExists = true;
            }
            //check if NPC is missing
            if(cookBed && !cookExists) createNPC("Cook",v);
            if(vetBed && !vetExists) createNPC("Vet",v);
            if(blacksmithBed && !blacksmithExists) createNPC("Blacksmith",v);
            if(healerBed && !healerExists) createNPC("Healer",v);
            if(wizardBed && !wizardExists) createNPC("Wizard",v);
            if(farmerBed && !farmerExists) createNPC("Farmer",v);
            
            //check if NPC should be deleted
            //Creature[] crets = Creatures.getInstance().getCreaturesWithName(name);
            if(!cookBed && cookExists) {
                Creature cretDel = Server.getInstance().getCreature(cookID);
                cretDel.destroy();
            }
            if(!vetBed && vetExists){
                Creature cretDel = Server.getInstance().getCreature(vetID);
                cretDel.destroy();
            }
            if(!blacksmithBed && blacksmithExists){
                Creature cretDel = Server.getInstance().getCreature(blacksmithID);
                cretDel.destroy();
            }
            if(!healerBed && healerExists){
                Creature cretDel = Server.getInstance().getCreature(healerID);
                cretDel.destroy();
            }
            if(!wizardBed && wizardExists){
                Creature cretDel = Server.getInstance().getCreature(wizardID);
                cretDel.destroy();
            }
            if(!farmerBed && farmerExists){
                Creature cretDel = Server.getInstance().getCreature(farmerID);
                cretDel.destroy();
            }    
    
}

public static void createNPC(String theNPC,Village v)throws Exception{
    float tokenX = v.getTokenX() * 4;
    float tokenY = v.getTokenY() * 4;
    Creature villNPC = Creature.doNew(1, tokenX, tokenY, 1f,1,theNPC + " of " + v.getName(), (byte)Server.rand.nextInt(2),(byte)4);
    villNPC.setCitizenVillage(v);
    
}

public static void findTheChair(Creature theNPC,Village v)throws Exception{
    Path path = null;
    int startX = v.getStartX();
    int startY = v.getStartY();
    int endX = v.getEndX();
    int endY = v.getEndY();
    int x,y;
    //check for each NPC
    //put sections below into their own routines
    try{
    String[] theNPCName = theNPC.getName().split(" ",2);
    for (x = startX; x <= endX; ++x) {
        for (y = startY; y <= endY; ++y) {
            VolaTile t = Zones.getTileOrNull(x, y, true);
            if (t == null){
                continue;
            }
            Item[] theItems = t.getItems();
            if (theItems.length > 0){
                for (Item theItem : theItems) {
                    if (theItem.getTemplate().getTemplateId() == 263 || theItem.getTemplate().getTemplateId() == 265|| theItem.getTemplate().getTemplateId() == 913|| theItem.getTemplate().getTemplateId() == 914|| theItem.getTemplate().getTemplateId() == 915|| theItem.getTemplate().getTemplateId() == 923) {
                        String theName = theItem.getDescription();
                        if (theName.contains(theNPCName[0])) {
                            if (!theNPC.isWithinDistanceTo(theItem.getTileX(), theItem.getTileY(), (int) 1f)) {
                                path = theNPC.findPath(theItem.getTileX() , theItem.getTileY() , null);
                                if(!theNPC.isPathing() && path != null && path.getSize()>=1){
                                    theNPC.getStatus().setPath(path);
                                    theNPC.receivedPath = true;
                                    //crets2[z].startPathing(10);
                                    PathTile p = path.getTargetTile();
                                    theNPC.startPathingToTile(p);
                                    theNPC.setPathing(true, false);
                                }
                            } else {
                                theNPC.say("I need to rest");
                                //BehaviourDispatcher.action(this, this.communicator, subject, _target, action);
                                
                                theNPC.embarkOn(theItem.getWurmId(), (byte)2);
                                BehaviourDispatcher.action(theNPC,theNPC.getCommunicator(),theNPC.getWurmId(),theItem.getWurmId(),(short)139);
                                theNPC.setPathing(false,false);
                                theNPC.setSkill(1014, 51f);
                              }
                            }
                            
                                
                        }
                    }
                }
            }
        }
    
    
    
}//after here
    

    catch(Exception e){
        logger.info(String.format("an error"));
            }    
    
    
}

public static void timeToSit(Creature theNPC,Village v)throws Exception{
    
    
    
}

public static void findTheBed(Creature theNPC,Village v)throws Exception{
    Path path = null;
    int startX = v.getStartX();
    int startY = v.getStartY();
    int endX = v.getEndX();
    int endY = v.getEndY();
    int x,y;
    //check for each NPC
    //put sections below into their own routines
    try{
    String[] theNPCName = theNPC.getName().split(" ",2);
    for (x = startX; x <= endX; ++x) {
        for (y = startY; y <= endY; ++y) {
            VolaTile t = Zones.getTileOrNull(x, y, true);
            if (t == null){
                continue;
            }
            Item[] theItems = t.getItems();
            if (theItems.length > 0){
                for (Item theItem : theItems) {
                    if (theItem.getTemplate().getTemplateId() == 484 || theItem.getTemplate().getTemplateId() == 890) {
                        String theName = theItem.getDescription();
                        if (theName.contains(theNPCName[0])) {
                            if (!theNPC.isWithinDistanceTo(theItem.getTileX(), theItem.getTileY(), (int) 1f)) {
                                path = theNPC.findPath(theItem.getTileX() , theItem.getTileY() , null);
                                if(!theNPC.isPathing() && path != null && path.getSize()>=1){
                                    theNPC.getStatus().setPath(path);
                                    theNPC.receivedPath = true;
                                    //crets2[z].startPathing(10);
                                    PathTile p = path.getTargetTile();
                                    theNPC.startPathingToTile(p);
                                    theNPC.setPathing(true, false);
                                }
                            } else {
                                theNPC.say("Goodnight everyone");
                                theNPC.setVisibleToPlayers(false);
                                theNPC.setPathing(false,false);
                                theNPC.setSkill(1014, 11f);
                              }
                            }
                            
                                
                        }
                    }
                }
            }
        }
    
    
    
}//after here
    

    catch(Exception e){
        logger.info(String.format("an error"));
            }
}

public static void timeToSleep(Creature theNPC,Village v)throws Exception{
    
    
    
    
}

public static List<Creature> getNPCS(Village v)throws Exception{
    List<Creature> villNPCs = new ArrayList<>();
    //Creature[] villNPCs = null;
    int startX = v.getStartX();
    int startY = v.getStartY();
    int endX = v.getEndX();
    int endY = v.getEndY();
    int x,y;
            for (x = startX; x <= endX; ++x) {
                for (y = startY; y <= endY; ++y) {
                    VolaTile t = Zones.getTileOrNull(x, y, true);
                    if (t == null){
                        continue;
                    }
                    Creature[] crets = t.getCreatures();
                    if (crets.length > 0) {
                        for (Creature cret : crets) {
                            if (cret.getTemplate().getTemplateId() == 1) {
                                //TheVillageNPC.templateId){
                                String theName = cret.getName();
                                if (theName.contains(" of ")) {
                                    villNPCs.add(cret);
                                }
                            }
                        }
                    }
                }
            }    
    
    return villNPCs;
}


// beds are vehicles.  It may be possible to embark a bed for simulating sleeping.
}
        

