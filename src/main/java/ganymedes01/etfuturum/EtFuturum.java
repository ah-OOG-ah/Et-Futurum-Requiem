package ganymedes01.etfuturum;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLConstructionEvent;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms.IMCEvent;
import cpw.mods.fml.common.event.FMLInterModComms.IMCMessage;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ganymedes01.etfuturum.api.waila.WailaRegistrar;
import ganymedes01.etfuturum.client.sound.ModSounds;
import ganymedes01.etfuturum.configuration.ConfigBase;
import ganymedes01.etfuturum.configuration.configs.ConfigBlocksItems;
import ganymedes01.etfuturum.configuration.configs.ConfigFunctions;
import ganymedes01.etfuturum.configuration.configs.ConfigWorld;
import ganymedes01.etfuturum.core.proxy.CommonProxy;
import ganymedes01.etfuturum.core.utils.BrewingFuelRegistry;
import ganymedes01.etfuturum.core.utils.DeepslateOreRegistry;
import ganymedes01.etfuturum.core.utils.HoeHelper;
import ganymedes01.etfuturum.core.utils.RawOreRegistry;
import ganymedes01.etfuturum.core.utils.StrippedLogRegistry;
import ganymedes01.etfuturum.entities.ModEntityList;
import ganymedes01.etfuturum.lib.Reference;
import ganymedes01.etfuturum.network.ArmourStandInteractHandler;
import ganymedes01.etfuturum.network.ArmourStandInteractMessage;
import ganymedes01.etfuturum.network.BlackHeartParticlesHandler;
import ganymedes01.etfuturum.network.BlackHeartParticlesMessage;
import ganymedes01.etfuturum.network.WoodSignOpenHandler;
import ganymedes01.etfuturum.network.WoodSignOpenMessage;
import ganymedes01.etfuturum.potion.ModPotions;
import ganymedes01.etfuturum.recipes.BlastFurnaceRecipes;
import ganymedes01.etfuturum.recipes.ModRecipes;
import ganymedes01.etfuturum.recipes.SmokerRecipes;
import ganymedes01.etfuturum.world.EtFuturumLateWorldGenerator;
import ganymedes01.etfuturum.world.EtFuturumWorldGenerator;
import ganymedes01.etfuturum.world.end.dimension.DimensionProviderEnd;
import ganymedes01.etfuturum.world.structure.OceanMonument;
import makamys.mclib.core.MCLib;
import makamys.mclib.ext.assetdirector.ADConfig;
import makamys.mclib.ext.assetdirector.AssetDirectorAPI;
import net.minecraft.block.Block;
import net.minecraft.block.Block.SoundType;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockHay;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockNetherWart;
import net.minecraft.block.BlockOre;
import net.minecraft.block.BlockSponge;
import net.minecraft.block.BlockStem;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.BlockVine;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.oredict.OreDictionary;

@Mod(
		modid = Reference.MOD_ID, 
		name = Reference.MOD_NAME, 
		version = Reference.VERSION_NUMBER, 
		dependencies = Reference.DEPENDENCIES, 
		guiFactory = "ganymedes01.etfuturum.configuration.ConfigGuiFactory"
	)

public class EtFuturum {

	@Instance("etfuturum")
	public static EtFuturum instance;

	@SidedProxy(clientSide = "ganymedes01.etfuturum.core.proxy.ClientProxy", serverSide = "ganymedes01.etfuturum.core.proxy.CommonProxy")
	public static CommonProxy proxy;

	public static SimpleNetworkWrapper networkWrapper;

	public static CreativeTabs creativeTabItems = new CreativeTabs(Reference.MOD_ID + ".items") {
		@Override
		public Item getTabIconItem() {
			return ConfigBlocksItems.enableNetherite ? ModItems.netherite_scrap : ConfigBlocksItems.enablePrismarine ? ModItems.prismarine_shard : Items.magma_cream;
		}
		
		@SideOnly(Side.CLIENT)
		@Override
		public void displayAllReleventItems(List p_78018_1_)
		{
			for(int i : ModEntityList.eggIDs)
				p_78018_1_.add(new ItemStack(Items.spawn_egg, 1, i));
			super.displayAllReleventItems(p_78018_1_);
		}
	};
	
