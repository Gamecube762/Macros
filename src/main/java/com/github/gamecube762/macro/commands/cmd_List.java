package com.github.gamecube762.macro.commands;

import com.github.gamecube762.macro.services.MacroManger;
import com.github.gamecube762.macro.spongePlugin.MMService;
import com.github.gamecube762.macro.util.MacroUtils;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Created by Gamecube762 on 10/6/2016.
 */
public class cmd_List implements CommandExecutor {

    public static final CommandSpec spec = CommandSpec.builder()
            .permission("macro.command.list")
            .description(Text.of("List your available macros."))
            .arguments()
            .executor(new cmd_List())
            .build();

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException {
        MacroManger mm = MacroUtils.getMacroManager().orElse(MMService.me);

        Collection<Text> texts = ( source instanceof Player ? mm.getAccessableMacros((Player)source) : mm.getStorage() )
                .stream()
                .map(
                        macro -> Text.builder(macro.getPublicName())
                                .onShiftClick(TextActions.insertText(macro.getPublicName()))
                                .onHover(
                                        TextActions.showText(
                                                Text.of(
                                                        TextColors.GREEN,
                                                        String.format("%s %s\nAuthor: %s\n Description: %s",
                                                                macro.getName(),
                                                                macro.getArgs().stream().collect(Collectors.joining(" ")),
                                                                macro.getAuthorName(),
                                                                macro.getDescription()))
                                        )
                                )
                                .build()
                )
                .sorted((o1, o2) -> o1.toPlain().compareTo(o2.toPlain()))
                .collect(Collectors.toList());

        Text.Builder out = Text.of(TextColors.RESET, "Macros: ").toBuilder();
        boolean a = false;
        for (Text text : texts) {
            if (a) out.append(Text.of(", ")); else a = true;
            out.append(text);
        }

        source.sendMessage(out.build());
        return CommandResult.builder().queryResult(texts.size()).successCount(1).build();
    }

    /**todo sort: ownership:name
     * My Macros
     * Shared Macros
     * Public Macros
     */

}
