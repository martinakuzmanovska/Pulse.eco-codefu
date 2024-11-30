package com.codefu.pulse_eco.domain.repositories.impl

import android.content.Context
import android.util.Log
import com.codefu.pulse_eco.domain.models.Activity
import com.codefu.pulse_eco.domain.models.ShopItem
import com.codefu.pulse_eco.domain.repositories.ShopItemRepository
import com.codefu.pulse_eco.utils.Constants.ACTIVITY_REF
import com.codefu.pulse_eco.utils.Constants.SHOP_REF
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ShopItemRepositoryImpl(context: Context,private val rootRefDB: DatabaseReference = Firebase.database.reference,
                             private val shopRef: DatabaseReference = rootRefDB.child(
                                 SHOP_REF
                             )):ShopItemRepository {

    private var shopItemListener: ValueEventListener? = null

    override suspend fun fetchShopItems(): List<ShopItem> {
        return suspendCoroutine { continuation ->
            shopItemListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    try {
                        val shopItems = dataSnapshot.children.mapNotNull { it.getValue(ShopItem::class.java) }
                        Log.d("FirebaseData", "Raw Data: ${dataSnapshot.value}")
                        continuation.resume(shopItems)
                    } catch (e: Exception) {
                        Log.e("firebase", "Error parsing data", e)
                        continuation.resume(emptyList())
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("firebase", "Database error", databaseError.toException())
                    continuation.resume(emptyList())
                }
            }

            shopRef.addListenerForSingleValueEvent(shopItemListener!!)
        }
    }

    override suspend fun createShopItem(
        name: String,
        pointsRequired: Int,
        description: String,
        url: String
    ) {
        val shopItem:ShopItem=ShopItem(name,pointsRequired,description,url)
        shopRef.push().setValue(shopItem)
    }

    fun detachShopItemListener() {
        shopItemListener.let {
            if (it != null) {
                shopRef.removeEventListener(it)
                shopItemListener = null
            }

        }
    }
}