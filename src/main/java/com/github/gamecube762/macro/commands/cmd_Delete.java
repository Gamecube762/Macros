package com.github.gamecube762.macro.commands;

import com.github.gamecube762.macro.spongePlugin.MMService;
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
import org.spongepowered.api.text.format.TextColors;

/**
 * Created by Gamecube762 on 10/6/2016.
 */
public class cmd_Delete implements CommandExecutor {

    public static final CommandSpec spec = CommandSpec.builder()
            .permission("macro.command.delete")
            .description(Text.of("Delete a Macro."))
            .arguments(MacroArguments.macro(Text.of("macro"), MacroArguments.MacroCommandElement.Extra.CHECKEXISTS))
            .executor(new cmd_Delete())
            .build();

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException {
        Macro m = context.<Macro>getOne("macro").get();

        if (!MacroUtils.canUse(source, m, MacroUtils.Permission.DELETE)) {
            source.sendMessage(Text.of("You cannot delete this macro."));
            return CommandResult.empty();
        }

        MacroUtils.getMacroManager().orElse(MMService.me).removeMacro(m);
        source.sendMessage(Text.of(TextColors.RED, String.format("Deleted Macro %s.", m.getName())));

        return CommandResult.success();
    }

}
