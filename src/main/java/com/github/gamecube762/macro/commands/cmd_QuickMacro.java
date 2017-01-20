package com.github.gamecube762.macro.commands;

import com.github.gamecube762.macro.spongePlugin.MMService;
import com.github.gamecube762.macro.util.Macro;
import com.github.gamecube762.macro.util.MacroAuthor;
import com.github.gamecube762.macro.util.MacroUtils;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Arrays;

/**
 * Created by Gamecube762 on 10/6/2016.
 */
public class cmd_QuickMacro implements CommandExecutor {

    public static final CommandSpec spec = CommandSpec.builder()
            .permission("macro.command.quickMacro")
            .description(Text.of("Quickly create a macro. Lines are separated by \";\""))
            .arguments(
                    GenericArguments.string(Text.of("name")),
                    GenericArguments.optional(
                            GenericArguments.remainingJoinedStrings(Text.of("text"))
                    )
            )
            .executor(new cmd_QuickMacro())
            .build();

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException {
        String name = context.<String>getOne("name").get();
        MacroAuthor author =
                source instanceof Player ?
                        MacroAuthor.getOrCreate(
                                ((Player) source).getUniqueId(), source.getName()
                        )
                        : MacroAuthor.consoleAuthor;

        Macro m;

        try {m = MacroUtils.CreateEmptyMacro(name, author);}
        catch (IllegalArgumentException e) {
            source.sendMessage(Text.of(TextColors.RED, e.getMessage()));
            return CommandResult.empty();
        }

        m.setActions(
                Arrays.asList(
                        context.<String>getOne("text")
                                .orElse("")
                                .split("(;)")
                )
        );

        MacroUtils.getMacroManager().orElse(MMService.me).registerMacro(m);
        MacroUtils.viewMacro(source, m);

        return CommandResult.success();
    }

}
