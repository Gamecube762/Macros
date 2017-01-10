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
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
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

            cmds = root.getNode("commandMacros").getList(Object::toString, cmds);

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

                        newMacs.add(root.getNode("macro").getValue(TypeToken.of(Macro.class)));
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

        try {root.getNode("macros").setValue(Macro.Token_MacroList, storage.stream().filter(o -> !o.isExternal()).collect(Collectors.toCollection(ArrayList::new)));}//we dont save externals yet
        catch (ObjectMappingException e) {plugin.logger.error(e.getMessage());}

        root.getNode("commandMacros")
                .setValue(
                        MacroCommands.Factory.getRegisteredMacroCommands()
                                .stream()
                                .map(MacroCommands.MacroCommand::getID)
                                .collect(Collectors.toList())
                );

        //todo external saving

        plugin.storageLoader.save(root);
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
        macro.setExternal(true);
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

        Runner r = new Runner(macro, source, args);
        Task t = Sponge.getScheduler().createTaskBuilder()
                .name(String.format("MacroRunner:%s:%s.%s", source.getName(), macro.getAuthorName(), source.getName()))
                .intervalTicks(1)
                .execute(r)
                .submit(plugin);

        source.sendMessage(Text.of(String.format("Running macro: %s%s", macro.getPublicName(), args.isEmpty() ? "" : args.stream().collect(Collectors.joining(" ", ", ", "")))));

        if (plugin.configRoot.getNode("startMacrosOnTheSameTick").getBoolean(true)) //if run immediately
            r.accept(t);//force run
    }

    //todo {User} for placeholder of the user using it.
    //todo {==} for remaining arguments | {=2=} for arguments 2 and after
    //todo #: = comment | todo document
    //todo wait: 20 = wait 20ticks before next command
    //todo wait Action | .c:Wait 20t | [#][format] | ticks, seconds, minutes, hours
    //todo configOption: custom tickrate
    //todo move class to its own thing
    private class Runner implements Consumer<Task> {

        int cmdNext = 0, cmdCount = 0, excCount = 0;
        int maxCMD, maxTickTime, maxErr;
        long lastTime = 0;
        Macro macro;
        CommandSource source;
        List<String> actions, macArgs, inArgs;
        MultipleObjectExceptionHandler<String> excHandler = new MultipleObjectExceptionHandler<>();

        Runner(Macro macro, CommandSource source, List<String> args) {
            this.macro = macro;
            this.source = source;
            this.actions = macro.getActions();
            this.macArgs = macro.getArgs();
            this.inArgs = args;

            ConfigurationNode root = plugin.configRoot;
            maxCMD = root.getNode("maxCommandsPerTick").getInt(5);
            maxTickTime = root.getNode("maxTimePerTick").getInt(5);
            maxErr = root.getNode("maxJavaErrors").getInt(5);
        }

        public void accept(Task task) {
            lastTime = System.currentTimeMillis();
            cmdCount = 0;

            /**
             * accept(task)             | Ran once per tick; while loop runs ~5 commands per tick
             * cmdCount < 5             | Max commands to run per tick
             * cmdNext < actions.size() | Did we complete all the tasks?
             * tickTime() < 5           | Wait for next tick if this tick has taken longer than 5ms
             * excCount < 10            | Max errors before canceling
             */
            while (cmdCount < maxCMD && cmdNext < actions.size() && tickTime() < maxTickTime && excCount < maxCMD)
                try {
                    String out = actions.get(cmdNext);

                    if (out == null || out.isEmpty() || out.startsWith("#:")) {
                        cmdCount--;//prevents going through a tick and not running any commands
                        continue;
                    }

                    Matcher m = Macro.REGEX_Arguments.matcher(out);

                    while (m.find()) {//find and fill in Arguments
                        String group = m.group();
                        //if (!out.contains(group)) continue;//Skip Group | Don't recall it being out.contains
                        int num = MacroUtils.getArgKey(group);
                        int size = inArgs.size();

                        if (num < size) {
                            String arg = inArgs.get(num);

                            out = out.replace(
                                    group,
                                    (arg.length() > 3 && arg.equals("~"))
                                            ? MacroUtils.getArgValue(group).orElse("")
                                            : arg
                            );
                        }
                        else if (group.length() > 3)
                            out = out.replace(group, MacroUtils.getArgValue(group).orElse(""));
                    }

                    plugin.logger.debug("> " + out);
                    Sponge.getCommandManager().process(source, out);
                }

                catch (Exception e) {
                    excCount++;
                    excHandler.thrown(
                            e,
                            String.format("Action %s | \'%s\'", cmdNext, actions.get(cmdNext))
                    );
                }

                finally {//End of Loop
                    cmdCount++;
                    cmdNext++;
                }

            //==============
            // End of Loop;
            //  Is the macro complete?
            //==============

            if (excCount >= maxErr) {//Error count maxed
                String err =
                        String.format(
                                "Macro %s(Used by %s) was Canceled due to too many errors. Errors thrown:\n%s",
                                macro.getPublicName(),
                                source.getName(),
                                excHandler.getMessage(o -> o)
                        );

                source.sendMessage(
                        Text.builder("Macro was canceled due to too many errors. Errors are printed in console.")
                                .onHover(TextActions.showText(Text.of(TextColors.RED, err)))
                                .build()
                );

                plugin.logger.warn(err);
                task.cancel();
            }

            if (cmdNext >= actions.size())//Completed all actions
                task.cancel();

        }

        long tickTime() {
            return System.currentTimeMillis() - lastTime;
        }
    }
}
