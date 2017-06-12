package com.github.gamecube762.macro.util.actionCommands;

import com.github.gamecube762.macro.util.MacroRunner;
import org.spongepowered.api.command.CommandSource;

import java.util.List;

/**
 * Created by kyle on 4/14/17.
 */
public interface ActionCommand {

    void parse(CommandSource source, List<String> args, MacroRunner runner) throws ActionCommandException;

}
