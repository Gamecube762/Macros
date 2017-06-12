package com.github.gamecube762.macro.util.actionCommands.commands;

import com.github.gamecube762.macro.util.MacroRunner;
import com.github.gamecube762.macro.util.SpecialUtils;
import com.github.gamecube762.macro.util.actionCommands.ActionCommand;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;

import java.util.List;

/**
 * Done command. Ends the macro and echos the arguments(if any)
 *
 * Usage: .Done: We did everything sir.
 * Result: We did everything sir.
 *
 * Created by kyle on 4/14/17.
 */
public class Done implements ActionCommand {
    @Override
    public void parse(CommandSource source, List<String> args, MacroRunner runner) {
        if (!args.isEmpty())
            source.sendMessage(Text.of(SpecialUtils.toString(args)));
        ((Task)runner).cancel();
    }
}
