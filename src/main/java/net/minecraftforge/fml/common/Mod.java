/*
 * Minecraft Forge
 * Copyright (c) 2016.
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

package net.minecraftforge.fml.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.common.event.FMLEvent;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms.IMCEvent;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.versioning.VersionRange;
import net.minecraftforge.fml.relauncher.Side;

/**
 * This defines a Mod to FML.
 * Any class found with this annotation applied will be loaded as a Mod. The instance that is loaded will
 * represent the mod to other Mods in the system. It will be sent various subclasses of {@link FMLEvent}
 * at pre-defined times during the loading of the game, based on where you have applied the {@link EventHandler}
 * annotation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Mod
{
    /**
     * The unique mod identifier for this mod.
     * <b>Required to be lowercased in the english locale for compatibility. Will be truncated to 64 characters long.</b>
     *
     * This will be used to identify your mod for third parties (other mods), it will be used to identify your mod for registries such as block and item registries.
     * By default, you will have a resource domain that matches the modid. All these uses require that constraints are imposed on the format of the modid.
     */
    String modid();

    /**
     * An optional GUI factory for this mod. This is the name of a class implementing {@link IModGuiFactory} that will be instantiated
     * on the client side, and will have certain configuration/options guis requested from it.
     *
     * @return The name of a class implementing {@link IModGuiFactory}
     */
    String guiFactory() default "";

    /**
     * A list of custom properties for this mod. Completely up to the mod author if/when they
     * want to put anything in here.
     * @return an optional list of custom properties
     */
    CustomProperty[] customProperties() default {};

    /**
     * A custom key => value property pair for use with {@link Mod#customProperties()}
     * @author cpw
     *
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({})
    @interface CustomProperty
    {
        /**
         * A key. Should be unique.
         * @return A key
         */
        String k();
        /**
         * A value. Can be anything.
         * @return A value
         */
        String v();
    }
    /**
     * Marks the associated method as handling an FML lifecycle event.
     * The method must have a single parameter, one of the following types. This annotation
     * replaces the multiple different annotations that previously were used.
     *
     * Current event classes. This first section is standard lifecycle events. They are dispatched
     * at various phases as the game starts. Each event should have information useful to that
     * phase of the lifecycle. They are fired in this order.
     *
     * These suggestions are mostly just suggestions on what to do in each event.
     * <ul>
     * <li> {@link FMLPreInitializationEvent} : Run before anything else. Read your config, create blocks,
     * items, etc, and register them with the {@link GameRegistry}.</li>
     * <li> {@link FMLInitializationEvent} : Do your mod setup. Build whatever data structures you care about. Register recipes,
     * send {@link FMLInterModComms} messages to other mods.</li>
     * <li> {@link FMLPostInitializationEvent} : Handle interaction with other mods, complete your setup based on this.</li>
     * </ul>
     * <p>These are the server lifecycle events. They are fired whenever a server is running, or about to run. Each time a server
     * starts they will be fired in this sequence.
     * <ul>
     * <li> {@link FMLServerAboutToStartEvent} : Use if you need to handle something before the server has even been created.</li>
     * <li> {@link FMLServerStartingEvent} : Do stuff you need to do to set up the server. register commands, tweak the server.</li>
     * <li> {@link FMLServerStartedEvent} : Do what you need to with the running server.</li>
     * <li> {@link FMLServerStoppingEvent} : Do what you need to before the server has started it's shutdown sequence.</li>
     * <li> {@link FMLServerStoppedEvent} : Do whatever cleanup you need once the server has shutdown. Generally only useful
     * on the integrated server.</li>
     * </ul>
     * The second set of events are more specialized, for receiving notification of specific
     * information.
     * <ul>
     * <li> {@link FMLFingerprintViolationEvent} : Sent just before {@link FMLPreInitializationEvent}
     * if something is wrong with your mod signature</li>
     * <li> {@link IMCEvent} : Sent just after {@link FMLInitializationEvent} if you have IMC messages waiting
     * from other mods</li>
     * </ul>
     *
     * @author cpw
     *
     */
    @Deprecated
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface EventHandler{}

    /**
     * Populate the annotated field with the mod instance based on the specified ModId. This can be used
     * to retrieve instances of other mods.
     * @author cpw
     *
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Instance {
        /**
         * The mod object to inject into this field
         */
        String value() default "";

        /**
         * Optional owner modid, required if this annotation is on something that is not inside the main class of a mod container.
         * This is required to prevent mods from classloading other, potentially disabled mods.
         */
        String owner() default "";
    }
    /**
     * Populate the annotated field with the mod's metadata.
     * @author cpw
     *
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Metadata {
        /**
         * The mod id specifying the metadata to load here
         */
        String value() default "";

        /**
         * Optional owner modid, required if this annotation is on something that is not inside the main class of a mod container.
         * This is required to prevent mods from classloading other, potentially disabled mods.
         */
        String owner() default "";
    }

    /**
     * A class which will be subscribed to {@link net.minecraftforge.common.MinecraftForge.EVENT_BUS} at mod construction time.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface EventBusSubscriber {
        Side[] value() default { Side.CLIENT, Side.SERVER };

        /**
         * Optional value, only nessasary if tis annotation is not on the same class that has a @Mod annotation.
         * Needed to prevent early classloading of classes not owned by your mod.
         * @return
         */
        String modid() default "";
    }
}
