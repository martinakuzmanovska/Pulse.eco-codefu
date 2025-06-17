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
import com.codefu.pulse_eco.presentation.sign_in.UserData
import kotlinx.coroutines.launch

class ShopItemViewModel(private val repository: ShopItemRepository): ViewModel() {
    private val _shopItems = MutableLiveData<List<ShopItem>>()
    val shopItems: LiveData<List<ShopItem>>
        get() = _shopItems

    private val _user =  MutableLiveData<UserData>()

    val user : MutableLiveData<UserData> = _user

    fun setUserValue(user: UserData) {
        this._user.value = user
    }

    fun getUserValue(): UserData? {
        return _user.value
    }

    fun getShopItems(){
        viewModelScope.launch {
            try {
                _shopItems.value = repository.fetchShopItems()
            } catch (e: Exception) {
                _shopItems.value = emptyList()
                Log.e("ShopItemsViewModel", "Error fetching shopItems", e)
            }
        }

    }

    override fun onCleared() {
        super.onCleared()
        (repository as? ShopItemRepositoryImpl)?.detachActivityListener()
    }
    fun redeemItem(item: ShopItem) {
        val currentUser = _user.value
        if (currentUser != null) {
            _user.value = currentUser.copy(points = currentUser.points?.minus(item.pointsRequired))
        }
    }

}