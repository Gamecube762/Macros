package com.github.gamecube762.macro.util.actionCommands.commands;

import com.github.gamecube762.macro.util.MacroRunner;
import com.github.gamecube762.macro.util.MacroUtils;
import com.github.gamecube762.macro.util.SpecialUtils;
import com.github.gamecube762.macro.util.actionCommands.ActionCommand;
import org.spongepowered.api.command.CommandSource;

import java.util.List;

/**
 * LogE command. Logs an [ERROR] to console.
 *
 * Usage: .loge: I love waffles!
 * Result: <--In Console:--> [ERROR] I love waffles!
 *
 * Created by kyle on 4/14/17.
 */
public class LogE implements ActionCommand {
    @Override
    public void parse(CommandSource source, List<String> args, MacroRunner runner) {
        if (args.isEmpty()) return;
        MacroUtils.getMacroManager().get().getLogger().error(SpecialUtils.toString(args));
    }
}
