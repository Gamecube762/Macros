package com.github.gamecube762.macro.spongePlugin;

import com.github.gamecube762.macro.services.MacroManger;
import com.github.gamecube762.macro.util.Macro;
import com.github.gamecube762.macro.util.MacroUtils;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Created by gamec on 10/31/2016.
 */
public class ChatHandler {

    SpongeLoader plugin;

    public ChatHandler(SpongeLoader plugin) {
        this.plugin = plugin;
    }

    @Listener(order = Order.FIRST)
    public void onChat(MessageChannelEvent.Chat e, @First Player source) {
        List<String> shortcuts = plugin.configRoot.getNode("chatShortcut").getList(Object::toString);

        String found = "";
        for (String b : shortcuts)
            if (e.getRawMessage().toPlain().toLowerCase().startsWith(b)) {
                found = b;
                break;
            }

        if (found.isEmpty()) return;//not a Macro Shortcut

        e.setMessageCancelled(true);

        String name, macID;
        LinkedList<String> args = new LinkedList<>(Arrays.asList(e.getRawMessage().toPlain().split(" ")));

        name = args.get(0);
        args.remove(0);

        macID = name.replace(found, "");

        if (macID.isEmpty()) {//If there was a space after the shortcut, we grab the group after it.
            macID = args.get(0);
            args.remove(0);
        }

        MacroManger mm = MacroUtils.getMacroManager().orElse(MMService.me);

        Optional<Macro> om = mm.getMacro(source.getUniqueId(), macID);

        if (!om.isPresent()) {
            source.sendMessage(Text.of(TextColors.RED, String.format("Unknown Macro \"%s\".", macID)));
            return;
        }

        Macro m = om.get();

        if (!MacroUtils.canUse(source, m)) {
            source.sendMessage(Text.of(TextColors.RED, "You cannot access this macro."));
            return;
        }

        if (m.isEmpty()) {
            source.sendMessage(Text.of(TextColors.RED, "Macro is empty."));
            return;
        }

        try {mm.runMacro(source, m, args);}
        catch (IllegalArgumentException ex) {source.sendMessage(Text.of(TextColors.RED, ex.getMessage()));}
    }
}
