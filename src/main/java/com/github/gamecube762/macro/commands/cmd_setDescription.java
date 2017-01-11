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
public class cmd_setDescription implements CommandExecutor {

    public static final CommandSpec spec = CommandSpec.builder()
            .permission("macro.command.setDescription")
            .description(Text.of("Set the description of a macro"))
            .arguments(
                    MacroArguments.macro(Text.of("macro"), MacroArguments.MacroCommandElement.Extra.CHECKEXISTS),
                    GenericArguments.optional(
                            GenericArguments.remainingJoinedStrings(Text.of("text"))
                    )
            )
            .executor(new cmd_setDescription())
            .build();

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException {
        String text = context.<String>getOne("text").orElse("");
        Macro m = context.<Macro>getOne("macro").get();

        if (!MacroUtils.canUse(source, m, MacroUtils.Permission.EDIT)) {
            source.sendMessage(Text.of("You cannot edit this macro."));
            return CommandResult.empty();
        }

        m.setDescription(text);
        source.sendMessage(Text.of(String.format("Set description to \"%s\"", text)));
        return CommandResult.success();
    }

}
