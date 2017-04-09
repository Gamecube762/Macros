package com.github.gamecube762.macro.spongePlugin;

import com.github.gamecube762.macro.util.Macro;
import com.github.gamecube762.macro.util.MacroAuthor;
import com.github.gamecube762.macro.util.MacroRunner;
import com.github.gamecube762.macro.util.MultipleObjectExceptionHandler;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.regex.Matcher;

/**
 * Created by gamec on 1/20/2017.
 */
public class Runner implements MacroRunner, Consumer<Task>  {

    //todo {User} for placeholder of the user using it.
    //todo {==} for remaining arguments | {=2=} for arguments 2 and after

    //todo /use -each 0 One Two three | runs the macro 3 times, one per arg, all args as {0}

    //todo commands:
    //todo .#: = comment | todo document
    //todo .wait: 20 | # [ticks | seconds | minutes] - def Ticks
    //todo .cond: {0} == Banana -> .goto: 5 | Statement -> Command
    //todo .goto: # | set the next line to run
    //todo .sudo: give {0} Diamond | Run as console - Requires extra perm
    //todo .logi: [text] | log with the info level - format log<level> - l = info; w = warn; e = error; d = debug
    //todo .echo: [Message] | print a message to the user
    //todo .done: [exitMessage] | Finish a macro with an exit message printed to the user - aka .echo:

    //todo Maybe add varibles? .var: pie = good

    //todo configOption: custom tickrate

    int cmdNext = 0, cmdCount = 0, excCount = 0;
    int maxCMD, maxTickTime, maxErr;
    long lastTime = 0;

    SpongeLoader plugin;
    Macro macro;
    CommandSource source;
    List<String> actions, macArgs, inArgs;
    MultipleObjectExceptionHandler<String> excHandler = new MultipleObjectExceptionHandler<>();

    public Runner(SpongeLoader plugin, Macro macro, CommandSource source, List<String> args) {
        this.plugin = plugin;
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

                //======================================
                //Find and replace Placeholder Arguments
                //======================================

                Matcher m = Macro.REGEX_Arguments.matcher(out);

                while (m.find()) {//find and fill in Arguments
                    String arg = m.group();
                    String replacement = "";

                    if (m.group(1) != null) {//{#orValue}
                        int num = Integer.parseInt(m.group(1));
                        int size = inArgs.size();
                        String newArg = num >= size ? "" : inArgs.get(num);//todo
                        replacement = newArg;

                        if (newArg.isEmpty() || newArg.equals("~"))
                            if (m.group(2) == null)
                                replacement = m.group(3);
                    }

                    else if (m.group(4) != null) {//{=#=}
                        StringJoiner a = new StringJoiner(" ");
                        for (int i = Integer.parseInt(m.group(4)); i < inArgs.size(); i++)
                            a.add(inArgs.get(i));

                        replacement = a.toString();
                    }

                    else if (m.group(5) != null) switch (m.group(5).toLowerCase()) {
                        case "user": replacement = source.getName(); break;
                        case "userid": replacement = (source instanceof Player ? ((Player)source).getUniqueId() : MacroAuthor.consoleUUID).toString(); break;
                        case "macroname": replacement = macro.getName(); break;
                        case "macroid": replacement = macro.getID(); break;
                    }

                    out = out.replace(arg, replacement);
                }

                //===============
                //Run the Command
                //===============

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

    public int getCurrentLine() {
        return cmdNext;
    }

    public int getTickRunCount() {
        return cmdCount;
    }

    public int getErrorCount() {
        return excCount;
    }

    public int getTickRunLimit() {
        return maxCMD;
    }

    public int getMaxTickTime() {
        return maxTickTime;
    }

    public int getMaxErrorCount() {
        return maxErr;
    }

    public long getTickStartTime() {
        return lastTime;
    }

    public SpongeLoader getPlugin() {
        return plugin;
    }

    public Macro getMacro() {
        return macro;
    }

    public CommandSource getSource() {
        return source;
    }

    public List<String> getActions() {
        return actions;
    }

    public List<String> getMacArgs() {
        return macArgs;
    }

    public List<String> getInArgs() {
        return inArgs;
    }

    public MultipleObjectExceptionHandler<String> getExcHandler() {
        return excHandler;
    }

    public static void main(String[] args) {
    }
}
