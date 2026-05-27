package com.bpo.gasapp.domain.util

/**
 * Best-effort extraction of liters and total amount from the OCR text of a fuel
 * ticket or pump display. Heuristic: looks for numbers next to liter/euro hints.
 */
object TicketParser {

    data class Result(val liters: Double?, val amount: Double?)

    private val number = """\d{1,4}[.,]\d{1,3}"""

    private val litersRegex = Regex(
        """($number)\s*(?:L\b|LTS|LITROS|LITRES)""",
        RegexOption.IGNORE_CASE
    )
    private val litersLabelRegex = Regex(
        """(?:LITROS|LITRES|LTS)\D{0,8}($number)""",
        RegexOption.IGNORE_CASE
    )
    private val amountEuroRegex = Regex(
        """($number)\s*(?:€|EUR\b|EUROS)""",
        RegexOption.IGNORE_CASE
    )
    private val amountLabelRegex = Regex(
        """(?:IMPORTE|TOTAL|A\s*PAGAR)\D{0,10}($number)""",
        RegexOption.IGNORE_CASE
    )

    private fun String.toDouble2(): Double? = replace(',', '.').toDoubleOrNull()

    fun parse(text: String): Result {
        val liters = litersRegex.find(text)?.groupValues?.get(1)?.toDouble2()
            ?: litersLabelRegex.find(text)?.groupValues?.get(1)?.toDouble2()

        // Prefer an explicit IMPORTE/TOTAL, else the largest value next to €.
        val amount = amountLabelRegex.find(text)?.groupValues?.get(1)?.toDouble2()
            ?: amountEuroRegex.findAll(text)
                .mapNotNull { it.groupValues[1].toDouble2() }
                .maxOrNull()

        return Result(liters = liters, amount = amount)
    }
}
