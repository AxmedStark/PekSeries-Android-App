package az.pekstudios.pekseries.core.testing.repository

import az.pekstudios.pekseries.core.domain.PekResult
import az.pekstudios.pekseries.core.domain.repository.UserProfileRepository
import az.pekstudios.pekseries.core.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeUserProfileRepository : UserProfileRepository {

    val profileFlow = MutableStateFlow(UserProfile(displayName = "Pek", isSignedIn = true))
    override val profile: Flow<UserProfile> = profileFlow

    var updateNameResult: PekResult<Unit> = PekResult.Success(Unit)
    var updatePhotoResult: PekResult<Unit> = PekResult.Success(Unit)
    var pushResult: PekResult<Unit> = PekResult.Success(Unit)

    val displayNameUpdates = mutableListOf<String?>()
    val photoUpdates = mutableListOf<String?>()
    val pushToggles = mutableListOf<Boolean>()
    var clearCallCount = 0

    override suspend fun updateDisplayName(name: String?): PekResult<Unit> {
        displayNameUpdates += name
        if (updateNameResult is PekResult.Success) {
            profileFlow.value = profileFlow.value.copy(
                displayName = name ?: "Pek",
                hasCustomDisplayName = name != null,
            )
        }
        return updateNameResult
    }

    override suspend fun updatePhotoUri(uri: String?): PekResult<Unit> {
        photoUpdates += uri
        return updatePhotoResult
    }

    override suspend fun setPushEnabled(enabled: Boolean): PekResult<Unit> {
        pushToggles += enabled
        if (pushResult is PekResult.Success) {
            profileFlow.value = profileFlow.value.copy(pushEnabled = enabled)
        }
        return pushResult
    }

    override suspend fun clearLocalProfile() {
        clearCallCount++
    }
}