	public static CreativeTabs creativeTabBlocks = new CreativeTabs(Reference.MOD_ID + ".blocks") {
		@Override
		public Item getTabIconItem() {
			return ConfigBlocksItems.enableSmoker ? Item.getItemFromBlock(ModBlocks.smoker) : ConfigBlocksItems.enableChorusFruit ? Item.getItemFromBlock(ModBlocks.chorus_flower) : Item.getItemFromBlock(Blocks.ender_chest);
		}
	};
	
	public static boolean netherAmbienceNetherlicious;
	public static boolean netherMusicNetherlicious;
	
	public static boolean hasIronChest;
	public static boolean hasNetherlicious;
	public static boolean hasEnderlicious;
	public static final boolean TESTING = Reference.VERSION_NUMBER.equals("@VERSION@");
	public static final boolean IS_CI_BUILD = Reference.VERSION_NUMBER.toLowerCase().contains("snapshot");
	
	static final Map<Item, Integer> DEFAULT_COMPOST_CHANCES = new HashMap<Item, Integer>();
	
	@EventHandler
	public void onConstruction(FMLConstructionEvent event) {
		MCLib.init();
		
		ADConfig config = new ADConfig();
		String ver = "1.18";
		
		config.addObject(ver, "minecraft/sounds/ambient/cave/cave14.ogg");
		config.addObject(ver, "minecraft/sounds/ambient/cave/cave15.ogg");
		config.addObject(ver, "minecraft/sounds/ambient/cave/cave16.ogg");
		config.addObject(ver, "minecraft/sounds/ambient/cave/cave17.ogg");
		config.addObject(ver, "minecraft/sounds/ambient/cave/cave18.ogg");
		config.addObject(ver, "minecraft/sounds/ambient/cave/cave19.ogg");
		
		config.addSoundEvent(ver, "weather.rain", "weather");
		config.addSoundEvent(ver, "weather.rain.above", "weather");
		
		config.addSoundEvent(ver, "music.nether.nether_wastes", "music");
		
		config.addSoundEvent(ver, "ambient.nether_wastes.additions", "ambient");
		config.addSoundEvent(ver, "ambient.nether_wastes.loop", "ambient");
		config.addSoundEvent(ver, "ambient.nether_wastes.mood", "ambient");
		
		config.addSoundEvent(ver, "music_disc.pigstep", "record");
		config.addSoundEvent(ver, "music_disc.otherside", "record");
		
		config.addSoundEvent(ver, "entity.boat.paddle_land", "player");
		config.addSoundEvent(ver, "entity.boat.paddle_water", "player");
		config.addSoundEvent(ver, "entity.rabbit.ambient", "neutral");
		config.addSoundEvent(ver, "entity.rabbit.jump", "neutral");
		config.addSoundEvent(ver, "entity.rabbit.attack", "neutral");
		config.addSoundEvent(ver, "entity.rabbit.hurt", "neutral");
		config.addSoundEvent(ver, "entity.rabbit.death", "neutral");
		config.addSoundEvent(ver, "entity.zombie_villager.ambient", "hostile");
		config.addSoundEvent(ver, "entity.zombie_villager.step", "hostile");
		config.addSoundEvent(ver, "entity.zombie_villager.hurt", "hostile");
		config.addSoundEvent(ver, "entity.zombie_villager.death", "hostile");
		config.addSoundEvent(ver, "entity.husk.ambient", "hostile");
		config.addSoundEvent(ver, "entity.husk.step", "hostile");
		config.addSoundEvent(ver, "entity.husk.hurt", "hostile");
		config.addSoundEvent(ver, "entity.husk.death", "hostile");
		config.addSoundEvent(ver, "entity.zombie.converted_to_drowned", "hostile");
		config.addSoundEvent(ver, "entity.husk.converted_to_zombie", "hostile");
		config.addSoundEvent(ver, "entity.stray.ambient", "hostile");
		config.addSoundEvent(ver, "entity.stray.step", "hostile");
		config.addSoundEvent(ver, "entity.stray.hurt", "hostile");
		config.addSoundEvent(ver, "entity.stray.death", "hostile");
		config.addSoundEvent(ver, "entity.skeleton.converted_to_stray", "hostile");
		config.addSoundEvent(ver, "entity.shulker_bullet.hurt", "hostile");
		config.addSoundEvent(ver, "entity.shulker_bullet.hit", "hostile");
		config.addSoundEvent(ver, "entity.shulker.ambient", "hostile");
		config.addSoundEvent(ver, "entity.shulker.open", "hostile");
		config.addSoundEvent(ver, "entity.shulker.close", "hostile");
		config.addSoundEvent(ver, "entity.shulker.shoot", "hostile");
		config.addSoundEvent(ver, "entity.shulker.hurt", "hostile");
		config.addSoundEvent(ver, "entity.shulker.hurt_closed", "hostile");
		config.addSoundEvent(ver, "entity.shulker.death", "hostile");
		config.addSoundEvent(ver, "entity.shulker.teleport", "hostile");
		
		config.addSoundEvent(ver, "item.axe.scrape", "player");
		config.addSoundEvent(ver, "item.axe.wax_off", "player");
		config.addSoundEvent(ver, "item.axe.strip", "player");
		config.addSoundEvent(ver, "item.honeycomb.wax_on", "player");
		config.addSoundEvent(ver, "item.totem.use", "player");
		config.addSoundEvent(ver, "item.shovel.flatten", "player");
		config.addSoundEvent(ver, "item.chorus_fruit.teleport", "player");
		
		config.addSoundEvent(ver, "block.barrel.open", "block");
		config.addSoundEvent(ver, "block.barrel.close", "block");
		config.addSoundEvent(ver, "block.chorus_flower.grow", "block");
		config.addSoundEvent(ver, "block.chorus_flower.death", "block");
		config.addSoundEvent(ver, "block.end_portal.spawn", "ambient");
		config.addSoundEvent(ver, "block.end_portal_frame.fill", "block");
		config.addSoundEvent(ver, "block.shulker_box.open", "block");
		config.addSoundEvent(ver, "block.shulker_box.close", "block");
		config.addSoundEvent(ver, "block.sweet_berry_bush.pick_berries", "player");
		
		config.addSoundEvent(ver, "block.brewing_stand.brew", "block");
		config.addSoundEvent(ver, "block.furnace.fire_crackle", "block");
		config.addSoundEvent(ver, "block.blastfurnace.fire_crackle", "block");
		config.addSoundEvent(ver, "block.smoker.smoke", "block");
		config.addSoundEvent(ver, "block.chest.close", "block");
		config.addSoundEvent(ver, "block.ender_chest.open", "block");
		config.addSoundEvent(ver, "block.ender_chest.close", "block");
		config.addSoundEvent(ver, "block.wooden_door.open", "block");
		config.addSoundEvent(ver, "block.wooden_door.close", "block");
		config.addSoundEvent(ver, "block.iron_door.open", "block");
		config.addSoundEvent(ver, "block.iron_door.close", "block");
		config.addSoundEvent(ver, "block.wooden_trapdoor.open", "block");
		config.addSoundEvent(ver, "block.wooden_trapdoor.close", "block");
		config.addSoundEvent(ver, "block.iron_trapdoor.open", "block");
		config.addSoundEvent(ver, "block.iron_trapdoor.close", "block");
		config.addSoundEvent(ver, "block.fence_gate.open", "block");
		config.addSoundEvent(ver, "block.fence_gate.close", "block");
		config.addSoundEvent(ver, "block.composter.empty", "block");
		config.addSoundEvent(ver, "block.composter.fill", "block");
		config.addSoundEvent(ver, "block.composter.fill_success", "block");
		config.addSoundEvent(ver, "block.composter.ready", "block");
	
		config.addSoundEvent(ver, "item.crop.plant", "block");
		config.addSoundEvent(ver, "block.crop.break", "block");
		config.addSoundEvent(ver, "item.nether_wart.plant", "block");
		config.addSoundEvent(ver, "block.nether_wart.break", "block");
		config.addSoundEvent(ver, "block.lantern.step", "block");
		config.addSoundEvent(ver, "block.lantern.break", "block");
		config.addSoundEvent(ver, "block.lantern.place", "block");
		config.addSoundEvent(ver, "block.deepslate.step", "block");
		config.addSoundEvent(ver, "block.deepslate.break", "block");
		config.addSoundEvent(ver, "block.deepslate.place", "block");
		config.addSoundEvent(ver, "block.sweet_berry_bush.break", "block");
		config.addSoundEvent(ver, "block.sweet_berry_bush.place", "block");
		config.addSoundEvent(ver, "block.deepslate_bricks.step", "block");
		config.addSoundEvent(ver, "block.deepslate_bricks.break", "block");
		config.addSoundEvent(ver, "block.soul_sand.step", "block");
		config.addSoundEvent(ver, "block.soul_sand.break", "block");
		config.addSoundEvent(ver, "block.wart_block.step", "block");
		config.addSoundEvent(ver, "block.wart_block.break", "block");
		config.addSoundEvent(ver, "block.nether_bricks.step", "block");
		config.addSoundEvent(ver, "block.nether_bricks.break", "block");
		config.addSoundEvent(ver, "block.bone_block.step", "block");
		config.addSoundEvent(ver, "block.bone_block.break", "block");
		config.addSoundEvent(ver, "block.netherrack.step", "block");
		config.addSoundEvent(ver, "block.netherrack.break", "block");
		config.addSoundEvent(ver, "block.nether_ore.step", "block");
		config.addSoundEvent(ver, "block.nether_ore.break", "block");
		config.addSoundEvent(ver, "block.ancient_debris.step", "block");
		config.addSoundEvent(ver, "block.ancient_debris.break", "block");
		config.addSoundEvent(ver, "block.netherite_block.step", "block");
		config.addSoundEvent(ver, "block.netherite_block.break", "block");
		config.addSoundEvent(ver, "block.basalt.step", "block");
		config.addSoundEvent(ver, "block.basalt.break", "block");
		config.addSoundEvent(ver, "block.copper.step", "block");
		config.addSoundEvent(ver, "block.copper.break", "block");
		config.addSoundEvent(ver, "block.tuff.step", "block");
		config.addSoundEvent(ver, "block.tuff.break", "block");
		config.addSoundEvent(ver, "block.vine.step", "block");
		config.addSoundEvent(ver, "block.vine.break", "block");
//		config.addSoundEvent(ver, "block.nylium.step", "block");
//		config.addSoundEvent(ver, "block.nylium.break", "block");
//		config.addSoundEvent(ver, "block.fungus.step", "block");
//		config.addSoundEvent(ver, "block.fungus.break", "block");
//		config.addSoundEvent(ver, "block.stem.step", "block");
//		config.addSoundEvent(ver, "block.stem.break", "block");
//		config.addSoundEvent(ver, "block.shroomlight.step", "block");
//		config.addSoundEvent(ver, "block.shroomlight.break", "block");
//		config.addSoundEvent(ver, "block.honey_block.step", "block");
//		config.addSoundEvent(ver, "block.honey_block.break", "block");
		
		AssetDirectorAPI.register(config);
	}
	
