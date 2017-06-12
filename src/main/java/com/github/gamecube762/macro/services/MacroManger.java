package com.github.gamecube762.macro.services;

import com.github.gamecube762.macro.util.Macro;
import com.github.gamecube762.macro.util.actionCommands.ActionCommand;
import com.sun.istack.internal.Nullable;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.slf4j.Logger;
import org.spongepowered.api.command.CommandSource;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by Gamecube762 on 10/6/2016.
 *
 * {@link MacroManger} interface containing all methods the service should have.
 *
 * Deprecated methods are WIP and may change soon.
 */
public interface MacroManger {

    /**
     * Get the set of (@link Macro}s registered.
     *
     * @return Set of the stored {@link Macro}s
     */
    Set<Macro> getStorage();

    /**
     * Get the logger this {@link MacroManger  } is using.
     *
     * @return Logger
     */
    Logger getLogger();

    /**
     * Register a {@link Macro}
     *
     * @param macro {@link Macro} to register
     */
    void registerMacro(Macro macro);

    /**
     * Unregister a{@link Macro}
     *
     * @param macro{@link Macro} to remove
     */
    void removeMacro(Macro macro);

    /**
     * Register an {@link ActionCommand}
     *
     * @param ac {@link ActionCommand}
     * @param aliases {@link ActionCommand}'s Aliases
     */
    void registerActionCommand(ActionCommand ac, String... aliases);

    /**
     * Unregisters an {@link ActionCommand}
     *
     * @param alias {@link ActionCommand}'s Alias
     */
    void unregisterActionCommand(String alias);

    /**
     * Get the map of {@link ActionCommand}s
     *
     * @return map
     */
    HashMap<ActionCommand, String[]> getActionCommands();

    /**
     * Try to get an {@link ActionCommand} with the provided alias.
     *
     * @param alias {@link ActionCommand}'s Alias
     * @return Optional of {@link ActionCommand}
     */
    Optional<ActionCommand> getActionCommand(String alias);

    /**
     * Load {@link Macro}s from config.
     *
     * @return List of newly loaded {@link Macro}
     * @throws IOException
     */
    List<Macro> loadMacros() throws IOException;

    /**
     * Save registered {@link Macro}s to file
     *
     * @throws IOException
     */
    void saveMacros() throws IOException;

    /**
     * Export a{@link Macro} to it's own file for easier moving.
     *
     * @param macro{@link Macro} to export
     * @throws IOException
     * @throws ObjectMappingException
     */
    void exportMacro(Macro macro) throws IOException, ObjectMappingException;

    /**
     * Export a{@link Macro} to the desired Path
     *
     * @param macro{@link Macro} to export
     * @param path Path to save{@link Macro} to
     * @throws IOException
     * @throws ObjectMappingException
     */
    void exportMacro(Macro macro, Path path) throws IOException, ObjectMappingException;

    /**
     * todo
     * @param path
     */
    @Deprecated
    void importMacro(Path path);

    @Deprecated
    Collection<Macro> getAccessableMacros(CommandSource source);

    @Deprecated
    Optional<Macro> getMacro(UUID uuid, String name);

    @Deprecated
    Optional<Macro> getMacro(String macroID);

    @Deprecated
    Collection<Macro> getPublicMacros();

    @Deprecated
    Collection<Macro> getMacrosAuthoredBy(UUID uuid);

    /**
     * Run a {@link Macro} for the CommandSource
     *
     * @param source User to run the {@link Macro}
     * @param macro{@link Macro} to run
     */
    void runMacro(CommandSource source, Macro macro);

    /**
     * Run a {@link Macro} for the CommandSource
     *
     * @param source User to run the {@link Macro}
     * @param macro{@link Macro} to run
     * @param args Arguments to pass to the {@link Macro}
     */
    void runMacro(CommandSource source, Macro macro, String... args);

    /**
     * Run a macro for the CommandSource
     *
     * @param source User to run the {@link Macro}
     * @param macro{@link Macro} to run
     * @param args Arguments to pass to the {@link Macro}
     */
    void runMacro(CommandSource source, Macro macro, @Nullable List<String> args);
}
