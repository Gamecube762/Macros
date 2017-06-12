package com.github.gamecube762.macro.util.actionCommands.commands;

import com.github.gamecube762.macro.util.MacroRunner;
import com.github.gamecube762.macro.util.MacroUtils;
import com.github.gamecube762.macro.util.SpecialUtils;
import com.github.gamecube762.macro.util.actionCommands.ActionCommand;
import org.spongepowered.api.command.CommandSource;

import java.util.List;

/**
 * LogI command. Logs an [INFO] to console.
 *
 * Usage: .logi: I love waffles!
 * Result: <--In Console:--> [INFO] I love waffles!
 *
 * Created by kyle on 4/14/17.
 */
public class LogI implements ActionCommand {
    @Override
    public void parse(CommandSource source, List<String> args, MacroRunner runner) {
        if (args.isEmpty()) return;
        MacroUtils.getMacroManager().get().getLogger().info(SpecialUtils.toString(args));
    }
}
