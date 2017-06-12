package com.github.gamecube762.macro.util.actionCommands.commands;

import com.github.gamecube762.macro.util.MacroRunner;
import com.github.gamecube762.macro.util.SpecialUtils;
import com.github.gamecube762.macro.util.actionCommands.ActionCommand;
import com.github.gamecube762.macro.util.actionCommands.ActionCommandException;
import org.spongepowered.api.command.CommandSource;

import java.util.List;

/**
 * Wait command. Waits X many (Scale) before running the next Action
 *
 * Usage: .wait: 5 seconds
 * Result: <-waits 5 seconds->
 *
 * Created by gamec on 6/8/2017.
 */
public class Wait implements ActionCommand {

    @Override
    public void parse(CommandSource source, List<String> args, MacroRunner runner) throws ActionCommandException{
        if (args.isEmpty())
            throw new ActionCommandException("Usage: .wait: <number> [Ticks|Seconds|Minutes|Hours]");

        long time = SpecialUtils.parseInt(args.get(0)).orElseThrow(() -> new ActionCommandException("Not A Number!"));
        String scale = (args.size() > 1) ? args.get(1).toLowerCase() : "ticks";

        switch (scale) {
            default: throw new ActionCommandException("Unknown scale.\nUsage: .wait: <number> [Ticks|Seconds|Minutes|Hours]");
            case "hour":    time *= 60;
            case "minute":  time *= 60;
            case "second":  time *= 20;
            case "ticks":   break;//todo time \= configTickRate
        }

        runner.setWaitingTicks(time);
    }

}
