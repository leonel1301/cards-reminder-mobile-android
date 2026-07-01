package com.lenaralabs.cardsreminder.core.data

import com.lenaralabs.cardsreminder.core.model.ApiOwner
import com.lenaralabs.cardsreminder.core.model.CreateOwnerRequest
import com.lenaralabs.cardsreminder.core.model.UpdateOwnerRequest
import com.lenaralabs.cardsreminder.core.network.ApiException
import com.lenaralabs.cardsreminder.core.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class OwnersRepository(
    private val apiService: ApiService,
) {
    private val _owners = MutableStateFlow<List<ApiOwner>>(emptyList())
    val owners: StateFlow<List<ApiOwner>> = _owners.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val selfOwner: ApiOwner?
        get() = _owners.value.firstOrNull { it.isSelf }

    suspend fun fetchOwners() {
        _isLoading.value = true
        _errorMessage.value = null
        try {
            val list: List<ApiOwner> = apiService.getDecoded("/owners")
            _owners.value = sortOwners(list)
        } catch (error: ApiException.NotAuthenticated) {
            resetSession()
        } catch (error: Throwable) {
            _errorMessage.value = error.localizedMessage
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun createOwner(request: CreateOwnerRequest): Result<ApiOwner> {
        _isLoading.value = true
        _errorMessage.value = null
        return try {
            val owner: ApiOwner = apiService.postDecoded("/owners", request)
            _owners.value = sortOwners(_owners.value + owner)
            Result.success(owner)
        } catch (error: Throwable) {
            _errorMessage.value = error.localizedMessage
            Result.failure(error)
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun updateOwner(id: String, request: UpdateOwnerRequest): Result<ApiOwner> {
        _isLoading.value = true
        _errorMessage.value = null
        return try {
            val owner: ApiOwner = apiService.patchDecoded("/owners/$id", request)
            _owners.value = sortOwners(
                _owners.value.map { if (it.id == id) owner else it },
            )
            Result.success(owner)
        } catch (error: Throwable) {
            _errorMessage.value = error.localizedMessage
            Result.failure(error)
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun deleteOwner(id: String): Result<Unit> {
        _isLoading.value = true
        _errorMessage.value = null
        return try {
            apiService.delete("/owners/$id")
            _owners.value = _owners.value.filterNot { it.id == id }
            Result.success(Unit)
        } catch (error: Throwable) {
            _errorMessage.value = error.localizedMessage
            Result.failure(error)
        } finally {
            _isLoading.value = false
        }
    }

    fun ownerDisplayName(owner: ApiOwner, selfFormat: String): String {
        return if (owner.isSelf) String.format(selfFormat, owner.name) else owner.name
    }

    fun salaryDayLabel(owner: ApiOwner, notSetLabel: String, dayFormat: String): String {
        val day = owner.salaryDay ?: return notSetLabel
        return String.format(dayFormat, day)
    }

    fun resetSession() {
        _owners.value = emptyList()
        _errorMessage.value = null
        _isLoading.value = false
    }

    private fun sortOwners(owners: List<ApiOwner>): List<ApiOwner> {
        return owners.sortedWith(
            compareByDescending<ApiOwner> { it.isSelf }
                .thenBy { it.name.lowercase() },
        )
    }
}
