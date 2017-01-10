package com.github.gamecube762.macro.services;

import com.github.gamecube762.macro.util.Macro;
import com.sun.istack.internal.Nullable;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by Gamecube762 on 10/6/2016.
 *
 * MacroManager interface containing all methods the service should have.
 *
 * Deprecated methods may change soon.
 */
public interface MacroManger {

    /**
     * Get the set of Macros registered.
     *
     * @return Set of the stored Macros
     */
    Set<Macro> getStorage();

    /**
     * Register a Macro
     *
     * @param macro Macro to register
     */
    void registerMacro(Macro macro);

    /**
     * Unregister a Macro
     *
     * @param macro Macro to remove
     */
    void removeMacro(Macro macro);


    /**
     * Load macros from config.
     *
     * @return List of newly loaded Macros
     * @throws IOException
     */
    List<Macro> loadMacros() throws IOException;

    /**
     * Save registered Macros to file
     *
     * @throws IOException
     */
    void saveMacros() throws IOException;


    /**
     * Export a Macro to it's own file for easier moving.
     *
     * @param macro Macro to export
     * @throws IOException
     * @throws ObjectMappingException
     */
    void exportMacro(Macro macro) throws IOException, ObjectMappingException;

    /**
     * Export a Macro to the desired Path
     *
     * @param macro Macro to export
     * @param path Path to save Macro to
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
    Collection<Macro> getAccessableMacros(Player p);

    @Deprecated
    Optional<Macro> getMacro(UUID uuid, String name);

    @Deprecated
    Optional<Macro> getMacro(String macroID);

    @Deprecated
    Collection<Macro> getPublicMacros();

    @Deprecated
    Collection<Macro> getMacrosAuthoredBy(UUID uuid);


    /**
     * Run a macro for the CommandSource
     *
     * @param source User to run the macro
     * @param macro Macro to run
     */
    void runMacro(CommandSource source, Macro macro);

    /**
     * Run a macro for the CommandSource
     *
     * @param source User to run the macro
     * @param macro Macro to run
     * @param args Arguments to pass to the macro
     */
    void runMacro(CommandSource source, Macro macro, String... args);

    /**
     * Run a macro for the CommandSource
     *
     * @param source User to run the macro
     * @param macro Macro to run
     * @param args Arguments to pass to the macro
     */
    void runMacro(CommandSource source, Macro macro, @Nullable List<String> args);
}
