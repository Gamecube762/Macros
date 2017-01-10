package com.github.gamecube762.macro.util.commands;

import com.github.gamecube762.macro.spongePlugin.MMService;
import com.github.gamecube762.macro.util.Macro;
import com.github.gamecube762.macro.util.MacroUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by gamec on 11/1/2016.
 */
public class MacroCommands {

    public static class Factory {

        private static Object obj;
        private static Map<MacroCommand, Boolean> map = new ConcurrentHashMap<>();

        public static MacroCommand toMacroCommand(Macro macro) {
            for (MacroCommand mc : map.keySet())
                if (mc.macro == macro)
                    return mc;

            MacroCommand mc;
            mc = new MacroCommand(macro);
            map.put(mc, false);

            return mc;
        }

        public static boolean registerMacroCommand(Macro m) {
            return registerMacroCommand(toMacroCommand(m));
        }

        public static boolean registerMacroCommand(MacroCommand mc) {
            if (map.containsKey(mc) && map.get(mc)) return false;

            if (map.entrySet().stream().anyMatch(e -> e.getValue() && e.getKey().getName().equalsIgnoreCase(mc.getName())))
                return false;

            mc.mapping = Sponge.getCommandManager().register(
                    obj,
                    mc.getCommandSpec(),
                    mc.getName()
            ).orElse(null);
            map.put(mc, true);
            return true;
        }

        public static void unregisterMacroCommand(MacroCommand mc) {
            if (mc.mapping == null || !map.containsKey(mc) || !map.get(mc)) return;

            Sponge.getCommandManager().removeMapping(mc.mapping);
            map.put(mc, true);
        }

        public static Set<Macro> getRegisteredMacros() {
            return map.keySet().stream().map(MacroCommand::getMacro).collect(Collectors.toSet());
        }

        public static Set<MacroCommand> getRegisteredMacroCommands() {
            return  map.keySet();
        }

        public static void setO(Object o) {
            if (obj != null) throw new IllegalArgumentException();

            obj = o;
        }

    }

    public static class MacroCommand {

        Macro macro;
        CommandSpec spec = null;
        CommandMapping mapping = null;

        private MacroCommand(Macro macro) {
            this.macro = macro;
        }

        public String getName() {
            return macro.getName();
        }

        public String getID() {
            return macro.getID();
        }

        public Macro getMacro() {
            return macro;
        }

        public CommandSpec getCommandSpec() {
            if (spec == null) {
                CommandSpec.Builder builder = CommandSpec.builder();

                if (false) builder.permission("macro.command.edit");//todo conf

                builder.description(Text.of("MacroCommand: " + macro.getDescription()));
                builder.arguments(GenericArguments.optional(GenericArguments.remainingJoinedStrings(Text.of("arguments"))));
                builder.executor((source, context) -> {
                    if (!MacroUtils.canUse(source, macro)) {
                        source.sendMessage(Text.of(TextColors.RED, "You cannot access this macro."));
                        return CommandResult.empty();
                    }

                    if (macro.isEmpty()) {
                        source.sendMessage(Text.of(TextColors.RED, "Macro is empty."));
                        return CommandResult.empty();
                    }

                    try {
                        MacroUtils.getMacroManager().orElse(MMService.me).runMacro(source, macro, context.<String>getOne("arguments").orElse("").split(" "));}
                    catch (IllegalArgumentException e) {source.sendMessage(Text.of(TextColors.RED, e.getMessage()));}
                    return CommandResult.success();
                });

                spec = builder.build();
            }

            return spec;
        }
    }

}