	static final String NETHER_FORTRESS = "netherFortress";
	private Field fortressWeightedField;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {

	    try {
	        Field chestInfo = ChestGenHooks.class.getDeclaredField("chestInfo");
	        chestInfo.setAccessible(true);
	        if(!((HashMap<String, ChestGenHooks>)chestInfo.get(null)).containsKey(NETHER_FORTRESS)) {
		        fortressWeightedField = Class.forName("net.minecraft.world.gen.structure.StructureNetherBridgePieces$Piece").getDeclaredField("field_111019_a");
		        fortressWeightedField.setAccessible(true);
		        Field modifiersField = Field.class.getDeclaredField("modifiers");
		        modifiersField.setAccessible(true);
		        modifiersField.setInt(fortressWeightedField, fortressWeightedField.getModifiers() & ~Modifier.FINAL);
		        ((HashMap<String, ChestGenHooks>)chestInfo.get(null)).put(NETHER_FORTRESS, new ChestGenHooks(NETHER_FORTRESS, (WeightedRandomChestContent[]) fortressWeightedField.get(null), 2, 5));
	        }
        } catch (Exception e) {
	        System.out.println("Failed to get Nether fortress loot table:");
	        e.printStackTrace();
        }
		
		hasIronChest = Loader.isModLoaded("IronChest");
		hasNetherlicious = Loader.isModLoaded("netherlicious");
		hasEnderlicious = Loader.isModLoaded("enderlicious");
		
		ModBlocks.init();
		ModItems.init();
		ModEnchantments.init();
		ModPotions.init();
		
//      if(ConfigurationHandler.enableNewNether) {
//          NetherBiomeManager.init(); // Come back to
//      }

		GameRegistry.registerWorldGenerator(EtFuturumWorldGenerator.INSTANCE, 0);
		GameRegistry.registerWorldGenerator(EtFuturumLateWorldGenerator.INSTANCE, Integer.MAX_VALUE);
		
		OceanMonument.makeMap();

		NetworkRegistry.INSTANCE.registerGuiHandler(instance, proxy);
		networkWrapper = NetworkRegistry.INSTANCE.newSimpleChannel(Reference.MOD_ID);
		networkWrapper.registerMessage(ArmourStandInteractHandler.class, ArmourStandInteractMessage.class, 0, Side.SERVER);
		networkWrapper.registerMessage(BlackHeartParticlesHandler.class, BlackHeartParticlesMessage.class, 1, Side.CLIENT);
		networkWrapper.registerMessage(WoodSignOpenHandler.class, WoodSignOpenMessage.class, 3, Side.CLIENT);   
		{
			if (Loader.isModLoaded("netherlicious")) {
				File file = new File(event.getModConfigurationDirectory() + "/Netherlicious/Biome_Sound_Configuration.cfg");
				if(file.exists()) {
					Configuration netherliciousSoundConfig = new Configuration(file);
					netherAmbienceNetherlicious = netherliciousSoundConfig.get("1 nether ambience", "Allow Biome specific sounds to play", true).getBoolean();
					netherMusicNetherlicious = netherliciousSoundConfig.get("2 biome music", "1 Replace the Music System in the Nether, to allow Biome specific Music. Default Music will still play sometimes", true).getBoolean();
				}
			}
		}
		
		//Define mod data here instead of in mcmod.info, adapted from Village Names.
		//Thanks AstroTibs!
		//Updated by Makamys to use mcmod.info again but also have color values without glitches.
		
		event.getModMetadata().autogenerated = false; // stops it from complaining about missing mcmod.info
		
		event.getModMetadata().name = "\u00a75\u00a7o" + Reference.MOD_NAME; // name 
		
		event.getModMetadata().version = "\u00a7e" + Reference.VERSION_NUMBER; // version 
		
		event.getModMetadata().credits = Reference.CREDITS; // credits 
		
		event.getModMetadata().authorList.clear();
		event.getModMetadata().authorList.addAll(Arrays.asList(Reference.AUTHOR_LIST)); // authorList - added as a list
		
		event.getModMetadata().url = Reference.MOD_URL;
		
		event.getModMetadata().description = Reference.DESCRIPTION; // description
				
		
		event.getModMetadata().logoFile = Reference.LOGO_FILE;
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		ModRecipes.init();
		
		if(Loader.isModLoaded("Waila")) {
			WailaRegistrar.register();
		}
		
		proxy.registerEvents();
		proxy.registerEntities();
		proxy.registerRenderers();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {

		for(BiomeGenBase biome : BiomeGenBase.getBiomeGenArray()) {
			if(biome == null)
				continue;
			//TODO: Change to bone meal event because the description of addFlower is a literal lie, as it affects worldgen too!
			Type[] biomeList = BiomeDictionary.getTypesForBiome(biome);
				if(biome.biomeID == 132 || (ArrayUtils.contains(biomeList, Type.PLAINS) && !ArrayUtils.contains(biomeList, Type.SNOWY) && !ArrayUtils.contains(biomeList, Type.SAVANNA)))
					biome.addFlower(ModBlocks.cornflower, 0, 12);
				if(biome.biomeID == 132)
					biome.addFlower(ModBlocks.lily_of_the_valley, 0, 5);
		}

		if (ConfigFunctions.enableUpdatedFoodValues) {
			((ItemFood)Items.carrot).healAmount = 3;
			((ItemFood)Items.baked_potato).healAmount = 5;
		}

		if (ConfigFunctions.enableUpdatedHarvestLevels) {
			Blocks.packed_ice.setHarvestLevel("pickaxe", 0);
			Blocks.ladder.setHarvestLevel("axe", 0);
			Blocks.melon_block.setHarvestLevel("axe", 0);
		}
		
		if(ConfigFunctions.enableFloatingTrapDoors) {
			BlockTrapDoor.disableValidation = true;
		}
		
		if (Loader.isModLoaded("Thaumcraft")) {
			CompatTC.doAspects();
		}
		
		Items.blaze_rod.setFull3D();
		Blocks.trapped_chest.setCreativeTab(CreativeTabs.tabRedstone);
	    
		if(ConfigBlocksItems.enableOtherside) {
		    ChestGenHooks.addItem(ChestGenHooks.STRONGHOLD_CORRIDOR, new WeightedRandomChestContent(ModItems.otherside_record, 0, 1, 1, 1));
		    ChestGenHooks.addItem(ChestGenHooks.DUNGEON_CHEST, new WeightedRandomChestContent(ModItems.otherside_record, 0, 1, 1, 1));
		}
		
		if(ConfigBlocksItems.enablePigstep) {
			ChestGenHooks.addItem(NETHER_FORTRESS, new WeightedRandomChestContent(ModItems.pigstep_record, 0, 1, 1, 5));
			
			if(fortressWeightedField != null) {
				try {
				    Field contents = ChestGenHooks.class.getDeclaredField("contents");
				    contents.setAccessible(true);
			        ArrayList<WeightedRandomChestContent> fortressContentList;
					fortressContentList = (ArrayList<WeightedRandomChestContent>)contents.get(ChestGenHooks.getInfo("netherFortress"));
					if(!fortressContentList.isEmpty()) {
				        WeightedRandomChestContent[] fortressChest = new WeightedRandomChestContent[fortressContentList.size()];
				        for (int i = 0; i < fortressContentList.size(); i++) {
				          fortressChest[i] = fortressContentList.get(i); 
				        }
						fortressWeightedField.set(null, fortressChest);
					}
				} catch (Exception e) {
			        System.out.println("Failed to fill Nether fortress loot table:");
					e.printStackTrace();
				}
			}
		}
	}
	
	@EventHandler
	public void onLoadComplete(FMLLoadCompleteEvent e){
		DeepslateOreRegistry.init();
		StrippedLogRegistry.init();
		RawOreRegistry.init();
		SmokerRecipes.init();
		BlastFurnaceRecipes.init();
		ConfigBase.postInit();
		
		//Block registry iterator
		Iterator<Block> iterator = Block.blockRegistry.iterator();
		while(iterator.hasNext()) {
			Block block = iterator.next();

			if(ConfigFunctions.enableHoeMining) {
				/*
				 * HOE MINING
				 */
				if(block instanceof BlockLeaves || block instanceof BlockHay || block instanceof BlockSponge || block instanceof BlockNetherWart) {
					HoeHelper.addToHoeArray(block);
				}
			}

			if(ConfigWorld.enableNewBlocksSounds) {
				/*
				 * SOUNDS
				 */
				String blockID = Block.blockRegistry.getNameForObject(block).split(":")[1].toLowerCase();
				
				SoundType sound = getCustomStepSound(block, blockID);
				if(sound != null) {
					block.setStepSound(sound);
				}
			}
			
			/*
			 * MATERIALS
			 */
			if(block == Blocks.bed && TESTING) {
				block.blockMaterial = Material.wood;
			}
		}
		
//      if(ConfigurationHandler.enableNewNether)
//        DimensionProviderNether.init(); // Come back to
		
		if(TESTING) {
			DimensionProviderEnd.init(); // Come back to
		}
	}
	
	public SoundType getCustomStepSound(Block block, String namespace) {
		
		if(block.stepSound == Block.soundTypePiston || block.stepSound == Block.soundTypeStone) {
			
			if(namespace.contains("nether") && namespace.contains("brick")) {
				return ModSounds.soundNetherBricks;
			}
			
			else if(namespace.contains("netherrack") || namespace.contains("hellfish")) {
					return ModSounds.soundNetherrack;
			}
			
			else if(block == Blocks.quartz_ore || (namespace.contains("nether") && (block instanceof BlockOre || namespace.contains("ore")))) {
				return ModSounds.soundNetherOre;
			}
			
			else if(namespace.contains("deepslate")) {
				return namespace.contains("brick") ? ModSounds.soundDeepslateBricks : ModSounds.soundDeepslate;
			}
			
			else if(block instanceof BlockNetherWart || (namespace.contains("nether") && namespace.contains("wart"))) {
				return ModSounds.soundCropWarts;
			}
			
			else if(namespace.contains("bone") || namespace.contains("ivory")) {
				return ModSounds.soundBoneBlock;
			}
			
		}
		
		if(block instanceof BlockCrops || block instanceof BlockStem) {
			return ModSounds.soundCrops;
		}
		
		if(block.stepSound == Block.soundTypeGrass && block instanceof BlockVine) {
			return ModSounds.soundVines;
		}
		
		if(block.stepSound == Block.soundTypeSand && namespace.contains("soul") && namespace.contains("sand")) {
			return ModSounds.soundSoulSand;
		}
		
		if(block.stepSound == Block.soundTypeMetal && (namespace.contains("copper") || namespace.contains("tin"))) {
			return ModSounds.soundCopper;
		}
		
		return null;
	}

	@EventHandler
	public void processIMCRequests(IMCEvent event) {
		for (IMCMessage message : event.getMessages())
			if (message.key.equals("register-brewing-fuel")) {
				NBTTagCompound nbt = message.getNBTValue();
				ItemStack stack = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("Fuel"));
				int brews = nbt.getInteger("Brews");
				BrewingFuelRegistry.registerFuel(stack, brews);
			}
	}

