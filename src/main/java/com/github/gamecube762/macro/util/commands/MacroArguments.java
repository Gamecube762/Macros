package com.github.gamecube762.macro.util.commands;

import com.github.gamecube762.macro.spongePlugin.MMService;
import com.github.gamecube762.macro.util.Macro;
import com.github.gamecube762.macro.util.MacroUtils;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Gamecube762 on 10/6/2016.
 */
public class MacroArguments {

    public static CommandElement bool(Text text) {
        return new BooleanCommandElement(text);
    }

    public static CommandElement macro(Text text) {
        return new MacroCommandElement(text, MacroCommandElement.Output.OBJECT, null);
    }

    public static CommandElement macro(Text text, MacroCommandElement.Extra... extras) {
        return new MacroCommandElement(text, MacroCommandElement.Output.OBJECT, Arrays.asList(extras));
    }

    public static CommandElement macroID(Text text) {
        return new MacroCommandElement(text, MacroCommandElement.Output.ID, null);
    }

    public static CommandElement macroID(Text text, MacroCommandElement.Extra... extras) {
        return new MacroCommandElement(text, MacroCommandElement.Output.ID, Arrays.asList(extras));
    }

    public static CommandElement macroOptional(Text text) {
        return new MacroCommandElement(text, MacroCommandElement.Output.OPTIONAL, null);
    }

    public static CommandElement macroOptional(Text text, MacroCommandElement.Extra... extras) {
        return new MacroCommandElement(text, MacroCommandElement.Output.OPTIONAL, Arrays.asList(extras));
    }

    public static CommandElement remainingStringsAsList(Text text) {
        return new RemainingStringsCommandElement(text, false);
    }

    public static CommandElement remainingStringsAsArray(Text text) {
        return new RemainingStringsCommandElement(text, true);
    }

    //classes

    private static class BooleanCommandElement extends CommandElement {

        private BooleanCommandElement(@Nullable Text key) {
            super(key);
        }

        @Nullable
        @Override
        protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
            boolean out;
            try {out = MacroUtils.parseBoolean(args.next());}
            catch (IllegalArgumentException e) {throw args.createError(Text.of(e.getMessage()));}
            return out;
        }

        @Override
        public List<String> complete(CommandSource source, CommandArgs args, CommandContext context) {
            return Collections.emptyList();
        }

        @Override
        public Text getUsage(CommandSource src) {
            return Text.of("Boolean: true|false yes|no on|off 1|0");
        }
    }

    public static class MacroCommandElement extends CommandElement {

        private MacroCommandElement.Output output;
        private List<MacroCommandElement.Extra> extras;

        private MacroCommandElement(@Nullable Text key, MacroCommandElement.Output output, List<MacroCommandElement.Extra> extras) {
            super(key);
            this.output = output;
            this.extras = extras == null ? new ArrayList<>() : extras;
        }

        @Nullable
        @Override
        protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
            String id;

            try {id = MacroUtils.findMacroID(args.next());}
            catch (IllegalArgumentException e) {throw args.createError(Text.of(e.getMessage()));}

            Optional<Macro> om = MacroUtils.getMacroManager().orElse(MMService.me).getMacro(id);

            if ((extras.contains(MacroCommandElement.Extra.CHECKEXISTS) || extras.contains(MacroCommandElement.Extra.CHECKPERMS)) && !om.isPresent())
                throw args.createError(Text.of("Macro doesn't exist."));

            if (extras.contains(MacroCommandElement.Extra.CHECKPERMS) && !MacroUtils.canUse(source, om.get()))
                throw args.createError(Text.of("You cannot access this macro."));

            switch (output) {
                case ID: return id;
                case OBJECT: return om.orElse(null);
                case OPTIONAL: return om;
            }

            return null;//It shouldn't reach this line.
        }

        @Override
        public List<String> complete(CommandSource source, CommandArgs args, CommandContext context) {
            String filter = args.nextIfPresent().orElse("");

            return MacroUtils.getMacroManager().get()
                    .getAccessableMacros(source).stream()
                    .map(m -> MacroUtils.isAuthor(source, m) ? m.getName() : m.getPublicName())
                    .filter(s -> s.startsWith(filter))
                    .collect(Collectors.toList());
        }

        @Override
        public Text getUsage(CommandSource src) {
            return Text.of("<Macro>");
            //return Text.of("\"{UUID}.MacroName\" or \"UserName.MacroName\" or \"MacroName\" is accepted.");
        }

        public enum Extra {
            CHECKPERMS,
            CHECKEXISTS
        }

        public enum Output {
            OPTIONAL,
            OBJECT,
            ID
        }
    }

    private static class RemainingStringsCommandElement extends CommandElement {

        private boolean asArray;

        private RemainingStringsCommandElement(Text key, boolean asArray) {
            super(key);
            this.asArray = asArray;
        }

        @Nullable//todo
        @Override//returns either a String(not List<String>) if list only has 1 or null...
        protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
            List<String> list = new ArrayList<>();
            while (args.hasNext())
                list.add(args.next());
            return asArray ? list.toArray() : list;
        }

        @Override
        public List<String> complete(CommandSource source, CommandArgs args, CommandContext context) {
            return Collections.emptyList();
        }

        @Override
        public Text getUsage(CommandSource src) {
            return Text.of("\"{UUID}.MacroName\" or \"UserName.MacroName\" or \"MacroName\" is accepted.");
        }
    }




}
