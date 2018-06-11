package net.minecraftforge.debug;

import net.minecraft.init.Blocks;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Mod(modid = BlockPlaceEventTest.MOD_ID, name = "BlockPlaceEvent test mod", version = "1.0", acceptableRemoteVersions = "*")
@Mod.EventBusSubscriber
public class BlockPlaceEventTest
{
    static final String MOD_ID = "block_place_event_test";
    static final boolean ENABLED = false;

    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void onBlockPlaced(BlockEvent.PlaceEvent event)
    {
        if (!ENABLED)
        {
            return;
        }

        if (event.getPlacedBlock().getBlock() == Blocks.CHEST
                && event.getPlacedAgainst().getBlock() != Blocks.DIAMOND_BLOCK)
        {
            event.setCanceled(true);
        }
    }
}
