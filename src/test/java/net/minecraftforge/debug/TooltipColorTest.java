package net.minecraftforge.debug;

import net.minecraft.init.Items;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.api.distmarker.Dist;

@Mod(modid = TooltipColorTest.MODID, name = "Tooltip Color Test", version = "0.1", clientSideOnly = true)
public class TooltipColorTest
{
    public static final String MODID = "tooltipcolortest";

    private static final boolean ENABLE = false;

    public TooltipColorTest()
    {
        if (ENABLE)
        {
            MinecraftForge.EVENT_BUS.register(this);
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void getTooltipColor(RenderTooltipEvent.Color event)
    {
        if (event.getStack().getItem() == Items.APPLE)
        {
            event.setBackground(0xF0510404);
            event.setBorderStart(0xF0bc0909);
            event.setBorderEnd(0xF03f0f0f);
        }
    }
}