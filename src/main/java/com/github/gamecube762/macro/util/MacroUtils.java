package com.github.gamecube762.macro.util;

import com.github.gamecube762.macro.services.MacroManger;
import com.github.gamecube762.macro.spongePlugin.MMService;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.format.TextColors;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * Created by gamec on 10/16/2016.
 *
 * Utility class.
 */
public class MacroUtils {

    /**
     * Get the registered MacroManager
     *
     * @return Optional of the registered MacroManager
     */
    public static Optional<MacroManger> getMacroManager() {
        return Sponge.getServiceManager().provide(MacroManger.class);
    }

    /**
     * Used for updating the GameProfiles of MacroAuthors to sync up with usernname changes.
     *
     * @param authors A collection of MacroAuthors
     * @return A CompletableFuture of the GameProfiles being updated
     */
    public static CompletableFuture<Collection<GameProfile>> updateGameProfiles(Collection<MacroAuthor> authors) {
        authors.forEach(a -> a.waitingOnBatch = true);
        return Sponge
                .getServer()
                .getGameProfileManager()
                .getAllById(
                        authors
                                .stream()
                                .filter( a -> !a.waitingOnBatch )
                                .map( MacroAuthor::getUniqueId )
                                .collect( Collectors.toCollection(ArrayList::new) ),
                        true
                );
    }

    /**
     * Used for updating the GameProfiles of MacroAuthors to sync up with usernname changes.
     * Once complete, the updated GameProfiles will be applied to their Macro Authors.
     *
     * @param authors A collection of MacroAuthors
     * @param plugin Plugin required for registering a Scheduler Task
     */
    public static void updateGameProfiles_SelfManaged(Collection<MacroAuthor> authors, Object plugin) {//maybe as strea,
        CompletableFuture<Collection<GameProfile>> future = updateGameProfiles(authors);

        Sponge.getScheduler().createTaskBuilder()//self killing task once complete
                .intervalTicks(2)
                .execute(task -> {
                    if (!future.isDone()) return;

                    Collection<GameProfile> profiles;
                    try {profiles = future.get();}
                    catch (InterruptedException | ExecutionException e) {e.printStackTrace(); task.cancel(); return;}

                    authors.forEach(a -> profiles.forEach(p -> {
                        if (p.getUniqueId() != a.getUniqueId())
                            return;

                        a.waitingOnBatch = false;
                        a.gameProfile = p;
                        task.cancel();
                    }));
                })
                .submit(plugin);
    }

    /**
     * Creates a empty Macro if a Macro with the name and UUID doesn't already exist.
     *
     * Will throw IllegalArgumentException if name doesn't match "[a-zA-Z0-9_]{1,16}"
     *
     * @param name Macro's name
     *             todo param
     * @return Macro
     */
    public static Macro CreateEmptyMacro(String name, MacroAuthor author) {
        if (name.contains(" "))
            throw new IllegalArgumentException("Name must not contain a space, use \'_\'");

        int i = 0;
        Matcher matcher = Macro.REGEX_Name.matcher(name);
        while (matcher.find()) i++;
        if (i != 1)
            throw new IllegalArgumentException("Name must be alphanumeric. Use _ instead of spaces.");

        UUID ownerUUID = author.getUniqueId();

        if (MacroUtils.getMacroManager().orElse(MMService.me).getMacro(ownerUUID, name).isPresent())//todo mm
            throw new IllegalArgumentException("Macro already exists!");

        if (ownerUUID.equals(MacroAuthor.consoleUUID))
            return new Macro(name, MacroAuthor.consoleAuthor);

        return new Macro(name, author);
    }

    /**
     * Get the key of a MacroArgument.
     *
     * The macro key is the number that defines the argument.
     * {2orPig}
     * Key: 2
     * Value: Pig
     *
     * @param in MacroArgument
     * @return Key of the MacroArgument
     */
    public static String getArgKey(String in)  {
        if (in.equals("{==}")) return "0";

        Matcher m = Macro.REGEX_Arguments.matcher(in);

        if (m.find()) {
            if (m.group(2) != null) return m.group(2);
            if (m.group(4) != null) return m.group(6);
            if (m.group(8) != null) return m.group(8);
        }

        throw new IllegalArgumentException("Invalid Argument Placeholder");
    }

    /**
     * Get the value of a MacroArgument.
     *
     * The macro value is the string after 'or' in a MacroArgument
     *
     * {2orPig}
     * Key: 2
     * Value: Pig
     *
     * @param in MacroArgument
     * @return Optional of the Argument's Value
     * @throws IllegalArgumentException "Error in Arg formatting."
     */
    public static Optional<String> getArgValue(String in) {
        Matcher m = Macro.REGEX_Arguments.matcher(in);
        return  (m.find() && m.group(3) != null) ? Optional.of(m.group(3)) : Optional.empty();
    }

    /**
     * Retrieves the {@link Macro}ID from the provided string.
     *
     * @param in String to search.
     * @return The {@link Macro}ID, including UUID if UUID is present. (UUID.MacroName or MacroName)
     * @throws IllegalArgumentException "Invalid Macro name formatting." | "Invalid UUID string: %s"
     */
    public static String findMacroID(String in) {
        Matcher m = Macro.REGEX_ID.matcher(in.startsWith("{") ? in.replace("{", "").replace("}", "") : in);

        if (!m.find())
            throw new IllegalArgumentException("Invalid Macro name formatting.");

        return m.group();
    }

