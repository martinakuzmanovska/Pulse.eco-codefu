package com.codefu.pulse_eco.market

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codefu.pulse_eco.domain.models.Activity
import com.codefu.pulse_eco.domain.models.ShopItem
import com.codefu.pulse_eco.domain.repositories.ShopItemRepository
import com.codefu.pulse_eco.domain.repositories.impl.ActivityRepositoryImpl
import com.codefu.pulse_eco.domain.repositories.impl.ShopItemRepositoryImpl
import com.codefu.pulse_eco.presentation.sign_in.GoogleAuthUiClient
import com.codefu.pulse_eco.presentation.sign_in.UserData
import com.google.firebase.database.database
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class ShopItemViewModel(
    private val repository: ShopItemRepository,
    private val authClient: GoogleAuthUiClient
) : ViewModel() {

    private val _shopItems = MutableLiveData<List<ShopItem>>()
    val shopItems: LiveData<List<ShopItem>> = _shopItems

    private val _user = MutableLiveData<UserData>()
    val user: LiveData<UserData> = _user

    fun setUserValue(user: UserData) {
        _user.value = user
    }

    fun getUserValue(): UserData? = _user.value


    fun listenToUserData(userId: String) {
        val database = Firebase.database.reference
        val userRef = database.child("users").child(userId)

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userData = snapshot.getValue(UserData::class.java)
                userData?.let {
                    _user.postValue(it)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HomeViewModel", "Database listen cancelled", error.toException())
            }
        })
    }


    fun getShopItems() {
        viewModelScope.launch {
            try {
                _shopItems.value = repository.fetchShopItems()
            } catch (e: Exception) {
                _shopItems.value = emptyList()
                Log.e("ShopItemViewModel", "Error fetching shopItems", e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        (repository as? ShopItemRepositoryImpl)?.detachActivityListener()
    }

    fun redeemItem(item: ShopItem) {
        val currentUser = _user.value ?: return
        val newPoints = (currentUser.points ?: 0) - item.pointsRequired

        // Update locally
        _user.value = currentUser.copy(points = newPoints)

        // Update in Firebase using the shared auth client function
        viewModelScope.launch {
            authClient.updateUserPointsAndItems(
                userId = currentUser.userId.toString(),
                newPoints = newPoints,
                newItemId = item.name.toString()
            )
        }
    }
}
