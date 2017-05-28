package com.artem.lendingwidget.data

data class CoinDeskRate(val bpi: Map<Currency, BpiEntry>) {

    data class BpiEntry(val rate_float: Float)

}

enum class Currency {
    // TODO add more currencies
    EUR,
    USD,
    GBP
}