package com.github.gamecube762.macro.spongePlugin;

import com.github.gamecube762.macro.util.*;
import com.github.gamecube762.macro.util.actionCommands.ActionCommandException;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.regex.Matcher;

/**
 * Created by gamec on 1/20/2017.
 */
public class Runner implements MacroRunner, Consumer<Task>  {

    //todo /use -each 0 One Two three | runs the macro 3 times, one per arg, all args as {0}

    //todo Document:
    //.#: = comment |
    //.wait: 20 | # [ticks | seconds | minutes] - def Ticks
    //.goto: # | set the next line to run
    //.sudo: give {0} Diamond | Run as console - Requires extra perm
    //.logi: [text] | log with the info level - format log<level> - l = info; w = warn; e = error; d = debug
    //.echo: [Message] | print a message to the user
    //.done: [exitMessage] | Finish a macro with an exit message printed to the user - aka .echo:

    //todo commands:
    //todo .cond: {0} == Banana -> .goto: 5 | Statement -> Command
    //todo Maybe add varibles? .var: pie = good | .var: a = 1 | .var: a += 2

    //todo configOption: custom tickrate

    int cmdNext = 0, cmdCount = 0, errCount = 0;
    int maxCMD, maxTickTime, maxErr;
    long lastTime = 0, waitingTicks = 0;

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

        if (waitingTicks < 0) {
            waitingTicks--;
            return;
        }

        /* accept(task)             | Ran once per tick; while loop runs ~5 commands per tick
         * cmdCount < 5             | Max commands to run per tick
         * cmdNext < actions.size() | Did we complete all the tasks?
         * tickTime() < 5           | Wait for next tick if this tick has taken longer than 5ms
         * errCount < 10            | Max errors before canceling
         */
        while (cmdCount < maxCMD && cmdNext < actions.size() && tickTime() < maxTickTime && errCount < maxCMD)
            try {
                String out = actions.get(cmdNext);

                if (out == null || out.isEmpty() || out.startsWith(".#:") || out.startsWith(".:")) {
                    cmdCount--;//prevents going through a tick and not running any commands
                    continue;
                }


                //======================================
                //Find and replace Placeholder Arguments
                //======================================

                Matcher m = Macro.REGEX_Arguments.matcher(out);
                while (m.find()) {//find and fill in Arguments
                    String arg = m.group();
                    String replacement = arg;

                    if (m.group(1) != null) {//{#orValue}
                        int key = Integer.parseInt(m.group(2));
                        int size = inArgs.size();
                        String newArg = key >= size ? "" : inArgs.get(key);//todo
                        replacement = newArg;

                        if (newArg.isEmpty() || newArg.equals("~"))
                            if (m.group(3) == null)
                                replacement = m.group(4);
                    }

                    else if (m.group(5) != null) {//{=#=}
                        StringJoiner a = new StringJoiner(" ");
                        String key = m.group(6);

                        for (int i = (key == null) ? 0 : Integer.parseInt(key); i < inArgs.size(); i++)
                            a.add(inArgs.get(i));

                        replacement = a.toString();
                    }

                    else if (m.group(7) != null) switch (m.group(8).toLowerCase()) {//todo Replace with Map<String, Function>
                        case "user":        replacement = source.getName(); break;
                        case "userid":      replacement = (source instanceof Player ? ((Player)source).getUniqueId() : MacroAuthor.consoleUUID).toString(); break;
                        case "macroname":   replacement = macro.getName(); break;
                        case "macroid":     replacement = macro.getID(); break;
                        case "authorname":  replacement = macro.getAuthorName(); break;
                        case "authorid":    replacement = macro.getAuthorUniqueId().toString(); break;
                        default: break; //Do nothing
                    }

                    out = out.replace(arg, replacement);
                }//end While() - Arg Replacement

                //===============
                //Run the command
                //===============

                //ActionCommand
                String[] a = out.split(" ");
                String cmd = a[0].substring( 1, a[0].length() - 1 );
                if (Macro.REGEX_ActionCommand.matcher(a[0]).matches())
                    MacroUtils.getMacroManager().orElse(MMService.me)//todo check user's perm
                            .getActionCommand(cmd)
                            .orElseThrow(() -> new ActionCommandException("Unknown ActionCommand: " + cmd))//Throw EXC if empty
                            .parse(source, Arrays.asList(Arrays.copyOfRange(a, 1, a.length)), this);//If not empty; trim array for args only and run AC

                //SpongeCommand
                else {
                    plugin.logger.debug("> " + out);
                    Sponge.getCommandManager().process(source, out);
                }
            }
            catch (ActionCommandException e) {
                source.sendMessage(Text.of(e.getMessage()));

                if (e.shouldEndMacro()) {
                    String err =
                            String.format(
                                    "Macro %s(Used by %s) was Canceled due to an ActionCommandException\nExc: %s\nMsg:%s",
                                    macro.getPublicName(),
                                    source.getName(),
                                    e.getClass().getSimpleName(),
                                    e.getMessage()
                            );

                    source.sendMessage(
                            Text.builder("Macro was canceled due to an ActionCommandException: " + e.getMessage())
                                    .onHover(TextActions.showText(Text.of(TextColors.RED, err)))
                                    .build()
                    );

                    plugin.logger.warn(err);
                    task.cancel();
                    return;
                }
            }
            catch (Exception e) {
                errCount++;
                excHandler.thrown(
                        e,
                        String.format("Action %s | \'%s\'", cmdNext, actions.get(cmdNext))
                );
            }
            finally/*its the end of the loop!*/{
                cmdCount++;
                cmdNext++;
            }

        //==============
        // End of Loop;
        //  Is the macro complete?
        //==============

        if (errCount >= maxErr) {//Error count maxed
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

    public String getCurrentLine() {
        return actions.get(cmdNext);
    }

    public int getCurrentLineNumber() {
        return cmdNext;
    }

    public void setNextLineNumber(int lineNumber) {
        cmdNext = lineNumber-1;
    }//todo - Not the safest way

    public int getTickRunCount() {
        return cmdCount;
    }

    public int getErrorCount() {
        return errCount;
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

    public void setWaitingTicks(long ticks) {
        this.waitingTicks = ticks;
    }

    public long getWaitingTicks() {
        return waitingTicks;
    }

    public static void main(String[] args) {}
}
