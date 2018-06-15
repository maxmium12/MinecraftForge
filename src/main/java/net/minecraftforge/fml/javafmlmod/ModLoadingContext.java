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

package net.minecraftforge.fml.javafmlmod;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.language.ExtensionPoint;

import java.util.function.Supplier;

public class ModLoadingContext
{
    private static ThreadLocal<ModLoadingContext> context = ThreadLocal.withInitial(ModLoadingContext::new);
    FMLModContainer activeContainer;
    public static ModLoadingContext get() {
        return context.get();
    }

    public <T> void registerExtensionPoint(ExtensionPoint<T> point, Supplier<T> extension) {
        activeContainer.registerExtensionPoint(point, extension);
    }

    public IEventBus getModEventBus()
    {
        return activeContainer.getEventBus();
    }
}
