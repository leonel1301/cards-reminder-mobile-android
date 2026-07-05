package com.lenaralabs.cardsreminder.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lenaralabs.cardsreminder.core.data.FeedbackRepository
import com.lenaralabs.cardsreminder.core.data.OwnersRepository
import com.lenaralabs.cardsreminder.core.data.SessionRepository
import com.lenaralabs.cardsreminder.core.data.UserRepository
import com.lenaralabs.cardsreminder.core.model.ApiFeedback
import com.lenaralabs.cardsreminder.core.model.ApiOwner
import com.lenaralabs.cardsreminder.core.model.ApiUser
import com.lenaralabs.cardsreminder.core.model.CreateFeedbackRequest
import com.lenaralabs.cardsreminder.core.model.CreateOwnerRequest
import com.lenaralabs.cardsreminder.core.model.UpdateFeedbackRequest
import com.lenaralabs.cardsreminder.core.model.UpdateOwnerRequest
import com.lenaralabs.cardsreminder.core.util.DeviceInfo
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import kotlin.time.Duration.Companion.milliseconds

sealed class ProfileSheet {
    data object None : ProfileSheet()
    data object CreateOwner : ProfileSheet()
    data class EditOwner(val owner: ApiOwner) : ProfileSheet()
    data object FeedbackList : ProfileSheet()
    data object CreateFeedback : ProfileSheet()
    data class EditFeedback(val feedback: ApiFeedback) : ProfileSheet()
}

data class OwnerFormState(
    val name: String = "",
    val salaryDaySelection: Int = 0,
    val isDirty: Boolean = false,
) {
    val canSave: Boolean get() = name.trim().isNotEmpty()
    val salaryDay: Int? get() = if (salaryDaySelection == 0) null else salaryDaySelection
}

data class FeedbackFormState(
    val title: String = "",
    val content: String = "",
    val isDirty: Boolean = false,
) {
    val canSave: Boolean get() = title.trim().isNotEmpty() && content.trim().isNotEmpty()
}

