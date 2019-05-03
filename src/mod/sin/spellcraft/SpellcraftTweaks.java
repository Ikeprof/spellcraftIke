package mod.sin.spellcraft;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.spells.*;
import com.wurmonline.shared.constants.Enchants;
import javassist.*;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;

import javassist.bytecode.Descriptor;
import mod.sin.lib.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

public class SpellcraftTweaks {
    public static Logger logger = Logger.getLogger(SpellcraftTweaks.class.getName());

    protected static ArrayList<Byte> demiseEnchants = new ArrayList<>();
    protected static ArrayList<Byte> jewelryEnchants = new ArrayList<>();
    protected static void initializeSpellArrays(){
    	jewelryEnchants.add((byte) 1); // Toxin
		jewelryEnchants.add((byte) 2); // Blaze
		jewelryEnchants.add((byte) 3); // Glacial
		jewelryEnchants.add((byte) 4); // Corrosion
		jewelryEnchants.add((byte) 5); // Acid Protection
		jewelryEnchants.add((byte) 6); // Frost Protection
		jewelryEnchants.add((byte) 7); // Fire Protection
		jewelryEnchants.add((byte) 8); // Poison Protection
		jewelryEnchants.add((byte) 29); // Nolocate
		jewelryEnchants.add(SpellcraftSpell.ACUITY.getEnchant());
		jewelryEnchants.add(SpellcraftSpell.ENDURANCE.getEnchant());
		jewelryEnchants.add(SpellcraftSpell.INDUSTRY.getEnchant());
		jewelryEnchants.add(SpellcraftSpell.PROWESS.getEnchant());
        demiseEnchants.add((byte) 9); // Human's Demise
        demiseEnchants.add((byte) 10); // Selfhealer's Demise
        demiseEnchants.add((byte) 11); // Animal's Demise
        demiseEnchants.add((byte) 12); // Dragon's Demise
    }

    protected static boolean canSpellApplyItem(Spell spell, Item target){
        if(!spell.isReligiousSpell()){
            return false;
        }
        if(spell.isItemEnchantment()){
            byte enchant = spell.getEnchantment();
            // Check for negation first
			if(target.getBonusForSpellEffect(enchant) > 0){
				return true;
			}
            // Custom spells
            if(spell.getName().equals(SpellcraftSpell.EXPAND.getName())){
                return Expand.isValidContainer(target);
            }else if(spell.getName().equals(SpellcraftSpell.LABOURING_SPIRIT.getName())){
                return LabouringSpirit.isValidTarget(target);
            }else if(spell.getName().equals(SpellcraftSpell.QUARRY.getName())){
                return target.getTemplateId() == ItemList.pickAxe;
            }else if(spell.getName().equals(SpellcraftSpell.REPLENISH.getName())){
                return target.isContainerLiquid();
            }
            // Jewelery enchants
            if(!target.isEnchantableJewelry()) {
				if (jewelryEnchants.contains(spell.getEnchantment())) {
					return false;
				}
            }
            if(enchant == 48 || enchant == 49 || enchant == 50){ // Lurker enchants
                if(target.getTemplateId() != ItemList.pendulum){
                    return false;
                }
            }
            if(enchant == Enchants.BUFF_COURIER || enchant == Enchants.BUFF_DARKMESSENGER){
				if(!target.isMailBox() && !target.isSpringFilled() && !target.isPuppet() && !target.isUnenchantedTurret() && !target.isEnchantedTurret() || target.hasCourier() && !target.isEnchantedTurret()){
					return false;
				}else{
					return true;
				}
			}
            return Spell.mayBeEnchanted(target);
        }else{
            if(spell.getName().equals("Vessel")){
                if (target.isGem()) {
                    if(target.isSource() || target.getData1() > 0){
                        return false;
                    }
                }else{
                    return false;
                }
            }else if(spell.getName().equals("Break Altar")){
                if(!target.isDomainItem()){
                    return false;
                }
                if(target.isHugeAltar() && !Deities.mayDestroyAltars()){
                    return false;
                }
            }else if(spell.getName().equals("Sunder")){
                if(!Spell.mayBeEnchanted(target)){
                    return false;
                }
            }
        }
        return true;
    }
    public static Spell[] newGetSpellsTargettingItems(Creature performer, Deity deity, Item target){
        Spell[] spells = deity.getSpellsTargettingItems((int) performer.getFaith());
        if(performer.getPower() > 0 && SpellcraftMod.allSpellsGamemasters){
            spells = Spells.getSpellsTargettingItems();
            Arrays.sort(spells);
        }
        ArrayList<Spell> newSpellList = new ArrayList<>();
        for(Spell spell : spells){
            if(canSpellApplyItem(spell, target)){
                newSpellList.add(spell);
            }
        }
        return newSpellList.toArray(new Spell[0]);
    }

