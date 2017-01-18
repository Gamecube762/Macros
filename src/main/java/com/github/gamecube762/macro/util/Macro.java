package com.github.gamecube762.macro.util;

import com.github.gamecube762.macro.spongePlugin.MMService;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Macro.
 */
public class Macro {

    public static final Pattern REGEX_Arguments = Pattern.compile("\\{\\d+(or[a-zA-Z0-9]*)?\\}");
    public static final Pattern REGEX_ID = Pattern.compile("(([a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}|[a-zA-Z0-9_]{3,16})\\.[a-zA-Z0-9_]{3,16}|[a-zA-Z0-9_]{3,16})");
    public static final Pattern REGEX_Name = Pattern.compile("[a-zA-Z0-9_]{3,16}");

    public static final TypeToken<Macro> Token_Macro = TypeToken.of(Macro.class);
    public static final TypeToken<ArrayList<Macro>> Token_MacroList = new TypeToken<ArrayList<Macro>>() {};

    private String name, description = "";
    private MacroAuthor author;
    private List<String> actions = new ArrayList<>();
    private boolean isPublic = false;
    private Path storagePath = null;

    private List<String> args = new ArrayList<>();
    private boolean requiresArgs = false;
    private int requiredArgCount = 0;

    /**
     * Construct a new Macro.
     *
     * @param name Macro name
     * @param author Macro's Author
     */
    public Macro(String name, MacroAuthor author) {
        this(name, author, Arrays.asList());
    }

    /**
     * Construct a new Macro.
     *
     * @param name Macro name
     * @param author Macro's Author
     * @param actions Macro's Actions
     */
    public Macro(String name, MacroAuthor author, List<String> actions) {
        this(name, "", author, actions);
    }

    /**
     *Construct a new Macro.
     *
     * @param name Macro name
     * @param description Macro's Description
     * @param author Macro's Author
     * @param actions Macro's Actions
     */
    public Macro(String name, String description, MacroAuthor author, List<String> actions) {
        this(name, "", author, actions, false);
    }

    /**
     *Construct a new Macro.
     *
     * @param name Macro name
     * @param description Macro's Description
     * @param author Macro's Author
     * @param actions Macro's Actions
     * @param isPublic Is the macro available for all?
     */
    public Macro(String name, String description, MacroAuthor author, List<String> actions, boolean isPublic) {
        this(name, "", author, actions, false, null);
    }

    /**
     *Construct a new Macro.
     *
     * @param name Macro name
     * @param description Macro's Description
     * @param author Macro's Author
     * @param actions Macro's Actions
     * @param isPublic Is the macro available for all?
     * @param path External file where this Macro is stored
     */
    @Deprecated //isExternal to change
    public Macro(String name, String description, MacroAuthor author, List<String> actions, boolean isPublic, Path path) {
        this.name = name;
        this.description = description;
        this.author = author;
        this.isPublic = isPublic;
        this.storagePath = path;
        setActions(actions);
    }

    /**
     * Get the Author of the macro
     *
     * @return Macro Author
     */
    public MacroAuthor getAuthor() {
        return author;
    }

    /**
     * Get the name of the Macro
     *
     * @return Macro's name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of the Macro
     *
     * @param name new Name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the description of the Macro
     *
     * @return Macro's description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the description of he Macro
     *
     * @param description new Description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get the ID of the Macro.
     * The macro's ID is the Author's UUID and the Macro's name separated by a '.'
     *
     * @return Macro's ID
     */
    public String getID() {
        return String.format("%s.%s", author.getUniqueId().toString(), name).toLowerCase();
    }

    /**
     * Get the Macro's public name.
     * Similar to getID, but using the Author's username instead to make it more user friendly.
     *
     * @return Macro's public name
     */
    public String getPublicName() {
        return String.format("%s.%s", author.getName(), name).toLowerCase();
    }

    /**
     * Get the author's UniqueID
     *
     * @return Author' UUID
     */
    public UUID getAuthorUniqueId() {
        return author.getUniqueId();
    }

    /**
     * Get the author's Username
     *
     * @return Author's Name
     */
    public String getAuthorName() {
        return author.getName();
    }

    /**
     * Get the actions for this macro to perform.
     *
     * @return Actions
     */
    public List<String> getActions() {
        return actions;
    }

