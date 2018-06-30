package de.elliepotato.steve.status.minecraft;

/**
 * @author Ellie for VentureNode LLC
 * at 22/05/2018
 */
public enum MCService {

    MINECRAFT_NET("minecraft.net", "Minecraft.net"),
    SESSION("session.minecraft.net", "Sessions"),
    ACCOUNT("account.mojang.com", "Accounts"),
    AUTH("authserver.mojang.com", "Auth Server"),
    SESSION_SERVER("sessionserver.mojang.com", "Session Server"),
    API("api.mojang.com", "API"),
    TEXTURES("textures.minecraft.net", "Textures"),
    MOJANG_COM("mojang.com", "Mojang.com"),;

    private String raw, prettyName;

    MCService(String raw, String prettyName) {
        this.raw = raw;
        this.prettyName = prettyName;
    }

    public String getRaw() {
        return raw;
    }

    public String getPrettyName() {
        return prettyName;
    }

}
