Macros
===
A SpongeAPI plugin

This plugin is currently in Development. It has many bugs and features to be added.

You are free to test this plugin and make suggestions, [report bugs](https://github.com/Gamecube762/Macros/issues), or even contribute changes through [Pull Requests](https://github.com/Gamecube762/Macros/pulls)!

The code is very messy, a lot of it was in a "Get it working" stage.

How to use Macros
===
Creating and editing:
---

Macros can be created with `/macro create <macroName>` and edited with `/macro edit <macroName> <lineNumber> <Command to run>`

```
/macro create jail
/macro edit jail 0 tp {0} 100 64 100
/macro edit jail 1 gamemode 2 {0}
/macro edit jail 2 tellraw {0} ["",{"text":"You have been sent to Jail!","color":"red","bold":true}]
```

Alternatively you can use `/quickMacro <macroName> <actions>`. Lines can be seperated by `;`.

The above example also can be condensed to this:
```
/quickmacro jail tp {0} 100 64 100; gamemode 2 {0}; tellraw {0} ["",{"text":"You have been sent to Jail!","color":"red","bold":true}]
```

Using:
---

There are 3 ways you can use a macro. 

Command: `/macro use <macro> [arguments]`

Chat Shortcut: `.m:<macro> [arguments]`

As it's own command* : `/<macro> [arguments]`

In order for you to use a macro as a command, you will need to use `/macro setAsCommand <macro>`.

Arguments:
---
Arguments can be used to make macros more dynamic.

Arguments are formatted as `{#}` or `{#orVALUE}`. With Brackets containing them, a number to define which argument to use and an option `orVALUE` to use if no argument is present.

If an argument contains an optional value, you can use `~` when using the macro to use the argument's value.

Advanced Macro Usage
===

Advanced Arguments:
---

Using `{=#=}` will use all arguments after `#`. 

Using `{==}` will return all arguments passed by the player. This is good for `say ANNOUNCEMENT: {==}`

Special Arguments:
---

Special Arguments are arguments that are filled in by the plugin rather than the player. These can provide usefull information without bothering the player to provide them.

Argument | Description
---|---
{user}|Get the name of the user using this Macro.
{userID}|Get the UUID of the user using this Macro.
{macroName}|Get the name of this Macro.
{macroID}|Get the ID of this Macro.
{authorID}|Get this Macro's Author's UUID.
{authorName}|Get this Macro's Author's username.

Action Commands
---

Action Commands are special Macro-only commands allowing expanding what Macros can do.

Action commands are formatted differently than normal commands. Current formatting is `.<command>: [Args]` Notice the `.` and `:` before and after the command.

Action Commands are NOT case-sensitive.

Command | Usage | Description
---|---|---
\# | .\#: [Message] | Create a comment that is ignored when parsing the Macro.
Done | .done: [Message] | End the Macro with an optional message
Echo | .echo: [Message] | Send a message to the user of the macro
Goto | .goto: \<LineNumber> | Set the next line of the Macro to run
LogD | .logd: \<Message> | Log a [DEBUG] message to console
LogE | .loge: \<Message> | Log a [Error] message to console
LogI | .logi: \<Message> | Log a [Info] message to console
LogW | .logw: \<Message> | Log a [WARN] message to console
Perm | .perm: \<PermissionNode> | Run a permission check on the User. Will end macro if check failed.
Sudo | .sudo: \<Command> | DANGEROUS! Run command as Console. Requires user to have permmision `macro.use.other.sudo.<UUID>.<MacroName>` to use sudo on that macro.
Wait | .wait: \<Number> [Time] | Wait X amount of time before running the next command. Times accepted: Ticks, Seconds, Minutes, Hours

Installing
===
 * Download the macros.jar
 * Place it in your server's `mods` folder
 * Start the server

 This plugin can run out-of-the box with no aditional config editing needed.

Config//todo
===
Currently the config is a work in progress, it may change by the full release.

Config folder Format:
```
\configs\macros\macros.conf
\configs\macros\storage.conf
\configs\macros\custom\
```
macros.conf
---
This file contains all the settings for the plugin.

Current Settings:
```
chatShortcut=[
    ".m:",
    ".macro:"
]
maxCommandsPerTick=10
maxJavaErrors=10
maxTimePerTick=5
startMacrosOnTheSameTick=true
```
Name|Discription|Type
---|---|---
chatShortcut | Allows you to define custom chat shortcuts | String Array
maxCommandsPerTick | Max commands to run per Tick | Integer
maxJavaErrors | Max amount of Exceptions thrown before aborting | Integer
maxTimePerTick | Max ammount of time(ms) for a macro to run on a single tick | Integer
startMacrosOnTheSameTick | Start macros on the same tick they are called | boolean

storage.conf
---
This file is where all the macros are stored

/custom/
---
This folder is for external macros.

External macros are macros you can export and easily transfer them between servers or for easier manual editing.

Exported macros are named in the format `AuthorUUID.MacroName.mcmacro`; these files can be renamed.

Permissions
===
Commands
---

Commands are simple: `macro.command.<subcommand>`

Macros
---
By default, a Macro can only be used by Console and it's Author.

To allow a user to use another's macro: `macro.use.other.<UUID>.<Macro>`

You can restict the access they have on another's macro by changing what permission you give them.

`macro.<Usage>.other.<UUID>.<Macro>`

Usages:
```
use
view
edit
delete
```

Server Security
===

Anyone can create and fill a macro, this means anyone can add `/op 1337h@ck3rz` to their macro but it wont op them. Not unless they themselves has permission to use `/op`, then they wouldn't need to use a macro to do it..

Macros are ran through the player that ran them, meaning if they dont have permission to a command, they wont be able to run that command.

---

Admins, be aware of the contents of the macro before running it. If you have permissions to higher commands, those commands can run!

`/macros view <macro> <line#>` lets you view the contents of a macro so you know what the macro is doing.

---

Sudo is a new Macro Action that can run commands as Console. This is usefull for admins and automation but this can be usefull to malicious players in gaining access to the server Console.

To counter act players being able to create Sudo Macros, the Macro User requires `macro.use.other.sudo.<MacroID>` to use sudo for that macro. This helps if a user has access to using Sudo on one macro and not another. This also means an Administrator will need to check the macro before allowing permission to use sudo on it.

Other
===

Saving
---
The plugin automatically saves macros during the save cycle of Worlds. By default, worlds save every 900 ticks(45 seconds). This can be changed by `auto-save-interval` in global.conf.

You can manually load and save by using `/macro save` and `/macro load`.

Planned features
---

### Security checking - InDev:

* Checks for attempt at using blacklisted commands(op, ban, whitelist, ext)
* Checks for infinite loops - Calling the macro within the macro
* Warn an admin of malicious macros
* Require admin's approval above a certain warning level

Useful links:
---
Thread: https://forums.spongepowered.org/t/clipboard-alpha-macros/16761

Releases: https://github.com/Gamecube762/Macros/releases/

IssueTracker: https://github.com/Gamecube762/Macros/issues