    public static final List<String> parseBoolean_trues  = Arrays.asList("true", "t", "yes", "y", "yay", "on", "1");
    public static final List<String> parseBoolean_falses = Arrays.asList("false", "f", "no", "n", "nay", "off", "0");

    /**
     * Parse the boolean from a string.
     *
     * @param s String
     * @return boolean
     * @throws IllegalArgumentException "Not a Boolean."
     */
    public static boolean parseBoolean(String s) {
        s = s.toLowerCase();
        if (parseBoolean_trues.contains(s)) return true;
        if (parseBoolean_falses.contains(s)) return false;
        throw new IllegalArgumentException("Not a Boolean.");
    }

    public static Text viewMacro(Macro macro) {
        return viewMacro(macro, -1);
    }

    public static void viewMacro(MessageReceiver receiver, Macro macro) {
        receiver.sendMessage(viewMacro(macro));
    }

    public static void viewMacro(MessageReceiver receiver, Macro macro, int highlightLine) {
        receiver.sendMessage(viewMacro(macro, highlightLine));
    }

    /**
     * Current:
     *
     * [22:14:41 INFO]: MacroInfo: count by Console
     * [Start]
     * 7 | say 7
     * 8 | say 8
     * 9 |
     * 10 | say 10
     * 11 | say 11
     * 12 | say 12
     * 13 | say 13
     * 14 | say 14
     * [End - 15/21]
     *
     * Todo:
     *
     * [22:14:41 INFO]: MacroInfo:
     * [Start - 'count' by Console - X Arguments    ] - Start/End vs Cont. | 'Continued'
     * [7 | say 7                                   ]
     * [8 | say 8                                   ]
     * [9 |                                         ]
     * [10 | say 10                                 ]
     * [11 | say 11                                 ]
     * {12 | say My favorite type of pie is cherr...} // triple dots if too long
     * [13 | say 13                                 ]
     * [14 | say 14                                 ]
     * [End - Showing 14/21 - Selected #12          ]
     *
     * [ = line
     * { = current line
     *
     */
    /**
     * View a Macro
     *
     * @param macro
     * @param highlightLine
     * @return
     */
    public static Text viewMacro(Macro macro, int highlightLine) {
        Text.Builder builder = Text.builder();

        if (macro.isEmpty()) {
            builder.append(Text.of(TextColors.GREEN, String.format("%s is empty.", macro.getName())));
            builder.append(Text.NEW_LINE);
            builder.append(Text.of(TextColors.GREEN, String.format("Use /macro edit %s <line#> <action>", macro.getName())));
        } else {
            int size = macro.getActions().size();

            if (highlightLine > size)
                throw new IllegalArgumentException("Line number is bigger than the amount of lines.");

            builder.append(Text.of(TextColors.GREEN, String.format("MacroInfo: %s by %s", macro.getName(), macro.getAuthorName())));

            if (macro.isPublic())
                builder.append(Text.of(" is Public."));

            builder.append(Text.NEW_LINE, Text.of(TextColors.DARK_PURPLE,  highlightLine == -1 ? "[Start]" : String.format("[Start - Line: %s]", highlightLine)));

            boolean a = false;
            int i = highlightLine == -1 || highlightLine <= 8 ? 0 : highlightLine - 3 , ii = 0;
            if (i < 0) i = 0;
            while (i < size){
                if (ii == 8) {
                    a = true;
                    break;
                }
                builder.append(
                        Text.NEW_LINE,
                        Text.builder(i + " ")
                                .color(i == highlightLine ? TextColors.GREEN : TextColors.YELLOW)
                                .append(Text.of(TextColors.DARK_PURPLE, "| "))
                                .append(Text.of(i == highlightLine ? TextColors.GREEN : TextColors.RESET, macro.getActions().get(i)))
                                .build()
                );
                i++;
                ii++;
            }

            builder.append(
                    Text.NEW_LINE,
                    Text.of(
                            TextColors.DARK_PURPLE,
                            a ? String.format("[End - %s/%s]", i-1, size) : "[End]")
            );
        }

        return builder.build();
    }


    public static boolean isAuthor(CommandSource source, Macro macro) {
        return (source instanceof ConsoleSource && MacroAuthor.consoleAuthor == macro.getAuthor()) || ((Player)source).getUniqueId().equals(macro.getAuthorUniqueId());
    }

    public static boolean canUse(CommandSource source, Macro macro) {//todo grab perm for docs
        return canUse(source, macro, Permission.USE);
    }

    public static boolean canUse(CommandSource source, Macro macro, Permission mode) {//todo
        return source instanceof ConsoleSource ||
                macro.isPublic() ||
                isAuthor(source, macro) ||
                source.hasPermission(String.format("macro.%s.other.%s.%s", mode.toString().toLowerCase(), macro.getAuthorUniqueId(), macro.getName().toLowerCase())) ||
                source.hasPermission(String.format("macro.%s.other.%s.%s", mode.toString().toLowerCase(), macro.getAuthorName(), macro.getName().toLowerCase()));
    }

    public enum Permission {
        DELETE,
        EDIT,
        USE,
        VIEW
    }

}
