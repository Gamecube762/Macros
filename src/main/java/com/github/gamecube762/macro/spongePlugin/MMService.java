package com.github.gamecube762.macro.spongePlugin;

import com.github.gamecube762.macro.services.MacroManger;
import com.github.gamecube762.macro.util.Macro;
import com.github.gamecube762.macro.util.MacroUtils;
import com.github.gamecube762.macro.util.MultipleObjectExceptionHandler;
import com.github.gamecube762.macro.util.commands.MacroCommands;
import com.google.common.reflect.TypeToken;
import com.sun.istack.internal.Nullable;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by Gamecube762 on 10/6/2016.
 */
public class MMService implements MacroManger {

    @Deprecated
    public static MMService me;

    private SpongeLoader plugin;

    private Set<Macro> storage = new HashSet<>();

    protected MMService(SpongeLoader plugin) {
        if (this.plugin != null)
            throw new IllegalArgumentException();

        this.plugin = plugin;
        me = this;
    }

    public Set<Macro> getStorage() {
        return storage;
    }

    public void registerMacro(Macro macro) {
        storage.add(macro);
    }

    public void removeMacro(Macro macro) {
        storage.remove(macro);
    }

    /**
     *
     * does not apply new macros
     *
     */
    public List<Macro> loadMacros() throws IOException {
        List<Macro> newMacs = new ArrayList<>();
        List<String> cmds = new ArrayList<>();
        MultipleObjectExceptionHandler<ConfigurationNode>   storExc = new MultipleObjectExceptionHandler<>();
        MultipleObjectExceptionHandler<Path>                fileExc = new MultipleObjectExceptionHandler<>();

        //Search Storage.yml
        if (Files.exists(plugin.storageFile)) {
            ConfigurationNode root = plugin.storageLoader.load();

            try {cmds = root.getNode("commandMacros").getList(TypeToken.of(String.class));}
            catch (ObjectMappingException e) {storExc.thrown(e, root.getNode("commandMacros"));}

            root.getNode("macros").getChildrenList().forEach(node -> {
                try {newMacs.add(node.getValue(TypeToken.of(Macro.class)));}
                catch (ObjectMappingException e) {storExc.thrown(e, node);}
            });
        }


        //Search /macros/*.mcmacro
        Files.list(plugin.customFolder)
                .filter(path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(".mcmacro"))
                .forEach(path -> {
                    try {
                        ConfigurationNode root = HoconConfigurationLoader.builder().setPath(path).build().load();
                        String id = String.format("%s.%s", root.getNode("AuthorUUID").getString(), root.getNode("Name").getString()).toLowerCase();

                        for (Macro a : newMacs)
                            if (a.getID().equals(id)) {
                                plugin.logger.warn(String.format("%s contains a macro with the same ID as a macro from storage.conf: %s\n Using the storage.conf version.", path.getFileName().toString(), id));
                                return;
                            }

                        Macro m = root.getNode("macro").getValue(TypeToken.of(Macro.class));
                        m.setStoragePath(path);
                        newMacs.add(m);
                    }
                    catch (IOException | ObjectMappingException e) {fileExc.thrown(e, path);}
                });


        //check if errors
        if (!storExc.isEmpty() || !fileExc.isEmpty())
            plugin.logger.error(
                    String.format(
                            "Failed to load Macros:\n%s%s",
                            storExc.isEmpty() ? "" : storExc.getMessage(node -> Arrays.stream(node.getPath()).map(Object::toString).collect(Collectors.joining("."))),
                            fileExc.isEmpty() ? "" : fileExc.getMessage()
                            )
            );

        storage.addAll(newMacs);
        MacroUtils.updateGameProfiles_SelfManaged(newMacs.stream().map(Macro::getAuthor).collect(Collectors.toCollection(ArrayList::new)), plugin);

        if (!cmds.isEmpty()) {
            final List<String> a = cmds;
            newMacs.stream().filter(o -> a.contains(o.getID())).forEach(MacroCommands.Factory::registerMacroCommand);
        }

        return newMacs;
    }

