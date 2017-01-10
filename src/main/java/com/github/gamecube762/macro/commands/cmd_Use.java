package com.github.gamecube762.macro.commands;

import com.github.gamecube762.macro.spongePlugin.MMService;
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
import org.spongepowered.api.text.format.TextColors;

/**
 * Created by Gamecube762 on 10/6/2016.
 */
public class cmd_Use implements CommandExecutor {

    public static final CommandSpec spec = CommandSpec.builder()
            .permission("macro.command.use")
            .description(Text.of("cmd_Use a Macro."))
            .arguments(
                    MacroArguments.macro(Text.of("macro")),
                    GenericArguments.optional(
                            GenericArguments.remainingJoinedStrings(Text.of("arguments"))
                    )
            )
            .executor(new cmd_Use())
            .build();

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException {
        Macro m = context.<Macro>getOne("macro").get();

        if (m.isEmpty()) {
            source.sendMessage(Text.of(TextColors.RED, "Macro is empty."));
            return CommandResult.empty();
        }

        try {MacroUtils.getMacroManager().orElse(MMService.me).runMacro(source, m, context.<String>getOne("arguments").orElse("").split(" "));}
        catch (IllegalArgumentException e) {source.sendMessage(Text.of(TextColors.RED, e.getMessage()));}
        return CommandResult.success();
    }

}