    public static boolean canSpellApplyTile(Spell spell, int tilex, int tiley){
        if(!spell.isTargetTile()){
            return false;
        }
        if(!spell.isReligiousSpell()){
            return false;
        }
        return true;
    }
    public static Spell[] newGetSpellsTargettingTiles(Creature performer, Deity deity, int tilex, int tiley){
        Spell[] spells = deity.getSpellsTargettingTiles((int) performer.getFaith());
        if(performer.getPower() > 0 && SpellcraftMod.allSpellsGamemasters){
            spells = Spells.getAllSpells();
            Arrays.sort(spells);
        }
        ArrayList<Spell> newSpellList = new ArrayList<>();
        for(Spell spell : spells){
            if(canSpellApplyTile(spell, tilex, tiley)){
                newSpellList.add(spell);
            }
        }
        return newSpellList.toArray(new Spell[0]);
    }

	public static void riteChanges(SpellcraftMod mod){
		try{
			ClassPool classPool = HookManager.getInstance().getClassPool();
			Class<SpellcraftTweaks> thisClass = SpellcraftTweaks.class;
			
			// - Holy Crop -
			CtClass ctHolyCrop = classPool.get("com.wurmonline.server.spells.HolyCrop");
			int defaultFavor = 100000;
			final int hcFavorChangePrecondition = defaultFavor-mod.riteHolyCropFavorReq;
			String replace = "$_ = $proceed($$)+"+String.valueOf(hcFavorChangePrecondition)+";";
			Util.setReason("Adjust Holy Crop favor cost");
			Util.instrumentDeclared(thisClass, ctHolyCrop, "precondition", "getFavor", replace);

	    	final int hcFavorChangeDoEffect = defaultFavor-mod.riteHolyCropFavorReq;
	    	replace = "$_ = $proceed($$)+"+String.valueOf(hcFavorChangeDoEffect)+";";
	    	Util.setReason("Adjust Holy Crop favor cost");
	    	Util.instrumentDeclared(thisClass, ctHolyCrop, "doEffect", "getFavor", replace);

	    	final int hcFavorCost = mod.riteHolyCropFavorCost;
	    	replace = "$_ = $proceed(Math.max(0, performer.getDeity().getFavor()-"+ hcFavorCost +"));";
	    	Util.setReason("Adjust Holy Crop favor cost");
	    	Util.instrumentDeclared(thisClass, ctHolyCrop, "doEffect", "setFavor", replace);

	    	if(mod.riteHolyCropMassGenesis){
	    		replace = "$_ = $proceed($$);"
                		+ "com.wurmonline.server.creatures.Creature[] allCreatures = com.wurmonline.server.creatures.Creatures.getInstance().getCreatures();"
                		+ "int i = 0;"
                		+ "while(i < allCreatures.length){"
                		+ "  if(allCreatures[i].isBred() && com.wurmonline.server.Server.rand.nextInt("+ mod.riteHolyCropGenesisChance +") == 0){"
                		+ "    allCreatures[i].getStatus().removeRandomNegativeTrait();"
                		+ "  }"
                		+ "  i++;"
                		+ "}";
	    		Util.setReason("Make Holy Crop apply a mass Genesis effect to the map");
	    		Util.instrumentDeclared(thisClass, ctHolyCrop, "doEffect", "addHistory", replace);
	    	}
	    	
	    	// - Rite of Death -
	    	CtClass ctRiteDeath = classPool.get("com.wurmonline.server.spells.RiteDeath");
	    	defaultFavor = 100000;
	    	final int rdFavorChangePrecondition = defaultFavor-mod.riteDeathFavorReq;
	    	replace = "$_ = $proceed()+"+ rdFavorChangePrecondition +";";
	    	Util.setReason("Adjust Rite of Death favor cost");
	    	Util.instrumentDeclared(thisClass, ctRiteDeath, "precondition", "getFavor", replace);

	    	final int rdFavorChangeDoEffect = defaultFavor-mod.riteDeathFavorReq;
	    	replace = "$_ = $proceed($$)+"+String.valueOf(rdFavorChangeDoEffect)+";";
	    	Util.setReason("Adjust Rite of Death favor cost");
	    	Util.instrumentDeclared(thisClass, ctRiteDeath, "doEffect", "getFavor", replace);

	    	final int rdFavorCost = mod.riteDeathFavorCost;
	    	replace = "$_ = $proceed(Math.max(0, performer.getDeity().getFavor()-"+ rdFavorCost +"));";
	    	Util.setReason("Adjust Rite of Death favor cost");
	    	Util.instrumentDeclared(thisClass, ctRiteDeath, "doEffect", "setFavor", replace);
	    	
	    	// - Rite of Spring -
	    	CtClass ctRiteSpring = classPool.get("com.wurmonline.server.spells.RiteSpring");
	    	defaultFavor = 1000;
	    	replace = "$_ = 1;";
	    	Util.setReason("Set getActiveFollowers to return 1, making Rite of Spring a flat 1000 default favor cost.");
	    	Util.instrumentDeclared(thisClass, ctRiteSpring, "precondition", "getActiveFollowers", replace);

	    	Util.setReason("Set getActiveFollowers to return 1, making Rite of Spring a flat 1000 default favor cost.");
	    	Util.instrumentDeclared(thisClass, ctRiteSpring, "doEffect", "getActiveFollowers", replace);

	    	final int riteSpringPlayersRequired = mod.riteSpringPlayersRequired;
	    	replace = "$_ = $proceed($1, Math.min("+ riteSpringPlayersRequired +", $2));";
	    	Util.setReason("Edit the premium player requirement to cap out at 5 for Rite of Spring.");
	    	Util.instrumentDeclared(thisClass, ctRiteSpring, "precondition", "max", replace);

	    	final int riteSpringFavorChangePrecondition = defaultFavor-mod.riteSpringFavorReq;
	    	replace = "$_ = $proceed()+"+ riteSpringFavorChangePrecondition +";";
	    	Util.setReason("Adjust Rite of Spring favor cost");
	    	Util.instrumentDeclared(thisClass, ctRiteSpring, "precondition", "getFavor", replace);

	    	final int riteSpringFavorChangeDoEffect = defaultFavor-mod.riteSpringFavorReq;
	    	replace = "$_ = $proceed($$)+"+String.valueOf(riteSpringFavorChangeDoEffect)+";";
	    	Util.setReason("Adjust Rite of Spring favor cost");
	    	Util.instrumentDeclared(thisClass, ctRiteSpring, "doEffect", "getFavor", replace);

	    	final int riteSpringFavorCost = mod.riteSpringFavorCost;
	    	replace = "$_ = $proceed(Math.max(0, performer.getDeity().getFavor()-"+ riteSpringFavorCost +"));";
	    	Util.setReason("Adjust Rite of Spring favor cost");
	    	Util.instrumentDeclared(thisClass, ctRiteSpring, "doEffect", "setFavor", replace);
	    	
	    	// Ritual of the Sun
	    	CtClass ctRitualSun = classPool.get("com.wurmonline.server.spells.RitualSun");
	    	defaultFavor = 100000;
	    	final int riteSunFavorChangePrecondition = defaultFavor-mod.riteSunFavorReq;
	    	replace = "$_ = $proceed()+"+ riteSunFavorChangePrecondition +";";
	    	Util.setReason("Adjust Ritual of the Sun favor cost");
	    	Util.instrumentDeclared(thisClass, ctRitualSun, "precondition", "getFavor", replace);

	    	final int riteSunFavorChangeDoEffect = defaultFavor-mod.riteSunFavorReq;
	    	replace = "$_ = $proceed($$)+"+String.valueOf(riteSunFavorChangeDoEffect)+";";
	    	Util.setReason("Adjust Ritual of the Sun favor cost");
	    	Util.instrumentDeclared(thisClass, ctRitualSun, "doEffect", "getFavor", replace);

	    	final int riteSunFavorCost = mod.riteSunFavorCost;
	    	replace = "$_ = $proceed(Math.max(0, performer.getDeity().getFavor()-"+ riteSunFavorCost +"));";
	    	Util.setReason("Adjust Ritual of the Sun favor cost");
	    	Util.instrumentDeclared(thisClass, ctRitualSun, "doEffect", "setFavor", replace);

	    	replace = "$_ = $proceed(0f, true);";
	    	Util.setReason("Make Ritual of the Sun do a full refresh.");
	    	Util.instrumentDeclared(thisClass, ctRitualSun, "doEffect", "refresh", replace);
	        
		} catch (NotFoundException e) {
			e.printStackTrace();
		}
	}
	public static void preInit(SpellcraftMod mod){
		try{
			ClassPool classPool = HookManager.getInstance().getClassPool();
			Class<SpellcraftTweaks> thisClass = SpellcraftTweaks.class;
			String replace;

			// - Set new maximum player faith -
			if(mod.maximumPlayerFaith != 100){
		        CtClass ctDbPlayerInfo = classPool.get("com.wurmonline.server.players.DbPlayerInfo");
		        replace = "if($1 == 100.0){"
                		+ "  $_ = $proceed("+String.valueOf(mod.maximumPlayerFaith)+".0D, (double)$2);"
                		+ "}else{"
                		+ "  $_ = $proceed($$);"
                		+ "}";
		        Util.setReason("Set new maximum player faith.");
		        Util.instrumentDeclared(thisClass, ctDbPlayerInfo, "setFaith", "min", replace);

		        // Uncap player favor to the new maximum faith.
		        replace = "if($1 == 100.0){"
                		+ "  $_ = $proceed("+String.valueOf(mod.maximumPlayerFaith)+".0D, (double)$2);"
                		+ "}else{"
                		+ "  $_ = $proceed($$);"
                		+ "}";
		        Util.setReason("Uncap player favor to the new maximum faith.");
		        Util.instrumentDeclared(thisClass, ctDbPlayerInfo, "setFavor", "min", replace);
			}
			
			// - Update prayer faith gains to scale to the new maximumFaith -
			if(mod.scalePrayerGains && mod.hourlyPrayer){
				CtClass ctPlayerInfo = classPool.get("com.wurmonline.server.players.PlayerInfo");
				replace = "$_ = $proceed(Math.min(3.0f, Math.max(0.001f, 3.0f*("+String.valueOf(mod.maximumPlayerFaith)+".0f - this.getFaith()) / (10.0f * Math.max(1.0f, this.getFaith())))));" +
						"this.lastFaith = System.currentTimeMillis() + 2400000;";
				Util.setReason("Scale prayer gains to the new maximum faith.");
				Util.instrumentDeclared(thisClass, ctPlayerInfo, "checkPrayerFaith", "modifyFaith", replace);
				Util.setReason("Unlock the maximum of 1 faith adjustment.");
				replace = "$_ = $proceed(3.0f, $2);";
				Util.instrumentDeclared(thisClass, ctPlayerInfo, "modifyFaith", "min", replace);
			}else if(mod.scalePrayerGains){
				CtClass ctPlayerInfo = classPool.get("com.wurmonline.server.players.PlayerInfo");
				replace = "$_ = $proceed(Math.min(1.0f, Math.max(0.001f, ("+String.valueOf(mod.maximumPlayerFaith)+".0f - this.getFaith()) / (10.0f * Math.max(1.0f, this.getFaith())))));";
				Util.setReason("Scale prayer gains to the new maximum faith.");
				Util.instrumentDeclared(thisClass, ctPlayerInfo, "checkPrayerFaith", "modifyFaith", replace);
			}

			// - Update favor regeneration -
			if(mod.newFavorRegen){
		        CtClass ctPlayer = classPool.get("com.wurmonline.server.players.Player");
		        replace = "this.pollFavor();"
                		+ "$_ = $proceed($$);";
		        Util.setReason("Adjust favor regeneration to scale to new faith limit.");
		        Util.instrumentDeclared(thisClass, ctPlayer, "poll", "pollFat", replace);

				Util.setReason("Adjust favor regeneration to scale to new faith limit.");
		        replace = "if($1 != this.saveFile.getFaith()){"
                		// CurrentFavor + lMod * max(100, (channelSkill+currentFaith)*2*[Title?1:2]) / max(1, currentFavor*30)
                		+ "  $_ = $proceed(this.saveFile.getFavor() + lMod * (Math.max(100.0f, (float)(this.getChannelingSkill().getKnowledge()+this.saveFile.getFaith())*2f*(com.wurmonline.server.kingdom.King.isOfficial(1501, this.getWurmId(), this.getKingdomId()) ? 2 : 1)) / (Math.max(1.0f, this.saveFile.getFavor()) * 300.0f)));"
                		+ "}else{"
                		+ "  $_ = $proceed($$);"
                		+ "}";
		        Util.instrumentDeclared(thisClass, ctPlayer, "pollFavor", "setFavor", replace);
			}
			
			// - Attempt to allow custom priest faith - //
			if(mod.priestFaithRequirement != 30){
		        CtClass ctHugeAltarBehaviour = classPool.get("com.wurmonline.server.behaviours.HugeAltarBehaviour");
		        String actionDescriptor = Descriptor.ofMethod(CtClass.booleanType, new CtClass[] {
		        		classPool.get("com.wurmonline.server.behaviours.Action"),
		        		classPool.get("com.wurmonline.server.creatures.Creature"),
		        		classPool.get("com.wurmonline.server.items.Item"),
		        		CtClass.shortType,
		        		CtClass.floatType});
		        replace = "if(performer.getFaith() >= "+String.valueOf(mod.priestFaithRequirement)+" && performer.getFaith() < 50){"
	            		+ "  $_ = 30.0f;"
	            		+ "}else{"
	            		+ "  $_ = $proceed($$);"
	            		+ "}";
		        Util.setReason("Change faith required to priest.");
		        Util.instrumentDeclared(thisClass, ctHugeAltarBehaviour, "getCommonBehaviours", "getFaith", replace);

		        Util.setReason("Change faith required to priest.");
		        Util.instrumentDescribed(thisClass, ctHugeAltarBehaviour, "action", actionDescriptor, "getFaith", replace);

		        CtClass ctMethodsCreatures = classPool.get("com.wurmonline.server.behaviours.MethodsCreatures");
		        Util.setReason("Change faith required to priest.");
		        Util.instrumentDeclared(thisClass, ctMethodsCreatures, "sendAskPriestQuestion", "getFaith", replace);

	            // - Fix de-priesting when gaining faith below 30 - //
				Util.setReason("Fix de-priesting when gaining faith below 30 as a priest.");
	            CtClass ctDbPlayerInfo = classPool.get("com.wurmonline.server.players.DbPlayerInfo");
	            replace = "if($2 == 20.0f && $1 < 30){"
                		+ "  $_ = $proceed(30.0f, lFaith);"
                		+ "}else{"
                		+ "  $_ = $proceed($$);"
                		+ "}";
	            Util.instrumentDeclared(thisClass, ctDbPlayerInfo, "setFaith", "min", replace);

				Util.setReason("Minor change for custom priest faith.");
	            replace = "$_ = $proceed(true);";
	            Util.instrumentDeclared(thisClass, ctDbPlayerInfo, "setFaith", "setPriest", replace);

				Util.setReason("Minor change for custom priest faith.");
	            replace = "$_ = null;";
	            Util.instrumentDeclared(thisClass, ctDbPlayerInfo, "setFaith", "sendAlertServerMessage", replace);
			}

			if(SpellcraftMod.onlyShowValidSpells){
			    // Initialize array first
                initializeSpellArrays();

                Util.setReason("Only show valid spells in the ItemBehaviour list.");
                CtClass ctItemBehaviour = classPool.get("com.wurmonline.server.behaviours.ItemBehaviour");
                CtMethod[] itemMethods = ctItemBehaviour.getDeclaredMethods("getBehavioursFor");
                for(CtMethod method : itemMethods){
                    try {
                        method.instrument(new ExprEditor() {
                            @Override
                            public void edit(MethodCall m) throws CannotCompileException {
                                if (m.getMethodName().equals("getSpellsTargettingItems")) {
                                    String replace = "$_ = "+SpellcraftTweaks.class.getName()+".newGetSpellsTargettingItems(performer, $0, target);";
                                    m.replace(replace);
                                    logger.info("Replaced getSpellsTargettingItems in getBehaviourFor to make spells only show for working targets.");
                                }
                            }
                        });
                    } catch (CannotCompileException e) {
                        e.printStackTrace();
                    }
                }

                Util.setReason("Only show valid spells in the ItemBehaviour list.");
                CtClass ctTileBehaviour = classPool.get("com.wurmonline.server.behaviours.TileBehaviour");
                replace = "$_ = "+SpellcraftTweaks.class.getName()+".newGetSpellsTargettingTiles(performer, $0, tilex, tiley);";
                Util.instrumentDeclared(thisClass, ctTileBehaviour, "getTileAndFloorBehavioursFor", "getSpellsTargettingTiles", replace);
			}

			if(SpellcraftMod.allSpellsGamemasters){
			    Util.setReason("Enable GM's to cast all spells.");
                CtClass ctAction = classPool.get("com.wurmonline.server.behaviours.Action");
                CtConstructor[] constructors = ctAction.getConstructors();
                for(CtConstructor constructor : constructors){
                    try {
                        constructor.instrument(new ExprEditor() {
                            @Override
                            public void edit(MethodCall m) throws CannotCompileException {
                                if (m.getMethodName().equals("hasSpell")) {
                                    String replace = "$_ = $proceed($$) || aPerformer.getPower() > 0;";
                                    m.replace(replace);
                                    logger.info("Replaced hasSpell in Action constructor to enable GM's to use all spells.");
                                }
                            }
                        });
                    } catch (CannotCompileException e) {
                        e.printStackTrace();
                    }
                }
            }
	        
		} catch (NotFoundException e) {
			e.printStackTrace();
		}
	}
}