    public void saveMacros() throws IOException {
        if (storage.isEmpty()) return;

        ConfigurationNode root = Files.exists(plugin.storageFile) ? plugin.storageLoader.load() : plugin.storageLoader.createEmptyNode();

        try {
            root.getNode("macros").setValue(
                    Macro.Token_MacroList,
                    storage.stream()
                            .filter(o -> !o.isExternal())
                            .collect(Collectors.toCollection(ArrayList::new))
            );
        }
        catch (ObjectMappingException e) {plugin.logger.error(e.getMessage());}

        root.getNode("commandMacros")
                .setValue(
                        MacroCommands.Factory.getRegisteredMacroCommands()
                                .stream()
                                .map(MacroCommands.MacroCommand::getID)
                                .collect(Collectors.toList())
                );

        plugin.storageLoader.save(root);//save storage.conf

        MultipleObjectExceptionHandler<Macro> excHandler = new MultipleObjectExceptionHandler<>();

        //save external macros
        storage.stream().filter(Macro::isExternal).forEach(m -> {
            try {
                ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder().setPath(m.getStoragePath().get()).build();
                ConfigurationNode node = loader.load();
                node.getNode("macro").setValue(Macro.Token_Macro, m);
                loader.save(node);
            }
            catch (ObjectMappingException | IOException e) {excHandler.thrown(e, m);}
        });

        if (!excHandler.isEmpty())
            plugin.logger.error(
                    excHandler.getMessage(
                            m -> String.format(
                                    "%s | %s",
                                    m.getID(),
                                    m.getStoragePath().get().getFileSystem().toString()
                            )
                    )
            );//todo format string

    }

    public void exportMacro(Macro macro) throws IOException, ObjectMappingException {
        exportMacro(macro, plugin.customFolder.resolve(macro.getID() + ".mcmacro"));
    }

    public void exportMacro(Macro macro, Path path) throws IOException, ObjectMappingException {
        if (macro.isExternal())
            throw new IllegalArgumentException(String.format("Macro is already External - %s.mcmacro", macro.getID()));

        ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder().setPath(path).build();
        ConfigurationNode root = loader.createEmptyNode();
        root.getNode("macro").setValue(Macro.Token_Macro, macro);
        loader.save(root);
        macro.setStoragePath(path);
    }

    public void importMacro(Path path) {
        if (!Files.exists(path))
            throw new IllegalArgumentException("Path does not exist.");




    }

    public Collection<Macro> getAccessableMacros(Player p) {
        return storage.stream().filter(
                m -> m.isPublic()
                        || m.getAuthorUniqueId() == p.getUniqueId()
                        || p.hasPermission("macro.other." + m.getID())//todo
        ).collect(Collectors.toList());
    }

    public Optional<Macro> getMacro(UUID uuid, String name) {//todo update to MacroAuthor
        //todo getAccessableMacros()
        return getMacro(String.format("%s.%s", uuid, name));
    }

    public Optional<Macro> getMacro(String macroID) {
        boolean longID = macroID.contains(".");
        String[] a = macroID.split("\\.");

        for (Macro m : storage)
            if ((longID && ( m.getAuthorName().equalsIgnoreCase(a[0]) || m.getAuthorUniqueId().toString().equalsIgnoreCase(a[0])) && m.getName().equalsIgnoreCase(a[1])) || m.getName().equalsIgnoreCase(macroID))
                return Optional.of(m);
        return Optional.empty();
    }

    public Collection<Macro> getPublicMacros() {
        return storage.stream().filter(Macro::isPublic).collect(Collectors.toList());
    }

    public Collection<Macro> getMacrosAuthoredBy(UUID uuid) {
        return storage.stream().filter(m -> m.getAuthorUniqueId() == uuid).collect(Collectors.toList());
    }

    public void runMacro(CommandSource source, Macro macro) {
        runMacro(source, macro);
    }

    public void runMacro(CommandSource source, Macro macro, String... args) {
        runMacro(source, macro, Arrays.asList(args));
    }

    public void runMacro(CommandSource source, Macro macro, @Nullable List<String> args) {
        if (args == null)
            if (macro.requiresArgs())
                throw new IllegalArgumentException("Arguments are required for this macro.");
            else
                args = new ArrayList<>();

        if (macro.getRequiredArgCount() > args.size())
            throw new IllegalArgumentException(
                    String.format(
                            "This macro requires %s arguments.\n%s",
                            macro.getArgs().size(),
                            macro.getArgs().stream().collect(Collectors.joining(" "))
                            )
            );

        Consumer<Task> r = new Runner(plugin, macro, source, args);
        Task t = Sponge.getScheduler().createTaskBuilder()
                .name(String.format("MacroRunner:%s:%s.%s", source.getName(), macro.getAuthorName(), source.getName()))
                .intervalTicks(1)
                .execute(r)
                .submit(plugin);

        source.sendMessage(Text.of(String.format("Running macro: %s%s", macro.getPublicName(), args.isEmpty() ? "" : args.stream().collect(Collectors.joining(" ", ", ", "")))));

        if (plugin.configRoot.getNode("startMacrosOnTheSameTick").getBoolean(true)) //if run immediately
            r.accept(t);//force run
    }

}
