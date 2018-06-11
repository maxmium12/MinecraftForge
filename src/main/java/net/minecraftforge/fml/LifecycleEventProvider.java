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

package net.minecraftforge.fml;

import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.loading.ModList;
import net.minecraftforge.fml.loading.ModLoadingStage;

import java.util.function.Supplier;

public enum LifecycleEventProvider
{
    LOAD(()->new LifecycleEvent(ModLoadingStage.BEGIN)),
    PREINIT(()->new LifecycleEvent(ModLoadingStage.PREINIT));

    public void dispatch(final ModList target) {
        target.dispatchLifeCycleEvent(this.event.get());
    }
    private final Supplier<? extends LifecycleEvent> event;

    LifecycleEventProvider(Supplier<? extends LifecycleEvent> e)
    {
        event = e;
    }


    public static class LifecycleEvent extends Event {
        private final ModLoadingStage stage;

        public LifecycleEvent(ModLoadingStage stage)
        {
            this.stage = stage;
        }

        public ModLoadingStage fromStage()
        {
            return this.stage;
        }

        public ModLoadingStage toStage()
        {
            return ModLoadingStage.values()[this.stage.ordinal()+1];
        }

    }
}
