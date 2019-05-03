/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mod.sin.spellcraft;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.creatures.SpellEffects;
import com.wurmonline.server.creatures.ai.NoPathException;
import com.wurmonline.server.creatures.ai.Path;
import com.wurmonline.server.creatures.ai.PathFinder;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.NoSpaceException;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.sounds.SoundPlayer;
import com.wurmonline.server.spells.SpellEffect;
import com.wurmonline.server.structures.Blocking;
import com.wurmonline.server.structures.BlockingResult;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.VirtualZone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.server.zones.VolaTile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static mod.sin.spellcraft.SpellcraftSpellEffects.logger;

/**
 *
 * @author Ike
 */
public class Ikecode {
    private static final double R2D = 57.29577951308232D;
    private static final double D2R = 0.01745329238474369D;
            //villageNPC variables
        public static long npcLastTalk = 0;
        public static long checkNPCvalid = 0;
        public static long interactNPC = 0;
	
        //fireorb spelleffect variables
        public static long FireOrblastTicktime = 0;
        public static long FireOrbSmokeTickTime = 0;
        public static long SparkSpreadTickTime = 0;
        
        //PetValidityCheck variables
        public static long PetValidityCheckTickTime = 0;
        
        //Pack Movement variables
        public static long PackMoveTickTime = 0;
        public static long FellowMobsTickTime = 0;
        public static long LeaderNPETime = 0;
        public static int packBiteSize = 5;
        public static int packLastIndex = 0;
        public static int packLoopCounter = 0;
        public static long packLoopStart = 0;
        public static long packLoopEnd = 0;
        
        //Pet/Guardian variables
        public static long GuardianTickTime = 0;
        public static long PetPathfindTickTime = 0;
        
        //Willowwisp variables
        public final static Map<Long, Long> wispToPlayerMap = new HashMap<>();        
        public final static Map<Long, Long> playerToWispMap = new HashMap<>();
        public static long wwLastTickTime = 0;
        
