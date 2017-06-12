package com.github.gamecube762.macro.util.actionCommands;

/**
 * Created by gamec on 6/8/2017.
 */
public class ActionCommandException extends Exception {

    private boolean endMacro;

    public ActionCommandException(String message) {
        this(message, false);
    }

    public ActionCommandException(String message, boolean endMacro) {
        super(message);
        this.endMacro = endMacro;
    }

    public boolean shouldEndMacro() {
        return endMacro;
    }
}
