package com.github.gamecube762.macro.commands;

import com.github.gamecube762.macro.util.Macro;
import com.github.gamecube762.macro.util.commands.MacroArguments;
import com.github.gamecube762.macro.util.commands.MacroCommands;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

import java.util.stream.Collectors;

/**
 * Created by Gamecube762 on 10/6/2016.
 */
public class cmd_SetAsCommand implements CommandExecutor {

    public static final CommandSpec spec = CommandSpec.builder()
            .permission("macro.command.edit")
            .description(Text.of("Set a macro to be publicly available."))
            .arguments(
                    MacroArguments.macro(Text.of("macro"), MacroArguments.MacroCommandElement.Extra.CHECKEXISTS)/*,//todo
                    MacroArguments.bool(Text.of("bool"))*/
            )
            .executor(new cmd_SetAsCommand())
            .build();

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException {
        Macro m = context.<Macro>getOne("macro").get();

        try {
            MacroCommands.Factory.registerMacroCommand(m);
            source.sendMessage(Text.of(String.format("Registered macro as a command. \"/%s%s\"", m.getName(), m.getArgs().stream().collect(Collectors.joining(" ", " ", "")))));
        } catch (IllegalArgumentException e) {
            source.sendMessage(Text.of(e.getMessage()));
        }

        return CommandResult.success();
    }

}
