package com.github.gamecube762.macro.commands;

import com.github.gamecube762.macro.util.Macro;
import com.github.gamecube762.macro.util.MacroUtils;
import com.github.gamecube762.macro.util.commands.MacroArguments;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

/**
 * Created by Gamecube762 on 10/6/2016.
 */
public class cmd_View implements CommandExecutor {

    public static final CommandSpec spec = CommandSpec.builder()
            .permission("macro.command.view")
            .description(Text.of("cmd_View a macro's actions."))
            .arguments(
                    MacroArguments.macro(Text.of("macro"), MacroArguments.MacroCommandElement.Extra.CHECKPERMS),
                    GenericArguments.optional(GenericArguments.integer(Text.of("lineNumber")))

            )
            .executor(new cmd_View())
            .build();

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException {
        MacroUtils.viewMacro(source, context.<Macro>getOne("macro").get(), context.<Integer>getOne("macro").orElse(-1));
        return CommandResult.success();
    }

}
