package com.github.gamecube762.macro.util;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.profile.GameProfile;

import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Created by gamec on 10/16/2016.
 *
 * MacroAuthor contains the info to Identify the Author of a macro.
 */
public class MacroAuthor {

    private static HashSet<MacroAuthor> cache = new HashSet<>();

    public static final UUID consoleUUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    public static final String consoleName = "Console";
    public static final GameProfile consoleProfile = GameProfile.of(consoleUUID, consoleName);
    public static final MacroAuthor consoleAuthor = getOrCreate(consoleUUID, consoleName);

    /**
     * Get the cache of MacroAuthors
     *
     * @return HashSet of MacroAuthors
     */
    public static HashSet<MacroAuthor> getCache() {
        return cache;
    }

    /**
     * Get a MacroAuthor from a UUID
     *
     * @param uuid Author's UUID
     * @return Optional of MacroAuthor
     */
    public static Optional<MacroAuthor> get(UUID uuid) {
        for (MacroAuthor a : cache)
            if (a.getUniqueId() == uuid)
                return Optional.of(a);

        return Optional.empty();
    }

    /**
     * Get a MacroAuthor or create one from a UUID and Name
     *
     * @param uuid Author's UUID
     * @param name Author's Name
     * @return MacroAuthor
     */
    public static MacroAuthor getOrCreate(UUID uuid, String name) {
        Optional<MacroAuthor> oA = get(uuid);

        if (oA.isPresent())
            return oA.get();

        MacroAuthor a = new MacroAuthor(uuid, name);
        cache.add(a);
        return a;
    }

    //=========================
    // Static end; class start
    //=========================

    private UUID uuid;
    private String name;

    protected GameProfile gameProfile;
    protected CompletableFuture<GameProfile> futureGP;
    protected boolean waitingOnBatch = false;

    /**
     * Construct a new MacroAuthor.
     * Use .getOrCreate(UUID uuid, String name)
     *
     * @param uuid Author's UUID
     * @param name Author's Name
     */
    private MacroAuthor(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;

        if (uuid == consoleProfile.getUniqueId())
            gameProfile = consoleProfile;
    }

    /**
     * Get the author's UniqueID.
     *
     * @return Author' UUID
     */
    public UUID getUniqueId() {
        return uuid;
    }

    /**
     * Get the author's Username.
     *
     * @return Author's Name
     */
    public String getName() {
        if (getGameProfile().isPresent() && gameProfile.getName().isPresent())
            name = gameProfile.getName().get();

        return name;
    }

    /**
     * Get the GameProfile of the Author.
     *
     * @return Optional of GameProfile
     */
    public Optional<GameProfile> getGameProfile() {
        if (waitingOnBatch)
            return Optional.empty();

        if (gameProfile == null) {

            if (futureGP == null)
                futureGP = Sponge.getServer().getGameProfileManager().get(uuid);

            if (!futureGP.isDone()) return Optional.empty();
            else
                try {
                    gameProfile = futureGP.get();
                    futureGP = null;
                }
                catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
        }

        return Optional.of(gameProfile);
    }
}
