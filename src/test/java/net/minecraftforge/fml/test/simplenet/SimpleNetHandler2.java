package net.minecraftforge.fml.test.simplenet;

import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.network.NetworkEvent;

public class SimpleNetHandler2 implements IMessageHandler<SimpleNetTestMessage2, SimpleNetTestMessage1>
{
    @Override
    public SimpleNetTestMessage1 onMessage(SimpleNetTestMessage2 message, NetworkEvent.Context context)
    {
        return null;
    }

}
