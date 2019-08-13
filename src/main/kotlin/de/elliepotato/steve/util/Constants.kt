package de.elliepotato.steve.util

/**
 * @author Ellie for VentureNode LLC
 * at 04/02/2018
 */
enum class Constants(val idLong: Long) {

    PRESUMED_SELF(409767305267118081),

    GUILD_MELON(303673504468041730),
    GUILD_BISECT(303673296929685504),
    GUILD_DEV(322276536407556117),

    // will try to use categories

    CAT_MELON_INFO(413087539377733642),
    CAT_BISECT_INFO(413087039890653184),

    CAT_MELON_CHAT(497480211990970388),
    CAT_BISECT_CHAT(413087332791484416),

    CAT_MELON_HELP(497480211990970388),
    CAT_BISECT_HELP(609086180935335946),

    //CAT_MELON_GAME(), COMING SOON
    CAT_BISECT_GAME(609448813433978930),

    CAT_MELON_BOTS(610820015310241792),
    CAT_BISECT_BOTS(603982530739175444),

    CAT_MELON_STAFF(413087525435998209),
    CAT_BISECT_STAFF(413087228370223135),

    // For auto mod log
    CHAT_MELON_MOD(413086166397616139),
    CHAT_BISECT_MOD(413086115717709824),

    CHAT_MELON_GENERAL(304743612212707339),
    CHAT_BISECT_GENERAL(304744201718071296),

    CHAT_MELON_HELP_SPIGOT_CRAFT_VAN(497480273638981652),
    CHAT_MELON_HELP_MODDED(497480309458075648),
    CHAT_MELON_HELP_OTHER(497482525908664349),

    CHAT_BISECT_HELP_SPIGOT_CRAFT_VAN(497479599114944527),
    CHAT_BISECT_HELP_MODDED(497479703641325568),
    CHAT_BISECT_HELP_OTHER(497482450025316356),

    //CHAT_MELON_AD(320100020978450433),
    //CHAT_BISECT_AD(320099659890556931),

    // These are stored to check for tagging
    STAFF_MAX(303668659723829261),
    STAFF_JACOB(138445436091498497),
    STAFF_ANDREW(281344565753937932),
    STAFF_AMBER(139565868060377089),
    STAFF_DANIEL(325791100897853442),
    STAFF_JOSH(186901518211874827),

    ;

    override fun toString(): String {
        return "<#$idLong>"
    }

    fun isHelpChannel(): Boolean {
        return Regex("CHAT_(BISECT|MELON)_HELP_.*").matches(name)
    }

}