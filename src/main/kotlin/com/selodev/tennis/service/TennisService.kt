package com.selodev.tennis.service

import org.springframework.stereotype.Service
import java.io.File

@Service
class TennisService {
    fun generateRotations(players: List<String>, config: Config): MutableList<Rotation> {
        val rotations = mutableListOf<Rotation>()
        for (i in 1..config.numRotations) {
            val rotationPlayers = players.toMutableList()
            val playersSittingOut = getAndRemovePlayersSittingOut(rotationPlayers, rotations, config)
            val matches = getMatches(rotationPlayers, rotations, config)
            rotations.add(Rotation(matches, playersSittingOut))
        }
        return rotations
    }

    private fun getAndRemovePlayersSittingOut(
        players: MutableList<String>,
        pastRotations: MutableList<Rotation>,
        config: Config
    ): List<String> {
        players.shuffle()
        players.sortBy { player -> pastRotations.flatMap { rotation -> rotation.sittingOut }.count { it === player } }
        val numPlayersSittingOut = players.size - (config.matchType.numPlayersPerMatch * config.numCourts)
        return players
            .filter { player -> lastRotationDoesNotContainPlayer(pastRotations, player) }
            .take(numPlayersSittingOut)
            .also { players.removeAll(it) }
    }

    private fun lastRotationDoesNotContainPlayer(rotations: List<Rotation>, player: String) =
        rotations.isEmpty() || !rotations.last().sittingOut.contains(player)

    private fun getMatches(allPlayersInRotation: MutableList<String>, pastRotations: MutableList<Rotation>, config: Config): List<Match> {
        val matches = mutableListOf<Match>()
        val availablePlayers = allPlayersInRotation.toMutableList()
        for (i in 1 .. config.numCourts) {
            val match = generateMatch(availablePlayers, matches, pastRotations)
                ?: return getMatches(allPlayersInRotation, pastRotations, config)
            matches.add(match)
        }
        return matches
    }

    private fun generateMatch(players: MutableList<String>,
                              matchesInCurrentRotation: MutableList<Match>,
                              pastRotations: MutableList<Rotation>
    ): Match? {
        players.shuffle()
        val pair1: Pair = generatePair(players, matchesInCurrentRotation, pastRotations) ?: return null
        val pair2: Pair = generatePair(players, matchesInCurrentRotation, pastRotations) ?: return null
        return Match(pair1, pair2)
    }

    private fun generatePair(players: MutableList<String>,
                             matchesInCurrentRotation: MutableList<Match>,
                             pastRotations: MutableList<Rotation>
    ): Pair? {
        do {
            val firstPlayer = players.removeAt(0)
            players.forEach { secondPlayer ->
                val pair = Pair(firstPlayer, secondPlayer)
                if (pairIsPossible(pair, matchesInCurrentRotation, pastRotations)) {
                    players.remove(secondPlayer)
                    return pair
                }
            }
        } while (players.isNotEmpty())
        return null
    }

    private fun pairIsPossible(pair: Pair, matchesInCurrentRotation: MutableList<Match>, pastRotations: MutableList<Rotation>): Boolean {
        if (matchesInCurrentRotation.getAllPairs().any { it == pair }) {
            return false
        }
        if (pastRotations.flatMap { it.matches }.getAllPairs().any { it == pair }) {
            return false
        }
        return true
    }
}

fun List<Match>.getAllPairs(): List<Pair> = flatMap { match -> listOf(match.pair1, match.pair2) }

data class Config (
    val numCourts: Int = 2,
    val numRotations: Int = 7,
    val matchType: MatchType = MatchType.DOUBLES
)

enum class MatchType(val numPlayersPerMatch: Int) {
    SINGLES(2), DOUBLES(4)
}

data class Match (
    val pair1: Pair,
    val pair2: Pair
)

data class Rotation (
    val matches: List<Match>,
    val sittingOut: List<String>
)

data class Pair (
    val player1: String,
    val player2: String
) {
    override fun equals(other: Any?): Boolean {
        if (other !is Pair) return false
        if (player1 == other.player1) return player2 == other.player2
        if (player1 == other.player2) return player2 == other.player1
        return false
    }

    override fun hashCode(): Int {
        var result = player1.hashCode()
        result = 31 * result + player2.hashCode()
        return result
    }
}
