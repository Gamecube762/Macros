package com.github.gamecube762.macro.spongePlugin;

import com.github.gamecube762.macro.util.Macro;
import com.github.gamecube762.macro.util.MacroUtils;
import com.github.gamecube762.macro.util.MultipleObjectExceptionHandler;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;

/**
 * Created by gamec on 1/20/2017.
 */
public class MacroRunner implements Consumer<Task>  {

    //todo {User} for placeholder of the user using it.
    //todo {==} for remaining arguments | {=2=} for arguments 2 and after
    //todo #: = comment | todo document
    //todo wait: 20 = wait 20ticks before next command
    //todo wait Action | .c:Wait 20t | [#][format] | ticks, seconds, minutes, hours
    //todo configOption: custom tickrate

    int cmdNext = 0, cmdCount = 0, excCount = 0;
    int maxCMD, maxTickTime, maxErr;
    long lastTime = 0;

    SpongeLoader plugin;
    Macro macro;
    CommandSource source;
    List<String> actions, macArgs, inArgs;
    MultipleObjectExceptionHandler<String> excHandler = new MultipleObjectExceptionHandler<>();

    public MacroRunner(SpongeLoader plugin, Macro macro, CommandSource source, List<String> args) {
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
