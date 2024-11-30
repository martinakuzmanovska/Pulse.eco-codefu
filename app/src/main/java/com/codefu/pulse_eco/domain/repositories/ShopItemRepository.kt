package com.codefu.pulse_eco.domain.repositories

import com.codefu.pulse_eco.domain.models.Activity
import com.codefu.pulse_eco.domain.models.ShopItem

interface ShopItemRepository {
    //val shopItem:String?="",
    //    val pointsRequired:Int=0,
    //    val description:String?="",
    //    val url:String?=""

    suspend fun fetchShopItems(): List<ShopItem>
    suspend fun createShopItem(name:String,pointsRequired:Int,description:String,url:String)
}