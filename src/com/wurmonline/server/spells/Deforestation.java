package com.wurmonline.server.spells;

import com.wurmonline.mesh.FoliageAge;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.mesh.TreeData;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.zones.Zones;
import java.awt.geom.Ellipse2D;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.behaviours.ActionEntry;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

public class Deforestation extends ReligiousSpell {

    public Deforestation(SpellcraftSpell spell){
        super(spell.getName(), ModActions.getNextActionId(), spell.getCastTime(), spell.getCost(), spell.getDifficulty(), spell.getFaith(), spell.getCooldown());
        this.targetCreature = false;
        this.targetTile = true;
        this.offensive = false;
        this.healing = false;
        this.effectdesc = "a tree clearing spell.";
        this.description = "a whirlwind";

        ActionEntry actionEntry = ActionEntry.createEntry((short) number, name, "deforestation",
                new int[] {2,3 /* ACTION_TYPE_SPELL */, 36 /* ACTION_TYPE_ALWAYS_USE_ACTIVE_ITEM */,
                        48 /* ACTION_TYPE_ENEMY_ALWAYS */ ,Actions.ACTION_TYPE_IGNORERANGE});
        ModActions.registerAction(actionEntry);
    }


	
    @Override
    void doEffect(Skill castSkill, double power, Creature performer, int tilex, int tiley, int layer, int heightOffset) {
        performer.getCommunicator().sendNormalServerMessage("A powerful wind swirls around you");
        int sx = Zones.safeTileX(tilex - (int)Math.max(10.0, power / 10.0 + (double)performer.getNumLinks()));
        int sy = Zones.safeTileY(tiley - (int)Math.max(10.0, power / 10.0 + (double)performer.getNumLinks()));
        int ex = Zones.safeTileX(tilex + (int)Math.max(10.0, power / 10.0 + (double)performer.getNumLinks()));
        int ey = Zones.safeTileY(tiley + (int)Math.max(10.0, power / 10.0 + (double)performer.getNumLinks()));
        Ellipse2D.Float circle = new Ellipse2D.Float(sx, sy, ex - sx, ey - sy);
        for (int x = sx; x < ex; ++x) {
            block7 : for (int y = sy; y < ey; ++y) {
                if (!circle.contains(x, y)) continue;
                
                int tile = Server.surfaceMesh.getTile(x, y);
                byte type = Tiles.decodeType(tile);
                Tiles.Tile theTile = Tiles.getTile(type);
                
                byte data = Tiles.decodeData(tile);

                if (theTile.isNormalTree() || theTile.isMyceliumTree() || theTile.isBush())  {
                    byte treeAge = FoliageAge.getAgeAsByte(data);
                    TreeData.TreeType treeType = theTile.getTreeType(data);
                    if (true) {
                        byte newt = Tiles.Tile.TILE_GRASS.id;

                        Server.setSurfaceTile(x, y, Tiles.decodeHeight(tile), newt, (byte)0);
                        Server.setWorldResource(x, y, 0);
                        int templateId = 9;
                        if (treeAge >= FoliageAge.OLD_ONE.getAgeId() && treeAge < FoliageAge.SHRIVELLED.getAgeId()) {
                            templateId = 385;
                        }
                        double sizeMod = (double)treeAge / 4.0;
                        if (!treeType.isFruitTree()) {
                            sizeMod *= 0.25;
                        }
                        double lNewRotation = Math.atan2((y << 2) + 2 - ((y << 2) + 2), (x << 2) + 2 - ((x << 2) + 2));
                        float rot = (float)(lNewRotation * 57.29577951308232);
                        try {
                            if (!theTile.isBush() && !treeType.isFruitTree()){
                            Item newItem = ItemFactory.createItem(templateId, (float)power / 5.0f, x * 4 + Server.rand.nextInt(4), y * 4 + Server.rand.nextInt(4), rot, performer.isOnSurface(), treeType.getMaterial(), (byte)0, -10L, null, treeAge);
                            newItem.setWeight((int)Math.max(1000.0, sizeMod * (double)newItem.getWeightGrams()), true);
                            newItem.setLastOwnerId(performer.getWurmId());
                           
                            }
                        }
                        catch (Exception newItem) {
                            // empty catch block
                        }
                        Players.getInstance().sendChangedTile(x, y, true, false);
                    }
                }
            }
        }
    }

    @Override
    void doNegativeEffect(Skill castSkill, double power, Creature performer, Item target) {
        this.checkDestroyItem(power, performer, target);
    }
}
