package com.wurmonline.server.spells;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.behaviours.NoSuchActionException;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.highways.HighwayPos;
import com.wurmonline.server.highways.MethodsHighways;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import java.util.logging.Level;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

public class IronFloor extends ReligiousSpell {

    public IronFloor(SpellcraftSpell spell){
        super(spell.getName(), ModActions.getNextActionId(), spell.getCastTime(), spell.getCost(), spell.getDifficulty(), spell.getFaith(), spell.getCooldown());
        this.targetTile = true;
        this.effectdesc = "Reinforced cave floor";
        this.description = "reinforces cave floors";

        ActionEntry actionEntry = ActionEntry.createEntry((short) number, name, "Iron Floor",
                new int[] { 2 /* ACTION_TYPE_SPELL */, 36 /* ACTION_TYPE_ALWAYS_USE_ACTIVE_ITEM */ ,Actions.ACTION_TYPE_IGNORERANGE});
        ModActions.registerAction(actionEntry);
    }
 @Override
    boolean precondition(Skill castSkill, Creature performer, int tilex, int tiley, int layer) {
        int tile = Server.caveMesh.getTile(tilex, tiley);
        if (layer < 0 || Tiles.decodeType(tile) == Tiles.Tile.TILE_CAVE_EXIT.id) {
            if (Tiles.decodeType(tile) == Tiles.Tile.TILE_CAVE_EXIT.id || Tiles.decodeType(tile) == Tiles.Tile.TILE_CAVE.id || Tiles.isReinforcedFloor(Tiles.decodeType(tile))) {
                byte type;
                int ts;
                HighwayPos highwayPos = MethodsHighways.getHighwayPos(tilex, tiley, false);
                if (highwayPos != null && MethodsHighways.onHighway(highwayPos)) {
                    return false;
                }
                VolaTile t = Zones.getOrCreateTile(tilex, tiley, false);
                if (t.getStructure() != null) {
                    performer.getCommunicator().sendNormalServerMessage("The structure gets in the way.", (byte)3);
                    return false;
                }
                if (t.getVillage() == null) {
                    for (int x = -1; x <= 1; ++x) {
                        for (int y = -1; y <= 1; ++y) {
                            VolaTile vt;
                            if (x == 0 && y == 0 || (vt = Zones.getTileOrNull(tilex + x, tiley + y, false)) == null || vt.getStructure() == null) continue;
                            performer.getCommunicator().sendNormalServerMessage("The nearby structure gets in the way.", (byte)3);
                            return false;
                        }
                    }
                }
                if (Tiles.isMineDoor(type = Tiles.decodeType(ts = Server.surfaceMesh.getTile(tilex, tiley)))) {
                    performer.getCommunicator().sendNormalServerMessage("You need to destroy the mine door first.", (byte)3);
                    return false;
                }
            } else {
                if (Tiles.isOreCave(Tiles.decodeType(tile))) {
                    performer.getCommunicator().sendNormalServerMessage("Nothing happens on the ore.", (byte)3);
                    return false;
                }
                if (Tiles.isSolidCave(Tiles.decodeType(tile))) {
                    performer.getCommunicator().sendNormalServerMessage("This spell is only to be used on cave floors", (byte)3);
                    return false;
                }
                if (Tiles.isReinforcedCave(Tiles.decodeType(tile))) {
                    performer.getCommunicator().sendNormalServerMessage("Nothing happens on the reinforced rock.", (byte)3);
                    return false;
                }
            }
            return true;
        }
        performer.getCommunicator().sendNormalServerMessage("This spell works on rock below ground.", (byte)3);
        return false;
    }

	
    void doEffect(Skill castSkill, double power, Creature performer, int tilex, int tiley, int layer, int heightOffset) {
        String floorexit;
        int tile = Server.caveMesh.getTile(tilex, tiley);
        Communicator comm = performer.getCommunicator();
        boolean done = false;
        Skills skills = performer.getSkills();
        boolean insta = performer.getPower() > 3;
        Skill mining = skills.getSkillOrLearn(1008);
        int time = 0;
            try {
                time = performer.getCurrentAction().getTimeLeft();
            }
            catch (NoSuchActionException nsa) {
                logger.log(Level.INFO, "This action does not exist?", nsa);
            }
        
        

            floorexit = Tiles.decodeType(tile) == Tiles.Tile.TILE_CAVE_EXIT.id ? "exit" : "floor";
            comm.sendNormalServerMessage("You add iron to the cave floor " + floorexit + ".");
            Server.getInstance().broadCastAction(performer.getName() + " adds iron to the cave floor " + floorexit + ".", performer, 5);

            if (Tiles.decodeType(tile) == Tiles.Tile.TILE_CAVE_EXIT.id) {
                Server.setClientCaveFlags(tilex, tiley, Tiles.Tile.TILE_CAVE_FLOOR_REINFORCED.id);
            } else {
                int encodedValue = Tiles.encode(Tiles.decodeHeight(tile), Tiles.Tile.TILE_CAVE_FLOOR_REINFORCED.id, Tiles.decodeData(tile));
                Server.caveMesh.setTile(tilex, tiley, encodedValue);
            }
            Players.getInstance().sendChangedTile(tilex, tiley, false, true);
    }

    }