        //MagicMobs Variables
        public static long StrikerCastTime = 0;
        public static long NecrosisCastTime = 0;
    public static void ThePoll(){
        
        try {
            CustomSpellsUpdate();
        } catch (NoSuchCreatureException ex) {
            Logger.getLogger(SpellcraftMod.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if(System.currentTimeMillis()> wwLastTickTime + 1000 ){
            wwLastTickTime = System.currentTimeMillis();
           WispUpdate.wwUpdate();
        }
        //pack form and move
        if(System.currentTimeMillis()>PackMoveTickTime + 1000){
            try {
                packLoopStart = System.currentTimeMillis();
                PackMoveTickTime = System.currentTimeMillis();
                PackFormAndMoveV3();
                packLoopEnd = System.currentTimeMillis();
                long theLag = packLoopEnd-packLoopStart;
                if(theLag > 7){
                    --packBiteSize;
                } else{
                    ++packBiteSize;
                }
                //packBiteSize = Math.min(200, packBiteSize);
                if(theLag > 1000){
                    logger.info(String.format("PackFormandMove Lag Spike = %d", theLag));
                }
                ++packLoopCounter;
                if(packLoopCounter >= 60){
                    packLoopCounter = 0;
                    logger.info(String.format("60 loops done.  Current pack bitesize = %d", packBiteSize));
                }

                //BunchingCheck();
            } catch (Exception ex) {
                Logger.getLogger(SpellcraftMod.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
        if(System.currentTimeMillis()>PetValidityCheckTickTime + 30000){
            PetValidityCheckTickTime = System.currentTimeMillis();
            PetValidityCheck();
        }
        if(System.currentTimeMillis()>FellowMobsTickTime + 250){
            
            FellowMobsTickTime = System.currentTimeMillis();
            try {
                FellowMobsJoinIn();
            } catch (NoPathException ex) {
                Logger.getLogger(SpellcraftMod.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if(System.currentTimeMillis() > GuardianTickTime + 1000){
            try {
                Guardian.ProcessIt();
                MagicMobsCombat();                
//VillageNPC.ProcessIt();;
            } catch (Exception ex) {
                Logger.getLogger(SpellcraftMod.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }
        public static void CustomSpellsUpdate() throws NoSuchCreatureException{
        PathFinder pf = new PathFinder();
        SpellEffects effs = null;
        SpellEffect eff;
        SpellEffects teffs = null;
        Creature[] cretarr;
        Creature thePlayer = null;
        Creature theEldest = null;
        Creature thePolledCreature = null;
        cretarr = Creatures.getInstance().getCreatures();
        int PathCounter = 0;
        for (int x2 = 0; x2 < cretarr.length; ++x2) {
            thePolledCreature = cretarr[x2];
            if (thePolledCreature.isAlive()) {
                if (!thePolledCreature.isPlayer()){
                    try{
                    if (thePolledCreature.getSpellEffects()!= null) {
                        effs = thePolledCreature.getSpellEffects();
                        if (effs.getSpellEffect((byte)150)!= null) {
                            if(System.currentTimeMillis() > FireOrblastTicktime + 500){
                                FireOrblastTicktime = System.currentTimeMillis();
                                thePlayer = (thePolledCreature.lastOpponent);
                                if(thePlayer == null){
                                    long wurmID = thePolledCreature.target;
                                    if (wurmID != -10L){
                                        thePlayer = Players.getInstance().getPlayerOrNull(thePolledCreature.target);
                                        if(thePlayer == null){
                                            thePlayer = Creatures.getInstance().getCreature(thePolledCreature.target);
                                        }
                                        //thePlayer = Creatures.getInstance().getCreature(thePolledCreature.target);
                                    }
                                }
                            try {
                            //addWoundOfType(@Nullable Creature attacker, byte woundType, int pos, boolean randomizePos, float armourMod, boolean calculateArmour, double damage)

                                if(System.currentTimeMillis() >= FireOrbSmokeTickTime + 500 ){
                                    FireOrbSmokeTickTime = System.currentTimeMillis();
                                    thePolledCreature.getCurrentTile().sendAttachCreatureEffect(thePolledCreature, (byte)5, (byte)150, (byte)150, (byte)150, (byte)10);
                                }
                                float thePower =  (float) (effs.getSpellEffect((byte)150).getPower());// * 1.5);
                                //thePosition = thePolledCreature.getBody().getRandomWoundPos();
                                //thePower = Math.min(thePower, 10000f);
                                //logger.info(String.format("Fire Orb Damage %f ", thePower));
                                thePolledCreature.addWoundOfType(thePlayer, (byte)4,0 , true, thePolledCreature.getArmourMod(), true, thePower,0f,0f,true,true);
                                if(thePlayer!=null){
                                    thePolledCreature.addAttacker(thePlayer);
                                    thePolledCreature.setTarget(thePlayer.getWurmId(), true);
                                    thePlayer.addAttacker(thePolledCreature);
                                }
                            //this
                        } catch (Exception ex) {
                            Logger.getLogger(SpellcraftMod.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        }
                        //done with firorb, now do spark
                        } 
                        if (effs.getSpellEffect((byte)151)!= null) {

                            Creature toDebug = thePolledCreature;
                            thePlayer = (thePolledCreature.lastOpponent);
                            long wurmID = thePolledCreature.target;
                            if(thePlayer == null){
                                if (wurmID != -10L){
                                    thePlayer = Players.getInstance().getPlayerOrNull(thePolledCreature.target);
                                    if(thePlayer == null){
                                        thePlayer = Creatures.getInstance().getCreature(thePolledCreature.target);
                                    }
                                    
                                }
                            }
                            float thePower =  (float) (effs.getSpellEffect((byte)151).getPower());
                            thePower = thePower - 100;
                            if (thePower > 200 && (wurmID != -10L || thePlayer != null) ) {                            
                                float theTimeLeft = effs.getSpellEffect((byte)151).timeleft;                            
                                //thePolledCreature.addWoundOfType(thePolledCreature.opponent, (byte)10,0 , true, 0.0f, true, thePower,0f,0f,true,true);
                                int tileX = thePolledCreature.currentTile.tilex;
                                int tileY = thePolledCreature.currentTile.tiley;
                                int x,y,sx,sy,ex,ey;
                                sx = Zones.safeTileX(tileX - 4);
                                sy = Zones.safeTileY(tileY - 4);
                                ex = Zones.safeTileX(tileX + 4);
                                ey = Zones.safeTileY(tileY + 4); 
                                for (x = sx; x <= ex; ++x) {
                                    for (y = sy; y <= ey; ++y) {
                                    VolaTile t = Zones.getTileOrNull(x, y, thePolledCreature.isOnSurface());
                                    if (t == null) continue;
                                    Creature[] targetMob = t.getCreatures();
                                    if (targetMob.length > 0) {
                                    for (Creature targetMob1 : targetMob) {
                                        if (!targetMob1.getName().contains("guard") && !targetMob1.getName().contains("templar") && !targetMob1.getName().contains("dog") && !targetMob1.getName().contains("wisp") && !targetMob1.getName().contains("kitty")&& (!targetMob1.isPlayer() && targetMob1 != thePolledCreature) && targetMob1.isAlive() && targetMob1.leader == null && targetMob1.dominator == -10L && !targetMob1.isHitched() ) {    
                                            if ((effs = targetMob1.getSpellEffects()) == null) {
                                                effs = targetMob1.createSpellEffects();   
                                            }  
                                            if (effs.getSpellEffect((byte)151)== null) {
                                                if (System.currentTimeMillis()>SparkSpreadTickTime + 200){
                                                    SparkSpreadTickTime = System.currentTimeMillis();
                                                    eff = new SpellEffect(targetMob1.getWurmId(),(byte)151,(float) thePower,(int) theTimeLeft ,(byte) 1,(byte) 1,false);
                                                    effs.addSpellEffect(eff);
                                                    targetMob1.addWoundOfType(thePlayer, (byte)9,0 , true, 0.0f, true, thePower,0f,0f,true,true);
                                                    //targetMob1.setPositionZ(targetMob1.getPositionZ() - 2);
                                                    SoundPlayer.playSound("sound.combat.fleshbone1", targetMob1, 1.6f);
                                                    if(thePlayer == null){
                                                       logger.info( "null player"); 
                                                    }
                                                    if(thePlayer!=null){
                                                        targetMob1.addAttacker(thePlayer);
                                                        targetMob1.setTarget(thePlayer.getWurmId(), true);
                                                        thePlayer.addAttacker(targetMob1);
                                                    }
                                                    for (final VirtualZone vz : t.getWatchers()) {
                                                        if(vz.getWatcher().isPlayer()){
                                                        vz.getWatcher().getCommunicator().sendAddEffect(targetMob1.getWurmId(), (short)27, targetMob1.getPosX(), targetMob1.getPosY(), targetMob1.getPositionZ(),(byte)(targetMob1.isOnSurface()?0:-1),"tree",1f,0f);
                                                    
                                                        }
                                                    }
                                                }
                                            }
                                        }    

                                    }
                                    }

                                }
                        } 
                        }//after  
                        }
                    }
                    } catch (Exception ex) {
                            Logger.getLogger(SpellcraftMod.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }//this
    }
    public static void TileCleanup(VolaTile t){
        
        //destroy corpses off deed after interval
        if(Server.rand.nextInt(2)== 0){
            Item[] theItems = t.getItems();
            for (int i = 0; i<theItems.length;i++) {
                long theCreationDate = theItems[i].creationDate;
                long theTime = WurmCalendar.currentTime;
                long theDecay = theTime-theCreationDate;
                int theTemplateID = theItems[i].getTemplateId();
                long theParentID = theItems[i].getOwnerId();//.getParentId();
                String theParentName = "";
                if (theParentID != -10L){
                    theParentName = theItems[i].getParentOrNull().getName();
                } else {
                    theParentName = "poop";
                }
                int theItemsSize = theItems[i].getItems().size();
                Village theVillage = t.getVillage();

                if ((theTemplateID == 272 || theTemplateID == 314 || theTemplateID == 26 || theTemplateID == 146|| theTemplateID == 9) && (theParentID == -10L || theParentID == 177 || theParentName.toLowerCase().contains("pile")) && theDecay > 20000) {
                    theItems[i].setDamage(100f);
                    //logger.info(String.format("Corpse Poof"));
                }
            }
        }
    }    
    public static void PetValidityCheck(){
        Creature[] cretarr=null;
        cretarr = Creatures.getInstance().getCreatures();
        for (int x = 0; x < cretarr.length; ++x) {
            Creature thePet = cretarr[x];
            if(thePet.getDominator()!=null){
                Long theOwnerID = thePet.getDominator().getWurmId();
                Player theOwner = Players.getInstance().getPlayerOrNull(theOwnerID);
                if(theOwner!=null){
                    Creature theOwnersPet = theOwner.getPet();
                    if(theOwnersPet!=null){
                        Long theOwnersPetID = theOwner.getPet().getWurmId();
                        if(theOwnersPetID != thePet.getWurmId()){
                            logger.info(String.format("Pet Validity Check Fail.  Pet %s thought its owner was %s, but owner had pet of %s", thePet.getName() , theOwner.getName(), theOwner.getPet().getName()));
                            theOwner.getCommunicator().sendNormalServerMessage(String.format("The animal %s thinks you are it's owner, yet your pet is a different creature.  %s will revert to no owner ", thePet.getName(), thePet.getName() ));
                            thePet.setDominator(-10L);
                        }                        
                    }

                }

            }
        }
    }    
    public static void PackFormAndMoveV3() throws NoSpaceException, FailedException, NoSuchTemplateException{     
        Creature[] cretarr = null;
        ArrayList<Creature> packArrayL = new ArrayList<Creature>();
        Creature theEldest = null;
        cretarr = Creatures.getInstance().getCreatures();
        PathFinder pf = new PathFinder();
        int MaxThisRun = packLastIndex + packBiteSize;
        boolean Restart = false;
        if(packLastIndex + packBiteSize >= cretarr.length ){
            MaxThisRun = cretarr.length;
            Restart = true;
            for (int x2 = 0; x2 < cretarr.length; ++x2) {
                //clears the "I've been moved" value
                cretarr[x2].setSkill(1010, (float) 1.111);
            }                
        }
        try{            
        for (int x2 = packLastIndex; x2 < MaxThisRun; ++x2) {
            Creature thePolledCreature = cretarr[x2];
            packArrayL.clear();
            double LoopStartTime = System.currentTimeMillis();
            if (!thePolledCreature.isPlayer() && !thePolledCreature.isFighting() && !thePolledCreature.isPathing() && !thePolledCreature.isSubmerged() && !thePolledCreature.isSwimming()  && thePolledCreature.isAlive()){
                long CurrentAge = thePolledCreature.getWurmId();
                Path path = null;
                Path path2 = null;
                Path path3 = null;
                int tileX = thePolledCreature.currentTile.tilex;
                int tileY = thePolledCreature.currentTile.tiley;
                int x,y,sx,sy,ex,ey;
                sx = Zones.safeTileX(tileX - 20);
                sy = Zones.safeTileY(tileY - 20);
                ex = Zones.safeTileX(tileX + 20);
                ey = Zones.safeTileY(tileY + 20);
                long MaxAge = Long.MAX_VALUE;
                int numOfLoops = 0;
                long NSAvg = 0;
                long EWAvg = 0;
                long hasBeenMoved = 0;
                int targetX = 0;
                int targetY = 0;
                for (x = sx; x <= ex; ++x) {
                    for (y = sy; y <= ey; ++y) {
                        VolaTile t = Zones.getTileOrNull(x, y, thePolledCreature.isOnSurface());
                        if (t==null) continue;
                        TileCleanup(t);
                        if( thePolledCreature.leader != null || thePolledCreature.isOnDeed() || thePolledCreature.isHitched()){
                            continue;
                        }
                        Creature[] targetMob = t.getCreatures();
                        //check if any mobs same species are older nearby, used after try below
                        if (targetMob.length > 0) {
                            for (Creature targetMob1 : targetMob) {
                                ++numOfLoops;
                                NSAvg = (long) (NSAvg + targetMob1.getSkills().getSkillOrLearn(1008).getKnowledge());
                                EWAvg = (long) (EWAvg + targetMob1.getSkills().getSkillOrLearn(1009).getKnowledge());
                                hasBeenMoved = (long) targetMob1.getSkills().getSkillOrLearn(1009).getKnowledge();
                                if ((targetMob1 != thePolledCreature) && !targetMob1.isSubmerged() && !targetMob1.isSwimming() && !targetMob1.isPathing() && targetMob1.isAlive() && targetMob1.leader == null && !targetMob1.isOnDeed() && !targetMob1.isHitched() && targetMob1.getTemplate().getTemplateId() == thePolledCreature.getTemplate().getTemplateId()) {
                                    long TargetAge = targetMob1.getWurmId();//.getStatus().age;
                                    packArrayL.add(targetMob1);
                                    if (TargetAge < CurrentAge && TargetAge < MaxAge) {
                                        targetX = targetMob1.getTileX();
                                        targetY = targetMob1.getTileY();
                                        MaxAge = TargetAge;
                                        theEldest = targetMob1;

                                    } 
                                } 
                            }   
                        }
                    }
                }                       
                if (MaxAge !=Long.MAX_VALUE ) {
                    if(!theEldest.isSubmerged() && !theEldest.isSwimming() && theEldest.isAlive() && theEldest.leader==null && !theEldest.isOnDeed() && !theEldest.isHitched() ){
                        if(true){ //Server.rand.nextInt(2)==0
                            if(true){
                                Boolean goodPath = false;
                                int pathTries = 0;
                                double NS = 0;
                                double EW = 0;
                                theEldest.setPathing(false, true);
                                theEldest.setPathfindcounter(0);
                                do{
                                    pathTries++;
                                        if(pathTries == 1){
                                            NS = theEldest.getSkills().getSkillOrLearn(1008).getKnowledge();
                                            EW = theEldest.getSkills().getSkillOrLearn(1009).getKnowledge(); 
                                        } else if(pathTries == 2) {
                                            NS = NSAvg/numOfLoops;
                                            EW = EWAvg/numOfLoops;
                                        } 
                                        while((Math.abs(NS)<=1 && Math.abs(EW)<=1)) {
                                            NS = Server.rand.nextInt(10) + 2;
                                            EW = Server.rand.nextInt(10) + 2;                                                              
                                        }                                                                                        
                                        targetX = (int) (theEldest.getTileX() + NS - 6);
                                        targetY = (int) (theEldest.getTileY() + EW - 6);
                                        Village v = Villages.getVillageWithPerimeterAt(targetX, targetY, true);
                                        //don't move the leader onto a deed.
                                        if (v == null) {
                                            VolaTile t = Zones.getOrCreateTile(theEldest.getTileX(), theEldest.getTileY(), true);
                                            theEldest.setSkill(1008, (float) 1.0);
                                            theEldest.setSkill(1009, (float) 1.0);
                                            Boolean EldestPath = false;
                                            try{
                                                path = pf.findPath(theEldest,theEldest.getTileX(), theEldest.getTileY(), targetX, targetY,theEldest.isOnSurface(),1);//was 10, not 1
                                                EldestPath = true;
                                            }catch(Exception espp){
                                                if(goodPath == false){
                                                    theEldest.setSkill(1008, (float) 1.0);
                                                    theEldest.setSkill(1009, (float) 1.0);
                                                    targetX = theEldest.getTileX();
                                                    targetY = theEldest.getTileY();
                                                }                                                   
                                            }
                                            if (EldestPath == true && path !=null){
                                                    for (final VirtualZone vz : t.getWatchers()) {
                                                        if(vz.getWatcher().isPlayer()){
                                                            vz.getWatcher().getCommunicator().sendAddEffect(theEldest.getWurmId(), (short)27, theEldest.getPosX(), theEldest.getPosY(), theEldest.getPositionZ(),(byte)(theEldest.isOnSurface()?0:-1),"dust03",5f,0f);
                                                        }
                                                    }
                                                //we didn't have an exception so set direciton to real values
                                                theEldest.setSkill(1008, (float) NS);
                                                theEldest.setSkill(1009, (float) EW);
                                                if(true){//allow satellites to group up
                                                    theEldest.getStatus().setPath(path);
                                                    theEldest.receivedPath = true;
                                                    //theEldest.startPathing(3);
                                                    theEldest.startUsingPath();
                                                    //theEldest.startPathingToTile(path.getFirst());
                                                } 
                                                goodPath = true;
                                                //begin satellite mob code 
                                                //logger.info(String.format("the NS %d", packArrayL.size()));
                                                packArrayL.sort((r1,r2) -> Long.compare(r1.getWurmId(),r2.getWurmId()));
                                                for (int p = 0; p < packArrayL.size(); p++){                                                        
                                                   Creature currentMob = packArrayL.get(p);
                                                   currentMob.setPathing(false, true);                                                      
                                                   currentMob.setSkill((1008),(float)NS);
                                                   currentMob.setSkill((1009),(float)EW);
                                                    if(!currentMob.isPathing() && currentMob != theEldest){
                                                        int theTest = currentMob.getPathfindCounter();
                                                        if(p==0){
                                                            targetX = (int) (theEldest.getTileX()+ NS - 6);
                                                            targetY = (int) (theEldest.getTileY()+ EW - 6);
                                                        } else {
                                                            //targetX = packArrayL.get(p-1).getTileX();
                                                            //targetY = packArrayL.get(p-1).getTileY();
                                                            targetX = (int) ( theEldest.getTileX() + Server.rand.nextInt(4)-2);
                                                            targetY = (int) ( theEldest.getTileY() + Server.rand.nextInt(4)-2);
                                                        }
                                                        Boolean SatPath = false;
                                                        try{
                                                            path2 = pf.findPath(currentMob,currentMob.getTileX(),currentMob.getTileY(), (int) targetX, (int) targetY,currentMob.isOnSurface(),1);
                                                            SatPath = true;
                                                        } catch(Exception nps){                                                            
                                                            pathTries = 0;
                                                            currentMob.setSkill((1008),(float)1);
                                                            currentMob.setSkill((1009),(float)1);
                                                            currentMob.setPathfindcounter(0);
                                                            currentMob.setPathing(false, true);
                                                            continue;
                                                        }
                                                        if(SatPath == true && path2 !=null){
                                                        //if(path2 != null && path2.getSize()>=1){  
                                                            currentMob.getStatus().setPath(path2);                                                                
                                                            currentMob.receivedPath = true;                                                                
                                                            //currentMob.startPathingToTile(path2.getFirst());
                                                            currentMob.startUsingPath();
                                                            currentMob.setSkill((1010),(float)3.1);                                                                
                                                            //currentMob.startPathing(3);
                                                            t = Zones.getOrCreateTile(currentMob.getTileX(), currentMob.getTileY(), true);
                                                                for (final VirtualZone vz : t.getWatchers()) {
                                                                    if(vz.getWatcher().isPlayer()){
                                                                        vz.getWatcher().getCommunicator().sendAddEffect(currentMob.getWurmId(), (short)27, currentMob.getPosX(), currentMob.getPosY(), currentMob.getPositionZ(),(byte)(currentMob.isOnSurface()?0:-1),"dust03",5f,0f);
                                                                    }
                                                                }
                                                        } else {
                                                            currentMob.setSkill((1008),(float)1);
                                                            currentMob.setSkill((1009),(float)1);
                                                        }
                                                }

                                                }
                                               //move the original mob to the target
                                               try{
                                               path3 = pf.findPath(thePolledCreature, thePolledCreature.getTileX(), thePolledCreature.getTileY(), (int) (theEldest.getTileX()+ NS - 6), (int) (theEldest.getTileY()+ NS - 6), true, 1);
                                               } catch(Exception nps){
                                               }

                                               if(path3 != null && path3.getSize()>=1){;
                                                   thePolledCreature.getStatus().setPath(path3);
                                                   thePolledCreature.receivedPath= true;
                                                   thePolledCreature.startUsingPath();
                                               }
                                            } else {
                                               //could not path for some reason
                                                theEldest.setSkill(1008, (float) 1.0);
                                                theEldest.setSkill(1009, (float) 1.0);
                                            }
                                        } else {
                                        //we hit a village, set to 1 to force a reroll
                                            theEldest.setSkill(1008, (float) 1.0);
                                            theEldest.setSkill(1009, (float) 1.0);
                                        }

                                        //continue;

                                } while(goodPath == false && pathTries < 4);
                            }
                        }
                    }
                MaxAge = Long.MAX_VALUE;
                }
                }


                //logger.info("dog Perimeter check");
                if(System.currentTimeMillis()> LoopStartTime + 1000){
                    logger.info(String.format("PackFormandMove Lag Anchor mob = %d, Satellite Mob = %d", theEldest.getWurmId(),thePolledCreature.getWurmId()));
                }
        }
}catch(Exception nps){
    //logger.info(String.format("IkeError  = %s", nps));

}

        packLastIndex = packLastIndex + packBiteSize;
        if(Restart) {
            packLastIndex = 0;
            logger.info(String.format("Full Loop"));
        }
        }

    
    public static void MagicMobsCombat() {
        //get nearby mobs to join in the fight
        PathFinder pf = new PathFinder();
        Path path = null;
        Creature[] cretarr=null;
        Creature thePolledCreature = null;
        cretarr = Creatures.getInstance().getCreatures();
        for (int x2 = 0; x2 < cretarr.length; ++x2) {
            thePolledCreature = cretarr[x2];
            if (thePolledCreature.target!=-10L && !thePolledCreature.isOnDeed() &&!thePolledCreature.isUnique() && !thePolledCreature.getName().contains("guard") && !thePolledCreature.getName().contains("templar")){
                Creature theTarget = thePolledCreature.getTarget();
                if(thePolledCreature.getName().contains("sentient") && Server.rand.nextInt(10)==0 && thePolledCreature.isWithinDistanceTo(theTarget, 40.0f)){
                    if ( System.currentTimeMillis() > NecrosisCastTime + 5000){
                        final BlockingResult result = Blocking.getRangedBlockerBetween(thePolledCreature, theTarget);
                        if (result == null) {
                            NecrosisCastTime = System.currentTimeMillis();
                            SoundPlayer.playSound(theTarget.getHitSound(), theTarget, 1.6f);
                            theTarget.addWoundOfType(thePolledCreature, (byte)8, 0, true, 1.0f, true, Server.rand.nextInt(1500) + 1000,0f,0f,true,true);
                            theTarget.addAttacker(thePolledCreature);
                            thePolledCreature.addAttacker(theTarget);
                            VolaTile t = Zones.getOrCreateTile(thePolledCreature.getTileX(), thePolledCreature.getTileY(), true);
                            for (final VirtualZone vz : t.getWatchers()) {
                                vz.getWatcher().getCommunicator().sendAddEffect(thePolledCreature.getWurmId(), (short)27, thePolledCreature.getPosX(), thePolledCreature.getPosY(), thePolledCreature.getPositionZ(),(byte)(thePolledCreature.isOnSurface()?0:-1),"iceBolt1",5f,0f);
                            }
                        }
                    }

                } else if(thePolledCreature.getName().contains("striker") && Server.rand.nextInt(10)==0 && thePolledCreature.isWithinDistanceTo(theTarget, 40.0f)){
                    final BlockingResult result = Blocking.getRangedBlockerBetween(thePolledCreature, theTarget);
                    if (result == null) {
                        if( System.currentTimeMillis()> StrikerCastTime + 5000){
                            StrikerCastTime = System.currentTimeMillis();
                            theTarget.addAttacker(thePolledCreature);
                            thePolledCreature.addAttacker(theTarget);
                            theTarget.addWoundOfType(thePolledCreature, (byte)10, 0, true, 1.0f, true, Server.rand.nextInt(1500) + 1000 ,0f,0f,true,true);
                            SoundPlayer.playSound(theTarget.getHitSound(), theTarget, 1.6f);
                            VolaTile t = thePolledCreature.getCurrentTile();
                            long shardId = WurmId.getNextTempItemId();
                            if (t != null) {                        
                                t.sendProjectile(shardId, (byte)4, "model.resource.brick", "Marble Brick", (byte)0, thePolledCreature.getPosX(), thePolledCreature.getPosY(), thePolledCreature.getPositionZ() + thePolledCreature.getAltOffZ(), thePolledCreature.getStatus().getRotation(), (byte)thePolledCreature.getLayer(), (int)theTarget.getPosX(), (int)theTarget.getPosY(), theTarget.getPositionZ() + theTarget.getAltOffZ(), thePolledCreature.getWurmId(), theTarget.getWurmId(), 0.0f, 0.0f);   
                            }   
                        }
                    }

                }


            }
        }
    }
    public static void FellowMobsJoinIn() throws NoPathException{
            //get nearby mobs to join in the fight
            PathFinder pf = new PathFinder();
            Path path = null;
            Creature[] cretarr=null;
            Creature thePolledCreature = null;
            cretarr = Creatures.getInstance().getCreatures();
            try{
            for (int x2 = 0; x2 < cretarr.length; ++x2) {
                thePolledCreature = cretarr[x2];
                if (thePolledCreature.isFighting() && !thePolledCreature.isOnDeed() &&!thePolledCreature.isUnique() && !thePolledCreature.getName().contains("guard") && !thePolledCreature.getName().contains("templar")){
                int tileX = thePolledCreature.currentTile.tilex;
                    int tileY = thePolledCreature.currentTile.tiley;
                    int x,y,sx,sy,ex,ey;
                    sx = Zones.safeTileX(tileX - 15);
                    sy = Zones.safeTileY(tileY - 15);
                    ex = Zones.safeTileX(tileX + 15);
                    ey = Zones.safeTileY(tileY + 15);  
                    for (x = sx; x <= ex; ++x) {
                        for (y = sy; y <= ey; ++y) {
                            VolaTile t = Zones.getTileOrNull(x, y, thePolledCreature.isOnSurface());
                            if (t == null){
                                continue;
                            }
                            Creature[] targetMob = t.getCreatures();
                            if (targetMob.length > 0) {
                                for(int z = 0; z<targetMob.length; z++){
                                    if(targetMob[z].getTemplate().getTemplateId()== thePolledCreature.getTemplate().getTemplateId() && !targetMob[z].getName().contains("guard") && !targetMob[z].getName().contains("templar") && !targetMob[z].isHitched() && targetMob[z].opponentCounter == 0 && !targetMob[z].isFighting()&& Server.rand.nextInt(10) == 0){
                                        if (targetMob[z].rangeTo(targetMob[z],thePolledCreature)< Actions.actionEntrys[114].getRange()){
                                            targetMob[z].setTarget(thePolledCreature.target, true);
                                            targetMob[z].attackTarget();
                                        } else{
                                            path = pf.findPath(targetMob[z],targetMob[z].getTileX(), targetMob[z].getTileY(), thePolledCreature.getTileX(), thePolledCreature.getTileY(),targetMob[z].isOnSurface(),3);//was 10, not 1
                                            if(path != null && path.getSize()>=1){
                                                targetMob[z].getStatus().setPath(path);
                                                targetMob[z].receivedPath = true;
                                                //theEldest.startPathing(0);
                                                targetMob[z].startUsingPath();
                                                for (final VirtualZone vz : t.getWatchers()) {
                                                    vz.getWatcher().getCommunicator().sendAddEffect(targetMob[z].getWurmId(), (short)27, targetMob[z].getPosX(), targetMob[z].getPosY(), targetMob[z].getPositionZ(),(byte)(targetMob[z].isOnSurface()?0:-1),"dust03",2f,0f);
                                                }
                                            }
                                        }
                                    }
                                }
                            }


                        }
                    }
                }
            }//after
            }catch(Exception espp){
            }
    }
}
