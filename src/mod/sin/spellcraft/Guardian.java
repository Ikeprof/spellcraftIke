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
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import java.util.logging.Logger;
import com.wurmonline.server.Server;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.bodys.Wounds;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.creatures.SpellEffects;
import com.wurmonline.server.creatures.ai.NoPathException;
import com.wurmonline.server.creatures.ai.Path;
import com.wurmonline.server.creatures.ai.PathTile;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.spells.SpellEffect;

import com.wurmonline.server.zones.Zones;
import static mod.sin.spellcraft.Ikecode.PetPathfindTickTime;

public class Guardian {
    public static Creature host = null;
    protected static final Logger logger = Logger.getLogger(Guardian.class.getName());
    private static final double R2D = 57.29577951308232D;
    private static final double D2R = 0.01745329238474369D;
	
	//public static StaticPathFinder pf = new StaticPathfinder();
	// 0 - move, 1 - teleport, 2 - light oven, 3 - recall rune
	public static void ProcessIt() throws NoSuchCreatureException, Exception{
            Path path = null;
            Player[] playarr;
            playarr = Players.getInstance().getPlayers();

            for (int x2 = 0; x2 < playarr.length; ++x2) {
                    if((playarr[x2].isAlive() && (playarr[x2].getPet()!=null))) {
                        Player thePlayer = playarr[x2];
                        Creature thePet = thePlayer.getPet();
                        //check to see if pet is hitched.
                        if(thePet.isHitched()){
                            thePet.setDominator(-10L);
                            thePlayer.setPet(-10L);
                            thePlayer.getCommunicator().sendNormalServerMessage("Your companion has been hitched.  You will need to make it your pet again upon unhitching", (byte)2);                                                
                        }
                        
                        if (thePet.getVisionArea()!=null){
                            //get the last command of the pet
                            double lastCommand = thePet.getSkills().getSkillOrLearn(1013).getKnowledge();
                            if(lastCommand < 10){
                                Guardian.initializeThePet(thePlayer);
                            }
                            if(thePet.getSkills().getSkillOrLearn(1014).getKnowledge()< 20){
                                thePet.setSkill(1014, 10f);
                            }
                            double mPain = (int) thePet.getSkills().getSkillOrLearn(1014).getKnowledge();

                            //follow and leave combat
                            if(lastCommand == 10){               
                                thePet.setTarget(-10L,true);
                                thePet.stopFighting();
                                if (!thePet.isWithinDistanceTo(thePlayer.getTileX(),thePlayer.getTileY(), (int) 0f)){
                                    Guardian.returnToPlayer(thePlayer, thePet);
                                }

                            //defend player
                            } else if(lastCommand == 20){               
                                if((thePlayer.isFighting() == true ) || thePet.isFighting()){
                                    Creature theTarget = null;
                                    if(thePlayer.opponent !=null){
                                        theTarget = thePlayer.opponent;
                                    } else {
                                        theTarget = thePet.opponent;
                                    }
                                    try{
                                        if(theTarget.isAlive() && theTarget != thePet && theTarget != thePlayer){
                                            //(thePet.isWithinDistanceTo(theTarget, 3f)){
                                            //if (Creature.rangeTo(this, tg) < Actions.actionEntrys[114].getRange()) {
                                            if (thePet.rangeTo(thePet,theTarget)< Actions.actionEntrys[114].getRange()){
                                                theTarget.addAttacker(thePlayer);
                                                thePet.setTarget(theTarget.getWurmId(), true);
                                                thePet.attackTarget();
                                                thePet.setSkill(1013, 21f);
                                                thePlayer.getCommunicator().sendNormalServerMessage("Your companion comes to your aid", (byte)2);                                                
                                            } else {
                                                Guardian.moveToTarget(theTarget, thePet);
//                                                path = thePet.findPath(thePet.getTarget().getTileX(), thePet.getTarget().getTileY(),null);                                
//                                                if(path != null && !thePet.isPathing() && path.getSize()>=1){
//                                                    thePet.getStatus().setPath(path);                                                                    
//                                                    thePet.receivedPath = true;;
//                                                    PathTile p = path.getTargetTile();
//                                                    thePet.startPathingToTile(p);
//                                                    thePet.setPathing(true, false);
//                                                    thePet.setLoyalty((float) (thePet.getLoyalty() + 5));
 //                                               }
                                            }
                                        }
                                    }
                                    catch(Exception nsp){
                                    thePlayer.getCommunicator().sendNormalServerMessage("Your companion can't find a path, switching to follow mode", (byte)2);
                                    thePet.setSkill(1013, 10f);
                                    }
                                } else{
                                    returnToPlayer(thePlayer,thePet);
                                }
    
                            //actively defending the player
                            } else if(lastCommand == 21){
                                if (thePet.target==-10L){
                                    Guardian.addTheSkills(thePet);
                                    thePet.setLoyalty((float) (thePet.getLoyalty() + 5));
                                    thePet.setSkill(1013, 20f);
                                    Guardian.returnToPlayer(thePlayer, thePet);
                                } else {
                                    
                                    thePet.attackTarget();
                                }
       
                            //attack a target       
                            } else if(lastCommand == 30){
                                    
                                Creature theTarget = null;
                                if(thePlayer.getTarget()!=null){
                                        theTarget = thePlayer.getTarget();
                                } else {
                                        theTarget = thePet.opponent;
                                }
                                if (theTarget!=null){
                                    try{                                        
                                        if(theTarget.isAlive() && theTarget != thePet && theTarget != thePlayer){
                                            if (thePet.rangeTo(thePet,theTarget)< Actions.actionEntrys[114].getRange()){
                                                thePet.setTarget(theTarget.getWurmId(), true);
                                                thePet.attackTarget();
                                                thePlayer.getCommunicator().sendNormalServerMessage("Your companion races to attack your target", (byte)2);
                                                thePet.setLoyalty((float) (thePet.getLoyalty() + 5)); 
                                                thePet.setSkill(1013, 31f);
                                            } else {
                                               Guardian.moveToTarget(theTarget, thePet); 
                                            }
                                        }

                                    }
                                    catch(NoPathException np){
                                        thePlayer.getCommunicator().sendNormalServerMessage("Your companion cannot find a path, switching to Follow Mode", (byte)2);
                                        thePet.setSkill(1013, 10f);
                                    }
                                }
                            //actively attacking a target
                            } else if(lastCommand == 31){
                                if(thePet.target==-10L && thePet.opponent==null || !thePet.getTarget().isAlive()) {
                                    Guardian.addTheSkills(thePet);
                                thePet.setSkill(1013, 30f);
                                }
                        
                            // stay command
                            } else if(lastCommand == 40){               
                                thePet.setPathing(false, false);  
                                
                            // pathfinding commmand
                            } else if(lastCommand == 60) {
                                if(System.currentTimeMillis()> PetPathfindTickTime + 1000){
                                    PetPathfindTickTime = System.currentTimeMillis();
                                    try{
                                        double tileX = thePet.getSkills().getSkillOrLearn(1008).getKnowledge();
                                        double tileY = thePet.getSkills().getSkillOrLearn(1009).getKnowledge();
                                        int gotileX = (int) (tileX * 100);
                                        int gotileY = (int) (tileY * 100);
                                        float petX = thePet.getPosX()/4;
                                        float petY = thePet.getPosY()/4;
                                        float diffX = petX - gotileX;
                                        float diffY = petY - gotileY;
                                        int diff = (int) Math.max(Math.abs(diffX), Math.abs(diffY));
                                        if(diff > 2){
                                            path = thePet.findPath(gotileX,gotileY,null);
                                            if(!thePet.isPathing() && path != null && path.getSize()>=1){
                                                thePet.getStatus().setPath(path);                                                                    
                                                thePet.receivedPath = true;
                                                //crets2[z].startPathing(10);
                                                PathTile p = path.getTargetTile();
                                                thePet.startPathingToTile(p);
                                                thePet.setPathing(true, false); 
                                            }
                                        } else {
                                            thePlayer.getCommunicator().sendNormalServerMessage("The scent is too strong, you are too close to the objective");
                                            thePet.setSkill(1013, 10f);
                                        }
                                    } catch (Exception nsp) {
                                                thePet.setPathing(false,false);
                                                //logger.info(String.format("%s no Path to treasuremap", thePet.getName()));
                                    }                           
                                }
                            }
                            //check if Masters Pain is on, if so transfer wounds to player
                            if (mPain == 20f){
                                Wounds theWounds = thePet.getBody().getWounds();
                                if(theWounds!=null){
                                    Wound[] wounds;
                                    if (!thePlayer.isDead()){
                                        if(!thePet.isVisible()){
                                            thePet.setVisible(true);
                                        }
                                        for (Wound lWound : wounds = thePet.getBody().getWounds().getWounds()) {
                                            //thePlayer.getCommunicator().sendNormalServerMessage("Healing " + lWound.getDescription());
                                            Creature theInflictor = lWound.getCreature();
                                            float theSeverity= lWound.getSeverity();
                                            byte theType = lWound.getType();
                                            // 1.9 
                                            thePlayer.addWoundOfType(null, theType,0, true, thePlayer.getArmourMod(), true, theSeverity,0f,0f,false,true);                                      
                                            
                                            lWound.heal();
                                        }//here
                                    } else {
                                        thePet.setVisible(false);
                                        thePet.setSkill(1013, 10f);
                                    }                                
                                }
                            }    
                        }
            //replicate buffs from player to pet
            if(Server.rand.nextInt(250) == 0){            
//            Player thePlayer = playarr[x2];
//            Creature thePet = thePlayer.getPet();
            SpellEffect effPet;
            SpellEffect effPlayer;
            SpellEffects effsPlayer;
            SpellEffects effsPet;
            //spellEffects = new HashMap<Byte, SpellEffect>();
        //if (WurmId.getType(_creatureId) == 0) {
            //SpellEffect[] speffs = SpellEffect.loadEffectsForPlayer(thePlayer.getWurmId());
            effsPlayer = thePlayer.getSpellEffects();
            if (thePet!=null){            
            effsPet = thePet.getSpellEffects();
            if (effsPet == null) thePet.createSpellEffects();
            if (thePlayer.getPet() !=null){
            for (int x = 0; x < 253; ++x) {
                effPlayer = effsPlayer.getSpellEffect((byte)x);
                    if(effPlayer != null){
                        effPet = effsPet.getSpellEffect((byte)x);                        
                        if(effPet == null){
                            effsPet.addSpellEffect(effPlayer);    
                        } else {
                            effPet = effsPlayer.getSpellEffect((byte) x);
                        thePet.sendUpdateSpellEffect(effPet);
                        }
                    }
            }

            
            
//effPlayer = effsPlayer(byte)x);
                }
                }
            }
    }
            }
        
        
        }
        public static void returnToPlayer(Player thePlayer, Creature thePet) throws NoSuchCreatureException, Exception{
            Path path = null;
            float ownerX = thePlayer.getPosX();
            float ownerY = thePlayer.getPosY();
            float creatureX = thePet.getPosX();
            float creatureY = thePet.getPosY();
            float creatureZ = thePet.getPositionZ();
            float diffX = ownerX - creatureX;
            float diffY = ownerY - creatureY;
            int tilex = thePlayer.getTileX();
            int tiley = thePlayer.getTileY();
            
            int diff = (int) Math.max(Math.abs(diffX), Math.abs(diffY));
            if ((diffX >= 0.0F || creatureX >= 10.0F) && (diffY >= 0.0F || creatureY >= 10.0F) && (diffX <= 0.0F || creatureX <= Zones.worldMeterSizeX - 10.0F) && (diffY <= 0.0F || creatureY <= Zones.worldMeterSizeY - 10.0F)) {
            if (diff > 35) {
                if (!thePet.isWithinDistanceTo(thePlayer, 30f)){
                    thePet.getCurrentTile().deleteCreatureQuick(thePet);
                    thePet.setLayer(thePlayer.getLayer(), false);
                    thePet.setPositionX(thePlayer.getPosX());
                    thePet.setPositionY(thePlayer.getPosY());
                    thePet.setPositionZ(thePlayer.getPositionZ());
                    thePet.setBridgeId(thePlayer.getBridgeId());
                    thePet.pushToFloorLevel(thePlayer.getFloorLevel());
                    try {
                        thePlayer.getCurrentTile().getZone().addCreature(thePet.getWurmId());
                    } catch (NoSuchCreatureException | NoSuchPlayerException e) {
                        logger.info(String.format("an error"));
                    }
                }
            } else if (diffX > 2.0F || diffY > 2.0F || diffX < -2.0F || diffY < -2.0F) {
                int oldTileX = (int) creatureX >> 2;
                int oldTileY = (int) creatureY >> 2;

                double newRot = Math.atan2((double) (ownerY - creatureY), (double) (ownerX - creatureX)) * R2D + 90.0D;
                if (newRot > 360.0D) newRot -= 360.0D;
                if (newRot < 0.0D) newRot += 360.0D;
                float moveX = 0.0F;
                float moveY = 0.0F;
                if (diffX < -2.0F) {
                    moveX = diffX + 2.0F;
                } else if (diffX > 2.0F) {
                    moveX = diffX - 2.0F;
                }
                if (diffY < -2.0F) {
                    moveY = diffY + 2.0F;
                } else if (diffY > 2.0F) {
                    moveY = diffY - 2.0F;
                }
                float modX = (float) Math.sin(newRot * D2R) * Math.abs(moveX -0.5f + Server.rand.nextFloat());
                float modY = -((float) Math.cos(newRot * D2R)) * Math.abs(moveY -0.5f + Server.rand.nextFloat());
                float newX = creatureX + modX;
                float newY = creatureY + modY;
                int newTileX = (int) newX >> 2;
                int newTileY = (int) newY >> 2;
                if (!thePlayer.isOnSurface() && !thePet.isOnSurface() && Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile((int) newX >> 2, (int) newY >> 2)))) {
                    newX = ownerX;
                    newY = ownerY;
                    newTileX = (int) newX >> 2;
                    newTileY = (int) newY >> 2;
                }

                float newZ = Zones.calculatePosZ(newX, newY, null, thePet.isOnSurface(), false, creatureZ, thePet, thePet.getBridgeId());

                thePet.setRotation((float) newRot);
                thePet.setPositionX(newX);
                thePet.setPositionY(newY);
                thePet.setPositionZ(newZ);             
                

                //1.9 
                thePet.moved(newX - creatureX, newY - creatureY, newZ - creatureZ, newTileX - oldTileX, newTileY - oldTileY);
            } else {
                thePet.moved(0, 0, 0, 0, 0);
                if(!thePlayer.isMoving() && Server.rand.nextInt(1000)==0){
                    try{
                        path = thePet.findPath(thePlayer.getTileX() + Server.rand.nextInt(8)-4,thePlayer.getTileY() + Server.rand.nextInt(8)-4,null);
                        if(!thePet.isPathing() && path != null && path.getSize()>=1){
                            thePet.getStatus().setPath(path);                                                                    
                            thePet.receivedPath = true;
                            //crets2[z].startPathing(10);
                            PathTile p = path.getTargetTile();
                            thePet.startPathingToTile(p);
                            thePet.setPathing(true, false); 
                        }
                        }
                        catch (Exception nsp) {
                            thePet.setPathing(false,false);
                            logger.info(String.format("%s no Path idling around %s", thePet.getName(),thePlayer.getName()));
                            }
                }
            }
        }

            }    
        
        public static void moveToTarget(Creature theTarget, Creature thePet) throws NoSuchCreatureException, Exception{
            Path path = null;
            float ownerX = theTarget.getPosX();
            float ownerY = theTarget.getPosY();
            float creatureX = thePet.getPosX();
            float creatureY = thePet.getPosY();
            float creatureZ = thePet.getPositionZ();
            float diffX = ownerX - creatureX;
            float diffY = ownerY - creatureY;
            int tilex = theTarget.getTileX();
            int tiley = theTarget.getTileY();
            
            int diff = (int) Math.max(Math.abs(diffX), Math.abs(diffY));
            if ((diffX >= 0.0F || creatureX >= 10.0F) && (diffY >= 0.0F || creatureY >= 10.0F) && (diffX <= 0.0F || creatureX <= Zones.worldMeterSizeX - 10.0F) && (diffY <= 0.0F || creatureY <= Zones.worldMeterSizeY - 10.0F)) {
            if (diff > 85) {
                if (!thePet.isWithinDistanceTo(theTarget, 600f)){
                    thePet.getCurrentTile().deleteCreatureQuick(thePet);
                    thePet.setLayer(theTarget.getLayer(), false);
                    thePet.setPositionX(theTarget.getPosX());
                    thePet.setPositionY(theTarget.getPosY());
                    thePet.setPositionZ(theTarget.getPositionZ());
                    thePet.setBridgeId(theTarget.getBridgeId());
                    thePet.pushToFloorLevel(theTarget.getFloorLevel());
                    try {
                        theTarget.getCurrentTile().getZone().addCreature(thePet.getWurmId());
                    } catch (NoSuchCreatureException | NoSuchPlayerException e) {
                        logger.info(String.format("an error"));
                    }
                }
            } else if (diffX > 2.0F || diffY > 2.0F || diffX < -2.0F || diffY < -2.0F) {
                int oldTileX = (int) creatureX >> 2;
                int oldTileY = (int) creatureY >> 2;

                double newRot = Math.atan2((double) (ownerY - creatureY), (double) (ownerX - creatureX)) * R2D + 90.0D;
                if (newRot > 360.0D) newRot -= 360.0D;
                if (newRot < 0.0D) newRot += 360.0D;
                float moveX = 0.0F;
                float moveY = 0.0F;
                if (diffX < -2.0F) {
                    moveX = diffX + 2.0F;
                } else if (diffX > 2.0F) {
                    moveX = diffX - 2.0F;
                }
                if (diffY < -2.0F) {
                    moveY = diffY + 2.0F;
                } else if (diffY > 2.0F) {
                    moveY = diffY - 2.0F;
                }
                float modX = (float) Math.sin(newRot * D2R) * Math.abs(moveX -0.5f  + Server.rand.nextFloat());
                float modY = -((float) Math.cos(newRot * D2R)) * Math.abs(moveY -05.f + Server.rand.nextFloat());
                float newX = creatureX + modX;
                float newY = creatureY + modY;
                int newTileX = (int) newX >> 2;
                int newTileY = (int) newY >> 2;
                if (!theTarget.isOnSurface() && !thePet.isOnSurface() && Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile((int) newX >> 2, (int) newY >> 2)))) {
                    newX = ownerX;
                    newY = ownerY;
                    newTileX = (int) newX >> 2;
                    newTileY = (int) newY >> 2;
                }

                float newZ = Zones.calculatePosZ(newX, newY, null, thePet.isOnSurface(), false, creatureZ, thePet, thePet.getBridgeId());

                thePet.setRotation((float) newRot);
                thePet.setPositionX(newX);
                thePet.setPositionY(newY);
                thePet.setPositionZ(newZ);
            
                

            thePet.moved(newX - creatureX, newY - creatureY, newZ - creatureZ, newTileX - oldTileX, newTileY - oldTileY);
;
            } else {
                thePet.moved(0, 0, 0, 0, 0);

            }
        }

            }
        
        public static void addTheSkills(Creature thePet) throws NoSuchCreatureException, Exception{
            logger.info(String.format("%s gained a skill tick", thePet.getName()));
            float theAdder = (float) ((Server.rand.nextInt(100))*.0005);
            double theCurrentSkill;
            theCurrentSkill = thePet.getSkills().getSkillOrLearn(100).getKnowledge();
            if(theCurrentSkill < 80){
                thePet.setSkill(100, (float) ((theCurrentSkill) + theAdder));
            }
            
            theAdder = (float) ((Server.rand.nextInt(100))*.0005);
            theCurrentSkill = thePet.getSkills().getSkillOrLearn(101).getKnowledge();
            if(theCurrentSkill < 80){
                thePet.setSkill(101, (float) ((theCurrentSkill) + theAdder));
            }
            
            theAdder = (float) ((Server.rand.nextInt(100))*.0005);          
            theCurrentSkill = thePet.getSkills().getSkillOrLearn(102).getKnowledge();
            if(theCurrentSkill < 80){
                thePet.setSkill(102, (float) ((theCurrentSkill) + theAdder));
            }
            
            theAdder = (float) ((Server.rand.nextInt(100))*.0005);          
            theCurrentSkill = thePet.getSkills().getSkillOrLearn(103).getKnowledge();
            if(theCurrentSkill < 80){
                thePet.setSkill(103, (float) ((theCurrentSkill) + theAdder));
            }
            
            theAdder = (float) ((Server.rand.nextInt(100))*.0005);          
            theCurrentSkill = thePet.getSkills().getSkillOrLearn(104).getKnowledge();
            if(theCurrentSkill < 80){
                thePet.setSkill(104, (float) ((theCurrentSkill) + theAdder));
            } 
            
            theAdder = (float) ((Server.rand.nextInt(100))*.0005);          
            theCurrentSkill = thePet.getSkills().getSkillOrLearn(105).getKnowledge();
            if(theCurrentSkill < 80){
                thePet.setSkill(105, (float) ((theCurrentSkill) + theAdder));
            }            

            theAdder = (float) ((Server.rand.nextInt(100))*.0005);          
            theCurrentSkill = thePet.getSkills().getSkillOrLearn(106).getKnowledge();
            if(theCurrentSkill < 80){
                thePet.setSkill(106, (float) ((theCurrentSkill) + theAdder));
            }

            //thePet.getStatus().modifyHunger(-10000, 0.9f);
            thePet.setLoyalty((float) (thePet.getLoyalty() + 1)); 
        }
        
        
        public static void initializeThePet(Player player){
            player.getPet().setSkill(100, 10f);
                player.getPet().setSkill(101, 20f);
                //player.getPet().setSkill(102, 30f);
                player.getPet().setSkill(103, 30f);
                player.getPet().setSkill(104, 15f);
                player.getPet().setSkill(105, 10f);               
                player.getPet().setSkill(106, 10f);
                player.getPet().setSkill(1013, 10f);
                player.getPet().setSkill(1014, 10f);
                player.getPet().setSkill(1023, 10f);
                player.getPet().setSkill(10052, 10f);
                player.getPet().setSkill(10053, 10f);
                player.getPet().setSkill(10054, 10f);
                player.getPet().setSkill(10055, 10f);                
                player.getPet().setSkill(10064, 10f);


        }
        

        }
        

