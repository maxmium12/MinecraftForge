/*
 * Minecraft Forge
 * Copyright (c) 2018.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.minecraftforge.fml.network.simple;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectArrayMap;
import net.minecraft.network.PacketBuffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class IndexedMessageCodec
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Marker SIMPLENET = MarkerManager.getMarker("SIMPLENET");
    private final Short2ObjectArrayMap<CodecIndex<?>> indicies = new Short2ObjectArrayMap<>();
    private final Object2ObjectArrayMap<Class<?>, CodecIndex<?>> types = new Object2ObjectArrayMap<>();

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public class CodecIndex<MSG>
    {

        private final Optional<BiConsumer<MSG, PacketBuffer>> encoder;
        private final Optional<Function<PacketBuffer, MSG>> decoder;
        private final int index;
        private final BiConsumer<MSG,Supplier<MessageContext>> messageConsumer;
        private final Class<MSG> messageType;
        public CodecIndex(int index, Class<MSG> messageType, BiConsumer<MSG, PacketBuffer> encoder, Function<PacketBuffer, MSG> decoder, BiConsumer<MSG, Supplier<MessageContext>> messageConsumer)
        {
            this.index = index;
            this.messageType = messageType;
            this.encoder = Optional.ofNullable(encoder);
            this.decoder = Optional.ofNullable(decoder);
            this.messageConsumer = messageConsumer;
            indicies.put((short)(index & 0xff), this);
            types.put(messageType, this);
        }


    }
    private static <M> void tryDecode(PacketBuffer payload, Supplier<MessageContext> context, CodecIndex<M> codec)
    {
        codec.decoder.map(d->d.apply(payload)).ifPresent(m->codec.messageConsumer.accept(m, context));
    }

    private static <M> void tryEncode(PacketBuffer target, M message, CodecIndex<M> codec) {
        codec.encoder.ifPresent(c->c.accept(message, target));
    }
    public <MSG> void build(MSG message, PacketBuffer target)
    {
        @SuppressWarnings("unchecked")
        CodecIndex<MSG> codecIndex = (CodecIndex<MSG>)types.get(message.getClass());
        if (codecIndex == null) {
            LOGGER.error(SIMPLENET, "Received invalid message {}", message.getClass().getName());
            throw new IllegalArgumentException("Invalid message "+message.getClass().getName());
        }
        tryEncode(target, message, codecIndex);
    }

    void consume(final PacketBuffer payload, Supplier<MessageContext> context) {
        short discriminator = payload.readUnsignedByte();
        final CodecIndex<?> codecIndex = indicies.get(discriminator);
        if (codecIndex == null) {
            LOGGER.error(SIMPLENET, "Received invalid discriminator byte {}", discriminator);
            return;
        }
        tryDecode(payload, context, codecIndex);
    }

    <MSG> void addCodecIndex(int index, Class<MSG> messageType, BiConsumer<MSG, PacketBuffer> encoder, Function<PacketBuffer, MSG> decoder, BiConsumer<MSG, Supplier<MessageContext>> messageConsumer) {
        new CodecIndex<>(index, messageType, encoder, decoder, messageConsumer);
    }
}
