package net.minecraftforge.fml.test.simplenet;

import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.network.NetworkEvent;

public class SimpleNetHandler1 implements IMessageHandler<SimpleNetTestMessage1, SimpleNetTestMessage2>
{
    @Override
    public SimpleNetTestMessage2 onMessage(SimpleNetTestMessage1 message, NetworkEvent.Context context)
    {
        return null;
    }

}
