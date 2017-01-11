package com.github.gamecube762.macro.commands;

import com.github.gamecube762.macro.util.Macro;
import com.github.gamecube762.macro.util.MacroUtils;
import com.github.gamecube762.macro.util.commands.MacroArguments;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

/**
 * Created by Gamecube762 on 10/6/2016.
 */
public class cmd_SetPublic implements CommandExecutor {

    public static final CommandSpec spec = CommandSpec.builder()
            .permission("macro.command.setPublic")
            .description(Text.of("Set a macro to be publicly available."))
            .arguments(
                    MacroArguments.macro(Text.of("macro"), MacroArguments.MacroCommandElement.Extra.CHECKEXISTS),
                    MacroArguments.bool(Text.of("bool"))
            )
            .executor(new cmd_SetPublic())
            .build();

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException {
        Macro m = context.<Macro>getOne("macro").get();

        if (!MacroUtils.canUse(source, m, MacroUtils.Permission.EDIT)) {
            source.sendMessage(Text.of("You cannot edit this macro."));
            return CommandResult.empty();
        }

        m.setPublic(context.<Boolean>getOne("bool").get());
        MacroUtils.viewMacro(source, m);

        return CommandResult.success();
    }

}