data class ProfileUiState(
    val user: ApiUser? = null,
    val owners: List<ApiOwner> = emptyList(),
    val isLoadingProfile: Boolean = false,
    val isLoadingOwners: Boolean = false,
    val errorMessage: String? = null,
    val activeSheet: ProfileSheet = ProfileSheet.None,
    val showSettings: Boolean = false,
    val showMenu: Boolean = false,
    val pendingDeleteOwner: ApiOwner? = null,
    val pendingDeleteFeedback: ApiFeedback? = null,
    val ownerFormState: OwnerFormState = OwnerFormState(),
    val feedbackFormState: FeedbackFormState = FeedbackFormState(),
    val showDiscardOwnerDialog: Boolean = false,
    val showDiscardFeedbackDialog: Boolean = false,
    val initialLoadComplete: Boolean = false,
    val isPullRefreshing: Boolean = false,
    val isSigningOut: Boolean = false,
) {

    class ProfileViewModel(
        private val userRepository: UserRepository,
        private val ownersRepository: OwnersRepository,
        private val feedbackRepository: FeedbackRepository,
        private val sessionRepository: SessionRepository,
    ) : ViewModel() {

        private val activeSheet = MutableStateFlow<ProfileSheet>(ProfileSheet.None)
        private val showSettings = MutableStateFlow(false)
        private val showMenu = MutableStateFlow(false)
        private val pendingDeleteOwner = MutableStateFlow<ApiOwner?>(null)
        private val pendingDeleteFeedback = MutableStateFlow<ApiFeedback?>(null)
        private val ownerFormState = MutableStateFlow(OwnerFormState())
        private val feedbackFormState = MutableStateFlow(FeedbackFormState())
        private val showDiscardOwnerDialog = MutableStateFlow(false)
        private val showDiscardFeedbackDialog = MutableStateFlow(false)
        private val initialLoadComplete = MutableStateFlow(false)
        private val isPullRefreshing = MutableStateFlow(false)
        private val isSigningOut = MutableStateFlow(false)

        val feedbacks = feedbackRepository.feedbacks

        private val _uiState = MutableStateFlow(ProfileUiState())
        val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                loadInitialData(silentOwners = true)
                initialLoadComplete.value = true
            }
            viewModelScope.launch {
                combine(
                    combine(
                        userRepository.user,
                        userRepository.isLoading,
                        userRepository.errorMessage,
                        ownersRepository.owners,
                        ownersRepository.isLoading,
                    ) { user, loadingProfile, userError, owners, loadingOwners ->
                        ProfileDataSnapshot(user, loadingProfile, userError, owners, loadingOwners)
                    },
                    combine(
                        ownersRepository.errorMessage,
                        activeSheet,
                        showSettings,
                        showMenu,
                        pendingDeleteOwner,
                    ) { ownersError, sheet, settings, menu, deleteOwner ->
                        ProfileUiSnapshot1(ownersError, sheet, settings, menu, deleteOwner)
                    },
                    combine(
                        pendingDeleteFeedback,
                        ownerFormState,
                        feedbackFormState,
                        showDiscardOwnerDialog,
                        showDiscardFeedbackDialog,
                    ) { deleteFeedback, ownerForm, feedbackForm, discardOwner, discardFeedback ->
                        ProfileUiSnapshot2(
                            deleteFeedback,
                            ownerForm,
                            feedbackForm,
                            discardOwner,
                            discardFeedback
                        )
                    },
                    combine(
                        initialLoadComplete,
                        isPullRefreshing,
                        isSigningOut,
                    ) { loadComplete, pullRefreshing, signingOut ->
                        Triple(loadComplete, pullRefreshing, signingOut)
                    },
                ) { data, ui1, ui2, loadState ->
                    ProfileUiState(
                        user = data.user,
                        owners = data.owners,
                        isLoadingProfile = data.loadingProfile,
                        isLoadingOwners = data.loadingOwners,
                        errorMessage = data.userError ?: ui1.ownersError,
                        activeSheet = ui1.sheet,
                        showSettings = ui1.settings,
                        showMenu = ui1.menu,
                        pendingDeleteOwner = ui1.deleteOwner,
                        pendingDeleteFeedback = ui2.deleteFeedback,
                        ownerFormState = ui2.ownerForm,
                        feedbackFormState = ui2.feedbackForm,
                        showDiscardOwnerDialog = ui2.discardOwner,
                        showDiscardFeedbackDialog = ui2.discardFeedback,
                        initialLoadComplete = loadState.first,
                        isPullRefreshing = loadState.second,
                        isSigningOut = loadState.third,
                    )
                }.collect { _uiState.value = it }
            }
        }

        private suspend fun loadInitialData(silentOwners: Boolean) {
            coroutineScope {
                val profileJob = async {
                    if (userRepository.user.value == null) {
                        userRepository.fetchProfile()
                    }
                }
                val ownersJob = async {
                    if (!silentOwners || ownersRepository.owners.value.isEmpty()) {
                        ownersRepository.fetchOwners()
                    }
                }
                profileJob.await()
                ownersJob.await()
            }
        }

        private data class ProfileDataSnapshot(
            val user: ApiUser?,
            val loadingProfile: Boolean,
            val userError: String?,
            val owners: List<ApiOwner>,
            val loadingOwners: Boolean,
        )

        private data class ProfileUiSnapshot1(
            val ownersError: String?,
            val sheet: ProfileSheet,
            val settings: Boolean,
            val menu: Boolean,
            val deleteOwner: ApiOwner?,
        )

        private data class ProfileUiSnapshot2(
            val deleteFeedback: ApiFeedback?,
            val ownerForm: OwnerFormState,
            val feedbackForm: FeedbackFormState,
            val discardOwner: Boolean,
            val discardFeedback: Boolean,
        )

        fun refresh(silentOwners: Boolean = false) {
            viewModelScope.launch {
                isPullRefreshing.value = true
                try {
                    coroutineScope {
                        val profileJob = async { userRepository.fetchProfile() }
                        val ownersJob = async {
                            if (!silentOwners || ownersRepository.owners.value.isEmpty()) {
                                ownersRepository.fetchOwners()
                            }
                        }
                        profileJob.await()
                        ownersJob.await()
                    }
                } finally {
                    isPullRefreshing.value = false
                }
            }
        }

        fun setMenuExpanded(expanded: Boolean) {
            showMenu.value = expanded
        }

        fun openSettings() {
            showMenu.value = false
            showSettings.value = true
        }

        fun closeSettings() {
            showSettings.value = false
        }

        fun openCreateOwner() {
            ownerFormState.value = OwnerFormState()
            activeSheet.value = ProfileSheet.CreateOwner
        }

        fun openEditOwner(owner: ApiOwner) {
            ownerFormState.value = OwnerFormState(
                name = owner.name,
                salaryDaySelection = owner.salaryDay ?: 0,
            )
            activeSheet.value = ProfileSheet.EditOwner(owner)
        }

        fun openFeedbackList() {
            activeSheet.value = ProfileSheet.FeedbackList
            viewModelScope.launch { feedbackRepository.fetchFeedbacks() }
        }

        fun openCreateFeedback() {
            feedbackFormState.value = FeedbackFormState()
            activeSheet.value = ProfileSheet.CreateFeedback
        }

        fun openEditFeedback(feedback: ApiFeedback) {
            feedbackFormState.value = FeedbackFormState(
                title = feedback.title,
                content = feedback.content,
            )
            activeSheet.value = ProfileSheet.EditFeedback(feedback)
        }

        fun requestDismissOwnerSheet() {
            if (ownerFormState.value.isDirty) {
                showDiscardOwnerDialog.value = true
            } else {
                closeSheet()
            }
        }

        fun requestDismissFeedbackForm() {
            if (feedbackFormState.value.isDirty) {
                showDiscardFeedbackDialog.value = true
            } else {
                closeFeedbackForm()
            }
        }

        fun confirmDiscardOwner() {
            showDiscardOwnerDialog.value = false
            closeSheet()
        }

        fun confirmDiscardFeedback() {
            showDiscardFeedbackDialog.value = false
            closeFeedbackForm()
        }

        fun dismissDiscardDialogs() {
            showDiscardOwnerDialog.value = false
            showDiscardFeedbackDialog.value = false
        }

        fun closeSheet() {
            activeSheet.value = ProfileSheet.None
        }

        fun closeFeedbackForm() {
            when (val sheet = activeSheet.value) {
                is ProfileSheet.CreateFeedback, is ProfileSheet.EditFeedback -> {
                    activeSheet.value = ProfileSheet.FeedbackList
                }

                else -> activeSheet.value = ProfileSheet.None
            }
        }

        fun closeFeedbackList() {
            activeSheet.value = ProfileSheet.None
        }

        fun updateOwnerForm(transform: (OwnerFormState) -> OwnerFormState) {
            ownerFormState.value = transform(ownerFormState.value.copy(isDirty = true))
        }

        fun updateFeedbackForm(transform: (FeedbackFormState) -> FeedbackFormState) {
            feedbackFormState.value = transform(feedbackFormState.value.copy(isDirty = true))
        }

        fun saveOwner(isSelfOwner: Boolean, editingOwner: ApiOwner?) {
            val form = ownerFormState.value
            viewModelScope.launch {
                when (activeSheet.value) {
                    is ProfileSheet.CreateOwner -> {
                        ownersRepository.createOwner(
                            CreateOwnerRequest(
                                name = form.name.trim(),
                                salaryDay = form.salaryDay,
                            ),
                        ).onSuccess { closeSheet() }
                    }

                    is ProfileSheet.EditOwner -> {
                        val owner = editingOwner ?: return@launch
                        ownersRepository.updateOwner(
                            owner.id,
                            UpdateOwnerRequest(
                                name = if (isSelfOwner) null else form.name.trim(),
                                salaryDay = form.salaryDay,
                            ),
                        ).onSuccess { closeSheet() }
                    }

                    else -> Unit
                }
            }
        }

        fun requestDeleteOwner(owner: ApiOwner) {
            pendingDeleteOwner.value = owner
        }

        fun dismissDeleteOwner() {
            pendingDeleteOwner.value = null
        }

        fun confirmDeleteOwner() {
            val owner = pendingDeleteOwner.value ?: return
            pendingDeleteOwner.value = null
            viewModelScope.launch {
                ownersRepository.deleteOwner(owner.id).onSuccess { closeSheet() }
            }
        }

        fun saveFeedback(editingFeedback: ApiFeedback?) {
            val form = feedbackFormState.value
            viewModelScope.launch {
                when (activeSheet.value) {
                    is ProfileSheet.CreateFeedback -> {
                        feedbackRepository.createFeedback(
                            CreateFeedbackRequest(
                                title = form.title.trim(),
                                device = DeviceInfo.feedbackDeviceString(),
                                content = form.content.trim(),
                            ),
                        ).onSuccess { closeFeedbackForm() }
                    }

                    is ProfileSheet.EditFeedback -> {
                        val feedback = editingFeedback ?: return@launch
                        feedbackRepository.updateFeedback(
                            feedback.id,
                            UpdateFeedbackRequest(
                                title = form.title.trim(),
                                content = form.content.trim(),
                            ),
                        ).onSuccess { closeFeedbackForm() }
                    }

                    else -> Unit
                }
            }
        }

        fun requestDeleteFeedback(feedback: ApiFeedback) {
            pendingDeleteFeedback.value = feedback
        }

        fun dismissDeleteFeedback() {
            pendingDeleteFeedback.value = null
        }

        fun confirmDeleteFeedback() {
            val feedback = pendingDeleteFeedback.value ?: return
            pendingDeleteFeedback.value = null
            viewModelScope.launch {
                feedbackRepository.deleteFeedback(feedback.id).onSuccess { closeFeedbackForm() }
            }
        }

        fun signOut() {
            if (isSigningOut.value) return
            showMenu.value = false
            viewModelScope.launch {
                isSigningOut.value = true
                yield()
                delay(SignOutIndicatorDuration)
                sessionRepository.signOut()
                isSigningOut.value = false
            }
        }

        private companion object {
            val SignOutIndicatorDuration = 350.milliseconds
        }

        fun deleteAccount(onSuccess: () -> Unit) {
            viewModelScope.launch {
                userRepository.deleteAccount()
                    .onSuccess {
                        sessionRepository.signOut()
                        onSuccess()
                    }
            }
        }

        class Factory(
            private val userRepository: UserRepository,
            private val ownersRepository: OwnersRepository,
            private val feedbackRepository: FeedbackRepository,
            private val sessionRepository: SessionRepository,
        ) : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ProfileViewModel(
                    userRepository,
                    ownersRepository,
                    feedbackRepository,
                    sessionRepository,
                ) as T
            }
        }
    }
}
