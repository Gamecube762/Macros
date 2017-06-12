package com.github.gamecube762.macro.util.actionCommands.commands;

import com.github.gamecube762.macro.util.MacroRunner;
import com.github.gamecube762.macro.util.SpecialUtils;
import com.github.gamecube762.macro.util.actionCommands.ActionCommand;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;

import java.util.List;

/**
 * Echo command. Prints the provided message in chat.
 *
 * Usage: .Echo: I love waffles!
 * Result: I love waffles!
 *
 * Created by kyle on 4/14/17.
 */
public class Echo implements ActionCommand {
    @Override
    public void parse(CommandSource source, List<String> args, MacroRunner runner) {
        source.sendMessage(Text.of(SpecialUtils.toString(args)));
    }
}
