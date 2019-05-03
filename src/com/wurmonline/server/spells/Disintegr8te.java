package com.wurmonline.server.spells;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.behaviours.NoSuchActionException;
import com.wurmonline.server.behaviours.TileRockBehaviour;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.highways.HighwayPos;
import com.wurmonline.server.highways.MethodsHighways;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.VillageRole;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import java.util.logging.Level;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

public class Disintegr8te extends ReligiousSpell {

    public Disintegr8te(SpellcraftSpell spell){
        super(spell.getName(), ModActions.getNextActionId(), spell.getCastTime(), spell.getCost(), spell.getDifficulty(), spell.getFaith(), spell.getCooldown());
        this.targetTile = true;
        this.effectdesc = "Destroys Cave Veins";
        this.description = "destroys cave veins";

        ActionEntry actionEntry = ActionEntry.createEntry((short) number, name, "Disintegr8te",
                new int[] { 2 /* ACTION_TYPE_SPELL */, 36 /* ACTION_TYPE_ALWAYS_USE_ACTIVE_ITEM */ ,Actions.ACTION_TYPE_IGNORERANGE});
        ModActions.registerAction(actionEntry);
    }
  @Override
    boolean precondition(Skill castSkill, Creature performer, int tilex, int tiley, int layer) {
        int tile;
        if (layer < 0 && Tiles.isSolidCave(Tiles.decodeType(tile = Server.caveMesh.getTile(tilex, tiley)))) {
            if (Tiles.isReinforcedCave(Tiles.decodeType(tile))) {
                    Village v = Villages.getVillage(tilex, tiley, true);
                    if (v != null) {
                        VillageRole r = v.getRoleFor(performer);
                        return r != null && r.mayMineRock() && r.mayReinforce();
                    }               
            }
            return true;
        }
        performer.getCommunicator().sendNormalServerMessage("This spell works on rock below ground.");
        return false;
    }

    @Override
    void doEffect(Skill castSkill, double power, Creature performer, int tilex, int tiley, int layer, int heightOffset) {
        try {
            Action act = performer.getCurrentAction();
            int dir = (int)(act.getTarget() >> 48) & 255;
            if (dir == 1) {
                performer.getCommunicator().sendNormalServerMessage("The roof just resounds hollowly.", (byte)3);
                return;
            }
            if (dir == 0) {
                performer.getCommunicator().sendNormalServerMessage("The floor just resounds hollowly.", (byte)3);
                return;
            }
            int tile = Server.caveMesh.getTile(tilex, tiley);
            byte type = Tiles.decodeType(tile);
            boolean dis = true;
            if (Tiles.isReinforcedCave(type)) {
                double theSoulDepth = performer.getSoulDepth().getKnowledge();
                int theChance = (int) (100/theSoulDepth);
                if (Server.rand.nextInt(theChance) == 0) {
                    Server.setCaveResource(tilex, tiley, Server.rand.nextInt(100) + 50);
                    Server.caveMesh.setTile(tilex, tiley, Tiles.encode(Tiles.decodeHeight(tile), Tiles.Tile.TILE_CAVE_WALL.id, Tiles.decodeData(tile)));
                    Players.getInstance().sendChangedTile(tilex, tiley, false, false);
                    performer.getCommunicator().sendNormalServerMessage("You affect the vein.", (byte)2);
                    return;
                } else {
                    performer.getCommunicator().sendNormalServerMessage("You fail to find a weak spot to direct the power to. The wall still stands this time.", (byte)3);
                    dis = false;

                }
            }
            if (dis && TileRockBehaviour.createInsideTunnel(tilex, tiley, tile, performer, 145, dir, true, act)) {
                performer.getCommunicator().sendNormalServerMessage("You disintegrate the " + Tiles.getTile((byte)Tiles.decodeType((int)tile)).tiledesc.toLowerCase() + ".", (byte)2);
            }
        }
        catch (NoSuchActionException nsa) {
            performer.getCommunicator().sendNormalServerMessage("You fail to channel the spell. If this happens regurarly, talk to the gods.");
        }
    }
    }





