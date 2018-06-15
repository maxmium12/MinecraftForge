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

import net.minecraftforge.fml.loading.DefaultModInfos;

public class DefaultModContainers
{
    public static final ModContainer MINECRAFT = new ModContainer(DefaultModInfos.minecraftModInfo)
    {
        @Override
        public boolean matches(final Object mod)
        {
            return mod == this;
        }

        @Override
        public Object getMod()
        {
            return this;
        }
    };
}