    /**
     * Set the actions for this macro to perform
     *
     * @param actions List of actions
     */
    public void setActions(List<String> actions) {
        this.actions = actions;
        this.args = findArgs(actions);
        this.requiredArgCount = 0;

        int a = 0;

        //scan through arguments and count how many arguments are required.
        if (!this.args.isEmpty())
            for (int i = args.size() - 1; i >= 0; i--){
                String ar = args.get(i);
                requiresArgs = ar.length() <= 3;//Found an arg that requires a value; User input required to run macro & no longer need to count a.
                if (!requiresArgs) a++;
            }

        if (requiresArgs) requiredArgCount = args.size()-a;
    }

    /**
     * Get the arguments for this Macro
     *
     * @return List of Arguments
     */
    public List<String> getArgs() {
        return args;
    }

    /**
     * Get the storage path of the macro. Empty unless isExternal() is true.
     *
     * @return Optional of the StoragePath
     */
    public Optional<Path> getStoragePath() {
        return storagePath == null ? Optional.empty() : Optional.of(storagePath);
    }

    /**
     * Is the Macro available for anyone to use?
     *
     * @return boolean
     */
    public Boolean isPublic() {
        return isPublic;
    }

    /**
     * Is the macro stored in /custom/
     *
     * @return boolean
     */
    public boolean isExternal() {
        return storagePath != null;
    }

    /**
     * Is the macro empty?
     *
     * @return True if macro has no Actions
     */
    public boolean isEmpty() {
        return actions == null || actions.isEmpty();
    }

    /**
     * Set the macro to be public
     *
     * @param aPublic boolean
     */
    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    /**
     * Set the external file where this macro is stored
     * @param path File where macro is stored
     */
    public void setStoragePath(Path path) {
        storagePath = path;
    }

    /**
     * Does the macro have arguments?
     *
     * @return boolean
     */
    public boolean hasArgs() {
        return args != null;
    }

    /**
     * Does the macro require arguments from the user?
     *
     * @return boolean
     */
    public boolean requiresArgs() {
        return requiresArgs;
    }

    /**
     * Get how many arguments that are required by the users.
     *
     * Example:
     * 5 total arguments, only 3 are required:
     *
     * .m:Macro Arg1 Arg2 Arg3 opt1 opt2
     *
     * @return boolean
     */
    public int getRequiredArgCount() {
        return requiredArgCount;
    }

    /**
     * Finds arguments in a list of Actions
     *
     * @param in Actions
     * @return Arguments
     */
    public static List<String> findArgs(List<String> in) {
        List<String> list = new ArrayList<>();
        in.forEach(a -> {
            Matcher b = REGEX_Arguments.matcher(a);
            while (b.find())
                list.add(b.group());
        });

        return list.isEmpty() ? Collections.EMPTY_LIST : (ArrayList<String>) list.stream().distinct().sorted().collect(Collectors.toList());
    }

    public static final class Serializer implements TypeSerializer<Macro> {

        @Override
        public Macro deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
            UUID a = UUID.fromString(value.getNode("AuthorUUID").getString());
            String b = value.getNode("Name").getString();

            //get current macro and update or create a new one if it doesn't exist. | prevents mem leaks
            Macro m = MacroUtils.getMacroManager()
                    .orElse(MMService.me)
                    .getMacro(a, b)
                    .orElse(new Macro(b, MacroAuthor.getOrCreate(a, value.getNode("AuthorName").getString())));

            m.description = value.getNode("Description").getString();
            m.isPublic = value.getNode("Public").getBoolean();
            m.setActions(value.getNode("Actions").getList(TypeToken.of(String.class)));

            return m;
        }

        @Override
        public void serialize(TypeToken<?> type, Macro m, ConfigurationNode value) throws ObjectMappingException {
            value.getNode("AuthorUUID") .setValue(m.author.getUniqueId().toString());
            value.getNode("AuthorName") .setValue(m.author.getName());
            value.getNode("Name")       .setValue(m.name);
            value.getNode("Description").setValue(m.description);
            value.getNode("Public")     .setValue(m.isPublic);
            value.getNode("Actions")    .setValue(m.actions);
        }
    }

}
