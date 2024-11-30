package com.codefu.pulse_eco.domain.models

class EventCardModel (private var cardTitleText: String,
                      private var cardDate: String,
                      private var imgId: String,
                      private var points: Int
    ) {

    fun getCardTitleText(): String {
        return cardTitleText
    }

    fun setCardTitleText(cardTitleText: String) {
        this.cardTitleText = cardTitleText
    }

    fun getImgid(): String {
        return imgId
    }

    fun setImgid(imgid: String) {
        this.imgId = imgid
    }

    fun getCardDate(): String {
        return cardDate
    }

    fun setCardDate(cardDate: String) {
        this.cardDate = cardDate
    }



}