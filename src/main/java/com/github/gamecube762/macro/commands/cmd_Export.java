package com.github.gamecube762.macro.commands;

import com.github.gamecube762.macro.spongePlugin.MMService;
import com.github.gamecube762.macro.util.Macro;
import com.github.gamecube762.macro.util.MacroUtils;
import com.github.gamecube762.macro.util.commands.MacroArguments;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import java.io.IOException;

/**
 * Created by Gamecube762 on 10/6/2016.
 */
public class cmd_Export implements CommandExecutor {

    public static final CommandSpec spec = CommandSpec.builder()
            .permission("macro.command.export")
            .description(Text.of("Export a Macro."))
            .arguments(MacroArguments.macro(Text.of("macro"), MacroArguments.MacroCommandElement.Extra.CHECKEXISTS))
            .executor(new cmd_Export())
            .build();

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException {
        Macro m = context.<Macro>getOne("macro").get();

        if (!MacroUtils.canUse(source, m, MacroUtils.Permission.EDIT)) {//falls under EDIT for now
            source.sendMessage(Text.of("You cannot edit this macro."));
            return CommandResult.empty();
        }

        try {
            MacroUtils.getMacroManager().orElse(MMService.me).exportMacro(m);
            source.sendMessage(
                    Text.builder(String.format("Exported macro \"%s\" to %s.mcmacro", m.getPublicName(), m.getID()))
                            .onHover(TextActions.showText(Text.of(String.format("Location: \\serverRoot\\config\\macros\\custom\\%s.mcmacro", m.getID()))))
                            .build()
            );
        }
        catch (IOException | ObjectMappingException e) {
            source.sendMessage(
                    Text.of(
                            TextColors.RED,
                            String.format(
                                    "Failed to export macro \"%s\": %s | %s",
                                    m.getPublicName(),
                                    e.getClass().getSimpleName(),
                                    e.getMessage()
                            )
                    )
            );
        }

        return CommandResult.success();
    }

}
