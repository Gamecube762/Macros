package com.github.gamecube762.macro.util;

import org.spongepowered.api.command.CommandSource;

import java.util.List;

/**
 * Created by gamec on 1/26/2017.
 */
public interface MacroRunner{


    /**
     * Get the current line of macro.getActions()
     *
     * @return int
     */
    int getCurrentLine();

    /**
     * Get the how many Actions has ran this tick.
     *
     * @return int
     */
    int getTickRunCount();

    /**
     * Get the count of Exceptions Thrown during this run.
     *
     * @return int
     */
    int getErrorCount();

    /**
     * Get the max amount of actions a single tick can run.
     *
     * @return int
     */
    int getTickRunLimit();

    /**
     * Get the longest amount of time(in ms) a tick can run.
     *
     * @return int
     */
    int getMaxTickTime();

    /**
     * Get the max amount of Exceptions that can be thrown before canceling the macro.
     *
     * @return int
     */
    int getMaxErrorCount();

    /**
     * Get the time the tick started on.
     *
     * @return long
     */
    long getTickStartTime();

    /**
     * Get the macro this runner is running.
     *
     * @return Macro
     */
    Macro getMacro();

    /**
     * Get the CommandSource this macro is running as.
     *
     * @return CommandSource
     */
    CommandSource getSource();

    /**
     * Get the actions of a Macro.
     *
     * @return List<String>
     */
    List<String> getActions();

    /**
     * Get the arguments defined in the Macro.
     *
     * @return List<String>
     */
    List<String> getMacArgs();

    /**
     * Get the arguments provided by the Player.
     *
     * @return List<String>
     */
    List<String> getInArgs();

    /**
     * Get the MultipleObjectExceptionHandler that catches the Exceptions for this runner.
     *
     * @return MultipleObjectExceptionHandler
     */
    MultipleObjectExceptionHandler<String> getExcHandler();

}
