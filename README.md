Macros
===
A SpongeAPI plugin

This plugin is currently in Development. It has many bugs and features to be added.

You are free to test this plugin and make suggestions, [report bugs](https://github.com/Gamecube762/Macros/issues), or even contribute changes through [Pull Requests](https://github.com/Gamecube762/Macros/pulls)!

The code is very messy, a lot of it was in a "Get it working" stage.

How to use
===
Creating and editing:
---
```
/macro create jail
/macro edit jail 0 tp {0} 100 64 100
/macro edit jail 1 gamemode 2 {0}
/macro edit jail 2 tellraw {0} ["",{"text":"You have been sent to Jail!","color":"red","bold":true}]
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

Advanced Arguments:
---

You can grab the name of the user using the macro with `{User}`

Using `{=#=}` will use all arguments after `#`. 

Using `{==}` will return all arguments passed by the player. This is good for `say ANNOUNCEMENT: {==}`

Installing
===
 * Download the macros.jar
 * Put it in your `mods` folder
 * Start the server

 This plugin can run out-of-the box with no aditional config editing needed.

Config
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

Other
===

Saving
---
The plugin automatically saves macros during the save cycle of Worlds. By default, worlds save every 900 ticks(15 secconds). This can be changed by `auto-save-interval` in global.conf.

You can manually load and save by using `/macro save` and `/macro load`.

Security
---
Anyone can create and fill a macro, this means anyone can add `/op 1337h@ck3rz` to their macro but it wont op them. Not unless they themselves has permission to use `/op`, then they wouldn't need to use a macro to do it..

Macros are ran through the player that ran them, meaning if they dont have permission to a command, they wont be able to run that command.

---

Admins, be aware of the contents of the macro before running it. If you have permissions to higher commands, those commands can run!

`/macros view <macro> <line#>` lets you view the contents of a macro so you know what the macro is doing.


Planned features
---

Security checking:

* Checks for attempt at using blacklisted commands(op, ban, whitelist, ext)
* Checks for infinite loops - Calling the macro within the macro

Macro Actions:

* wait: 20t - waits 20ticks before continueing
* echo: <message> - Prints a message
* logi: <message> - Logs to console | logi = info, logw = warn, logd = debug, loge = error
* sudo: <command> - run a command as console | requires extra permission
* #: comment

Useful links:
---
Thread: https://forums.spongepowered.org/t/clipboard-alpha-macros/16761

Releases: https://github.com/Gamecube762/Macros/releases/

IssueTracker: https://github.com/Gamecube762/Macros/issues