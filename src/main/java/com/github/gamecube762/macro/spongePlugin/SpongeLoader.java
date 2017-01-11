package com.github.gamecube762.macro.spongePlugin;

import com.github.gamecube762.macro.commands.*;
import com.github.gamecube762.macro.services.MacroManger;
import com.github.gamecube762.macro.util.Macro;
import com.github.gamecube762.macro.util.MacroUtils;
import com.github.gamecube762.macro.util.commands.MacroCommands;
import com.google.inject.Inject;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameConstructionEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.world.SaveWorldEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * Created by Gamecube762 on 10/5/2016.
 */
@Plugin(id = "macros", name = "Macros", version = "1.0-alpha-2", authors = {"Gamecube762"}, description = "Create and use macros to simplify your daily needs.")
public class SpongeLoader {

    @Inject
    protected Logger logger;

    @Inject @DefaultConfig(sharedRoot = false)
    protected Path configFile;
    protected Path storageFile;
    protected Path configFolder;
    protected Path customFolder;

    @Inject @DefaultConfig(sharedRoot = false)
    protected ConfigurationLoader<CommentedConfigurationNode> configLoader;
    protected ConfigurationLoader<CommentedConfigurationNode> storageLoader;
    protected ConfigurationNode configRoot;

    private long lastAutoSave = 0, autoSaveInterval = -2;

    @Listener
    public void con(GameConstructionEvent event) {
        TypeSerializers.getDefaultSerializers().registerType(Macro.Token_Macro, new Macro.Serializer());
        MacroCommands.Factory.setO(this);

        this.configFolder = configFile.getParent();
        this.customFolder = configFolder.resolve("custom");
        this.storageFile = configFolder.resolve("storage.conf");
        this.storageLoader = HoconConfigurationLoader.builder().setPath(storageFile).build();

        try {//get the auto-save-interval from the global.conf
            this.autoSaveInterval =
                    HoconConfigurationLoader
                            .builder().setPath(configFolder.getParent().resolve("sponge").resolve("global.conf")).build()
                            .load().getNode("sponge", "world", "auto-save-interval").getLong();
        } catch (IOException e) {
            this.autoSaveInterval = 900;//default
        }

        try {//create the folders
            if (Files.notExists(configFolder)) Files.createDirectories(configFolder);
            if (Files.notExists(customFolder)) Files.createDirectories(customFolder);
        } catch (IOException e) {
            e.printStackTrace();//todo
        }
    }

    private void loadConfig() throws IOException {
        if (Files.exists(configFile)) configRoot = configLoader.load();
        else {
            //todo temp | make conf file

            configRoot = configLoader.createEmptyNode();
            configRoot.getNode("maxCommandsPerTick").setValue(10);
            configRoot.getNode("maxTimePerTick").setValue(5);
            configRoot.getNode("maxJavaErrors").setValue(10);
            configRoot.getNode("startMacrosOnTheSameTick").setValue(true);
            configRoot.getNode("chatShortcut").setValue(Arrays.asList(".m:", ".macro:"));

            configLoader.save(configRoot);
        }
    }

    @Listener
    public void preInit(GamePreInitializationEvent event) {

        try {loadConfig();}
        catch (IOException ex) {ex.printStackTrace();}

        MMService mm = new MMService(this);
        Sponge.getServiceManager().setProvider(this, MacroManger.class, mm);

        try {mm.loadMacros();}
        catch (IOException e) {e.printStackTrace();}

        CommandSpec macro = CommandSpec.builder()//todo tab-complete for names
                .permission("macro.command")
                .description(Text.of("Macro manager command."))
                .child(cmd_Create.spec, "create", "add", "new", "c", "a", "n")
                .child(cmd_Use.spec, "use", "u")
                .child(cmd_Edit.spec, "edit", "e")
                .child(cmd_List.spec, "list", "l")
                .child(cmd_View.spec, "view", "v")
                .child(cmd_Delete.spec, "delete", "del", "remove", "rem")
                .child(cmd_FLoad.spec, "load")
                .child(cmd_FSave.spec, "save")
                .child(cmd_Export.spec, "export")
                .child(cmd_SetPublic.spec, "setPublic", "sp", "public", "p")
                .child(cmd_setDescription.spec, "setdescription", "description")
                .child(cmd_SetAsCommand.spec, "setascommand", "setcommand", "ascommand", "cmd")
                .build();

        Sponge.getCommandManager().register(this, macro, "macro", "macros", "m");
        Sponge.getEventManager().registerListeners(this, new ChatHandler(this));
    }

    @Listener
    public void preInit(GameInitializationEvent event) {}

    @Listener
    public void onWorldSave(SaveWorldEvent e) {//auto save
        if (System.currentTimeMillis() - lastAutoSave < autoSaveInterval)
            return;//prevents saving per world per save cycle

        lastAutoSave = System.currentTimeMillis();

        try {MacroUtils.getMacroManager().orElse(MMService.me).saveMacros();}
        catch (IOException ex) {logger.error("Unable to save: IOException | " + ex.getMessage());}
    }
}
