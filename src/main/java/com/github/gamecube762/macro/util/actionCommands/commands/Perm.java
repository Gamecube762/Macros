package com.github.gamecube762.macro.util.actionCommands.commands;

import com.github.gamecube762.macro.util.MacroRunner;
import com.github.gamecube762.macro.util.actionCommands.ActionCommand;
import com.github.gamecube762.macro.util.actionCommands.ActionCommandException;
import org.spongepowered.api.command.CommandSource;

import java.util.List;

/**
 * Perm command. Runs a permission check on the user; if the check fails, the macro will end
 *
 * Usage: .perm: command.summon.pigking
 * Result: <-Dependant on if the user can actually summon the PigKing->
 *
 * Created by gamec on 6/8/2017.
 */
public class Perm implements ActionCommand {

    @Override
    public void parse(CommandSource source, List<String> args, MacroRunner runner) throws ActionCommandException{
        if (args.isEmpty())
            throw new ActionCommandException("Usage: .Perm: <permissionNode>");

        if (!source.hasPermission(args.get(0)))
            throw new ActionCommandException(
                    String.format(
                            "User failed permission check \"%s\" on line %s",
                            args.get(0),
                            runner.getCurrentLineNumber()
                    ),
                    true//end continuation of macro
            );
    }

}
