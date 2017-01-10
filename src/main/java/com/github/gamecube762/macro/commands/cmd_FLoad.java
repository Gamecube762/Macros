package com.github.gamecube762.macro.commands;

import com.github.gamecube762.macro.spongePlugin.MMService;
import com.github.gamecube762.macro.util.MacroUtils;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.io.IOException;

/**
 * Created by Gamecube762 on 10/6/2016.
 */
public class cmd_FLoad implements CommandExecutor {

    public static final CommandSpec spec = CommandSpec.builder()
            .permission("macro.command.fload")
            .description(Text.of("Force load macros to config."))
            .executor(new cmd_FLoad())
            .build();

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException {
        try {MacroUtils.getMacroManager().orElse(MMService.me).loadMacros();}
        catch (IOException e) {
            source.sendMessage(Text.of(TextColors.RED, e.getMessage()));
            return CommandResult.empty();
        }

        return CommandResult.success();
    }

}
