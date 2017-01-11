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

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Created by Gamecube762 on 10/6/2016.
 */
public class cmd_Edit implements CommandExecutor {

    public static final CommandSpec spec = CommandSpec.builder()
            .permission("macro.command.edit")
            .description(Text.of("Edit a Macro."))
            .arguments(
                    MacroArguments.macro(Text.of("macro"), MacroArguments.MacroCommandElement.Extra.CHECKEXISTS),
                    GenericArguments.integer(Text.of("line")),//todo custom int or '1,7' (min, max)
                    GenericArguments.optional(
                            GenericArguments.remainingJoinedStrings(Text.of("text"))
                    )
            )
            .executor(new cmd_Edit())
            .build();

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException {//todo test | todo "arg1 word2" arg2 "arg3 word3"
        int line = context.<Integer>getOne("line").get();
        String text = context.<String>getOne("text").orElse("");

        Macro m = context.<Macro>getOne("macro").get();

        if (!MacroUtils.canUse(source, m, MacroUtils.Permission.EDIT)) {
            source.sendMessage(Text.of("You cannot edit this macro."));
            return CommandResult.empty();
        }

        ArrayList<String> list = m.getActions().stream().collect(Collectors.toCollection(ArrayList::new));

        while (line >= list.size())
            list.add("");

        list.set(line, text);
        m.setActions(list);
        MacroUtils.viewMacro(source, m, line);

        return CommandResult.success();
    }

}
