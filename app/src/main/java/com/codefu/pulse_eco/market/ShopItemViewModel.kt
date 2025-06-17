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
import kotlinx.coroutines.launch

class ShopItemViewModel(private val repository: ShopItemRepository): ViewModel() {
    private val _shopItems = MutableLiveData<List<ShopItem>>()
    val shopItems: LiveData<List<ShopItem>>
        get() = _shopItems

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

}