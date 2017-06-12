package com.github.gamecube762.macro.util.actionCommands.commands;

import com.github.gamecube762.macro.util.MacroRunner;
import com.github.gamecube762.macro.util.SpecialUtils;
import com.github.gamecube762.macro.util.actionCommands.ActionCommand;
import com.github.gamecube762.macro.util.actionCommands.ActionCommandException;
import org.spongepowered.api.command.CommandSource;

import java.util.List;

/**
 * Goto command. Sets the next line to run
 *
 * Usage: .goto: 20
 * Result: <---->
 *
 * Created by kyle on 4/14/17.
 */
public class Goto implements ActionCommand {
    @Override
    public void parse(CommandSource source, List<String> args, MacroRunner runner) throws ActionCommandException {
        if (args.isEmpty())
            throw new ActionCommandException("Usage: .goto <number>");

        int maxLines = runner.getActions().size(),
                line = SpecialUtils
                        .parseInt(args.get(0))
                        .orElseThrow(() -> new ActionCommandException("Not A Number"));

        if (line > maxLines)
            throw new ActionCommandException(String.format("Cannot goto line %s, Macro only has %s lines", line, maxLines));

        runner.setNextLineNumber(line);
    }
}
