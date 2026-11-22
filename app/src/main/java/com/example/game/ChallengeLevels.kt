package com.example.game

data class ChallengeLevel(
    val levelNumber: Int,
    val title: String,
    val subtitle: String,
    val targetScore: Int,
    val moveLimit: Int,
    val starterPattern: List<String>
) {
    fun buildBoard(startId: Long = 1000L): Board = Board.fromPattern(starterPattern, startId)
}

object ChallengeLevels {

    val levels: List<ChallengeLevel> = listOf(
        ChallengeLevel(
            levelNumber = 1,
            title = "First Drop",
            subtitle = "Clear row 7 to drop the suspended shelf",
            targetScore = 100,
            moveLimit = 10,
            starterPattern = listOf(
                "........",
                "........",
                "........",
                "........",
                "........",
                "..1111..",
                "00....00",
                "222..222"
            )
        ),
        ChallengeLevel(
            levelNumber = 2,
            title = "Twin Bridges",
            subtitle = "Trigger a 2-wave gravity chain",
            targetScore = 180,
            moveLimit = 12,
            starterPattern = listOf(
                "........",
                "........",
                "........",
                "........",
                ".33..33.",
                "44....44",
                "111..111",
                "0.0..0.0"
            )
        ),
        ChallengeLevel(
            levelNumber = 3,
            title = "Pillar Alley",
            subtitle = "Clear vertical pillars to collapse the canopy",
            targetScore = 260,
            moveLimit = 14,
            starterPattern = listOf(
                "........",
                "..5555..",
                "..2..2..",
                "1......1",
                "1..66..1",
                "1......1",
                "11.33.11",
                "00....00"
            )
        ),
        ChallengeLevel(
            levelNumber = 4,
            title = "Domino Step",
            subtitle = "Staircase ledges ready for cascading combos",
            targetScore = 340,
            moveLimit = 15,
            starterPattern = listOf(
                "........",
                "........",
                "666.....",
                "...555..",
                ".....444",
                "22....22",
                "111..111",
                "0000..00"
            )
        ),
        ChallengeLevel(
            levelNumber = 5,
            title = "Crossroads",
            subtitle = "Intersect rows and columns for massive bursts",
            targetScore = 440,
            moveLimit = 16,
            starterPattern = listOf(
                "...2....",
                "...2....",
                "444.444.",
                "...2....",
                ".11.111.",
                "...5....",
                "333.333.",
                "66...666"
            )
        ),
        ChallengeLevel(
            levelNumber = 6,
            title = "Sky Vault",
            subtitle = "Heavy upper deck awaits a clean sweep below",
            targetScore = 560,
            moveLimit = 18,
            starterPattern = listOf(
                "00.11.22",
                "33.44.55",
                "........",
                ".66..66.",
                "........",
                "222..222",
                "11....11",
                "000..000"
            )
        ),
        ChallengeLevel(
            levelNumber = 7,
            title = "Reactor Core",
            subtitle = "Thread tight gaps to ignite chain reactions",
            targetScore = 700,
            moveLimit = 20,
            starterPattern = listOf(
                "........",
                ".44..44.",
                "4..55..4",
                ".2....2.",
                "1.6666.1",
                "........",
                "33.00.33",
                "000..000"
            )
        ),
        ChallengeLevel(
            levelNumber = 8,
            title = "Apex Cascade",
            subtitle = "Master-level gravity puzzle with 22 moves",
            targetScore = 900,
            moveLimit = 22,
            starterPattern = listOf(
                "55.66.55",
                "1..22..1",
                ".33..33.",
                "44....44",
                "..0..0..",
                "222..222",
                "11....11",
                "00.66.00"
            )
        )
    )

    fun getLevel(levelNumber: Int): ChallengeLevel =
        levels.firstOrNull { it.levelNumber == levelNumber } ?: levels.first()
}
