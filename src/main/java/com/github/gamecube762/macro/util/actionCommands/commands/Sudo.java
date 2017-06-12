package com.github.gamecube762.macro.util.actionCommands.commands;

import com.github.gamecube762.macro.spongePlugin.MMService;
import com.github.gamecube762.macro.util.MacroRunner;
import com.github.gamecube762.macro.util.MacroUtils;
import com.github.gamecube762.macro.util.SpecialUtils;
import com.github.gamecube762.macro.util.actionCommands.ActionCommand;
import com.github.gamecube762.macro.util.actionCommands.ActionCommandException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;

import java.util.List;

/**
 * Sudo command. DANGEROUS! - Runs command as console
 *
 * Usage: .sudo: give Gamecube762 Diamond
 * Result: Console gave Gamecube762 Diamond
 * Requires: macro.use.other.sudo.MACRO_ID
 *
 * Users require special permission to use sudo for THAT macro. This allows admins to moderate what scripts can be ran with sudo.
 *
 * Created by kyle on 4/14/17.
 */
public class Sudo implements ActionCommand {
    @Override
    public void parse(CommandSource source, List<String> args, MacroRunner runner) throws ActionCommandException{
        if (args.isEmpty())
            throw new ActionCommandException("Usage: .sudo: <Command to run as console>");

        String perm = String.format("macro.use.other.sudo.%s.%s", runner.getMacro().getAuthorUniqueId(), runner.getMacro().getName());
        if (!source.hasPermission(perm))
            throw new ActionCommandException(String.format("User requires \"%s\' to run this command.", perm), true);//todo Config option to lock whole macro if user doesnt have sudo perms

        String out = SpecialUtils.toString(args);

        MacroUtils.getMacroManager().orElse(MMService.me).getLogger().warn("Macro %s used by %s is Sudoing: %s", runner.getMacro().getID(), source.getName(), out);
        Sponge.getCommandManager().process(Sponge.getServer().getConsole(), out);
    }
}
