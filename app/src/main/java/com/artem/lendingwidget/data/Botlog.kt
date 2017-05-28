package com.artem.lendingwidget.data

import java.util.*


data class Botlog(val last_status: String,
                  val last_update: Date,
                  val log : Array<String>,
                  val outputCurrency: CurrencyEntry,
                  val raw_data: Map<CryptoCurrency, Data>) {

    data class CurrencyEntry(val currency: CryptoCurrency,
                             val highestBid: Float)

    data class Data(val averageLendingRate: Float,
                    val lentSum: Float,
                    val maxToLend: Float)

}

enum class CryptoCurrency {
    // TODO add more currencies
    BTC,
    ETH
}