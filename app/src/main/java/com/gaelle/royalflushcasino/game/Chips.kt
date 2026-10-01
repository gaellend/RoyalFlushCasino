package com.gaelle.royalflushcasino.game

// Les valeurs de jetons disponibles
val CHIP_VALUES = listOf(10, 50, 100, 500)

// Décompose une mise en jetons, du plus gros (en bas de la pile) au plus petit (en haut)
fun chipsFor(amount: Int): List<Int> {
    var rest = amount
    val chips = mutableListOf<Int>()
    for (value in CHIP_VALUES.reversed()) {
        while (rest >= value) {
            chips.add(value)
            rest -= value
        }
    }
    return chips.take(10) // au-delà de 10 jetons, la pile resterait trop haute
}