	@EventHandler
	public void serverStarting(FMLServerStartingEvent event) {
//        if (ConfigurationHandler.enablePlayerSkinOverlay)
//            event.registerServerCommand(new SetPlayerModelCommand());
	}
	
	public static void copyAttribs(Block to, Block from) {
		to.setHardness(from.blockHardness);
		to.setResistance(from.blockResistance);
		to.setBlockName(from.getUnlocalizedName().replace("tile.", ""));
		to.setCreativeTab(from.displayOnCreativeTab);
		to.setStepSound(from.stepSound);
		to.setBlockTextureName(from.textureName);
		//We do this because Forge methods cannot be Access Transformed
		for(int i = 0; i < 16; i++) {
			String tool = from.getHarvestTool(i);
			int level = from.getHarvestLevel(i);
			to.setHarvestLevel(tool, level, i);
		}
	}
	
	public static List<String> getOreStrings(ItemStack stack) {
		final List<String> list = new ArrayList();
		if(stack != null) {
			for(int oreID : OreDictionary.getOreIDs(stack)) {
				list.add(OreDictionary.getOreName(oreID));
			}
		}
		return list;
	}
	
	public static boolean hasDictTag(ItemStack stack, String... tags) {
		for(String oreName : getOreStrings(stack)) {
			if(ArrayUtils.contains(tags, oreName)) {
				return true;
			}
		}
		return false;
	}
	
