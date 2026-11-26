package com.example.game

enum class ProfileAvatar(
    val id: String,
    val label: String,
    val primaryColor: BlockColor,
    val accentColor: BlockColor,
    val isGooglePhoto: Boolean = false
) {
    CLASSIC_BLOCK("classic_block", "Classic", BlockColor.CYAN, BlockColor.AMBER),
    COBALT_SHIELD("cobalt_shield", "Cobalt", BlockColor.COBALT, BlockColor.CYAN),
    EMERALD_GEM("emerald_gem", "Emerald", BlockColor.EMERALD, BlockColor.CYAN),
    AMBER_CROWN("amber_crown", "Crown", BlockColor.AMBER, BlockColor.TANGERINE),
    CORAL_SPARK("coral_spark", "Coral", BlockColor.CORAL, BlockColor.AMBER),
    VIOLET_ORB("violet_orb", "Prism", BlockColor.VIOLET, BlockColor.CYAN),
    GOLD_ROBOT("gold_robot", "Mecha", BlockColor.AMBER, BlockColor.CYAN),
    GOOGLE_PHOTO("google_photo", "Google", BlockColor.COBALT, BlockColor.EMERALD, isGooglePhoto = true);

    companion object {
        val VALID_IDS: Set<String> = entries.map { it.id }.toSet()

        fun fromId(id: String?): ProfileAvatar =
            entries.firstOrNull { it.id == id } ?: CLASSIC_BLOCK
    }
}
