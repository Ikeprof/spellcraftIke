/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mod.sin.spellcraft;

import com.wurmonline.mesh.Tiles;
import static com.wurmonline.server.Items.getItem;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.CreatureStatus;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.zones.VirtualZone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.server.zones.VolaTile;
import java.util.logging.Level;
import java.util.logging.Logger;
import static mod.sin.spellcraft.Ikecode.playerToWispMap;


/**
 *
 * @author Ike
 */
public class WispUpdate {
    private static final double R2D = 57.29577951308232D;
    private static final double D2R = 0.01745329238474369D;
        public static void wwUpdate() {
        Creature[] playerArray = null;
        playerArray = Players.getInstance().getPlayers();
        Creature theWisp = null;
        for (int pl = 0;pl < playerArray.length;++pl){
            if(playerToWispMap.containsKey(playerArray[pl].getWurmId())){
                try {
                    //player has a wisp.  See if it still actually exists in game or has poofed
                    Long theWispID = playerToWispMap.get(playerArray[pl].getWurmId());
                    theWisp = Creatures.getInstance().getCreature(theWispID);
                    theWisp.isLit = true;
                } catch (NoSuchCreatureException ex) {
                    Logger.getLogger(SpellcraftMod.class.getName()).log(Level.SEVERE, null, ex);
                    playerToWispMap.remove(playerArray[pl].getWurmId());
                    
                    continue;
                }
            float ownerX = playerArray[pl].getPosX() + Server.rand.nextInt(4)-2;
            float ownerY = playerArray[pl].getPosY()+ Server.rand.nextInt(4)-2;
            float ownerZ = playerArray[pl].getPositionZ();
            float creatureX = theWisp.getPosX();
            float creatureY = theWisp.getPosY();
            float creatureZ = theWisp.getPositionZ();
            float diffX = ownerX - creatureX;
            float diffY = ownerY - creatureY;
                if (diffX > 1.0F || diffY > 1.0F || diffX < -1.0F || diffY < -1.0F) {
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
                if (!playerArray[pl].isOnSurface() && !theWisp.isOnSurface() && Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile((int) newX >> 2, (int) newY >> 2)))) {
                    newX = ownerX;
                    newY = ownerY;
                    newTileX = (int) newX >> 2;
                    newTileY = (int) newY >> 2;
                }

                float newZ = Zones.calculatePosZ(newX, newY, null, playerArray[pl].isOnSurface(), false, ownerZ, theWisp, theWisp.getBridgeId());

                theWisp.setRotation((float) newRot);
                theWisp.setPositionX(newX);
                theWisp.setPositionY(newY);
                theWisp.setPositionZ(newZ);
                //next 4 lines not used in 1.9
                int deltaX = (int) (newX * 10.0F) - (int) (creatureX * 10.0F);
                int deltaY = (int) (newY * 10.0F) - (int) (creatureY * 10.0F);
                int deltaZ = (int) (newZ * 10.0F) - (int) (creatureZ * 10.0F);   
                theWisp.moved(deltaX, deltaY, deltaZ, newTileX - oldTileX, newTileY - oldTileY);
//1.9
//                theWisp.moved(newX - creatureX, newY - creatureY, newZ - creatureZ,
//                        newTileX - oldTileX, newTileY - oldTileY);
                VolaTile t = Zones.getTileOrNull(theWisp.getTileX(), theWisp.getTileY(), theWisp.isOnSurface());

                //this.watcher.getCommunicator().
                for (final VirtualZone vz : t.getWatchers()) {
                    if(vz.getWatcher().isPlayer()){
                    vz.getWatcher().getCommunicator().sendAddEffect(theWisp.getWurmId(), (short)27, theWisp.getPosX(), theWisp.getPosY(), theWisp.getPositionZ(),(byte)(theWisp.isOnSurface()?0:-1),"iceBolt1",3f,0f);
                    }
                    }
                
                
                
                // public void setHasLightSource(final Creature creature, final byte colorRed, final byte colorGreen, final byte colorBlue, final byte radius) {

                t.setHasLightSource(theWisp,(byte)253,(byte)253,(byte)253,(byte)20);
                    
            } else {
                theWisp.moved(0, 0, 0, 0, 0);
                

            }
                
            }
        }

    }
}