	public static PotionEffect getSuspiciousStewEffect(ItemStack stack) {
		
		if(stack == null)
			return null;

		Item item = stack.getItem();

		if(item == Item.getItemFromBlock(Blocks.red_flower)) {
			switch(stack.getItemDamage()) {
			default:
			case 0:
				return new PotionEffect(Potion.nightVision.id, 100, 0);
			case 1:
				return new PotionEffect(Potion.field_76443_y.id, 7, 0);
			case 2:
				return new PotionEffect(Potion.fireResistance.id, 80, 0);
			case 3:
				return new PotionEffect(Potion.blindness.id, 160, 0);
			case 4:
			case 5:
			case 6:
			case 7:
				return new PotionEffect(Potion.weakness.id, 180, 0);
			case 8:
				return new PotionEffect(Potion.regeneration.id, 160, 0);
			}
		}

		if(item == Item.getItemFromBlock(Blocks.yellow_flower)) {
			return new PotionEffect(Potion.field_76443_y.id, 7, 0);
		}

		if(item == Item.getItemFromBlock(ModBlocks.cornflower)) {
			return new PotionEffect(Potion.jump.id, 120, 0);
		}

		if(item == Item.getItemFromBlock(ModBlocks.lily_of_the_valley)) {
			return new PotionEffect(Potion.poison.id, 240, 0);
		}

		if(item == Item.getItemFromBlock(ModBlocks.wither_rose)) {
			return new PotionEffect(Potion.wither.id, 160, 0);
		}
		return null;
	}

}
