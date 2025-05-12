package com.codefu.pulse_eco.domain.models


class EventCardModel (private var cardTitleText: String,
                      private var cardDate: String,
                      private var imgId: String,
                      private var description: String,
                      private var points: Int
    ) {

    fun getCardTitleText(): String {
        return cardTitleText
    }

    fun getImgid(): String {
        return imgId
    }

    fun getCardDate(): String {
        return cardDate
    }

    fun getDescription(): String {
        return description
    }

}