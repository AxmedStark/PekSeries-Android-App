package az.pekstudios.pekseries.feature.profile

import az.pekstudios.pekseries.core.domain.DataError
import az.pekstudios.pekseries.core.domain.PekResult
import az.pekstudios.pekseries.core.domain.usecase.GetWatchStatsUseCase
import az.pekstudios.pekseries.core.testing.repository.FakeShowRepository
import az.pekstudios.pekseries.core.testing.repository.FakeSubscriptionRepository
import az.pekstudios.pekseries.core.testing.repository.FakeUserProfileRepository
import az.pekstudios.pekseries.core.testing.repository.TestData
import az.pekstudios.pekseries.core.testing.util.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val profileRepository = FakeUserProfileRepository()
    private val subscriptions = FakeSubscriptionRepository()
    private val shows = FakeShowRepository()

    private fun viewModel() =
        ProfileViewModel(profileRepository, GetWatchStatsUseCase(subscriptions, shows))

    @Test
    fun `loads watch stats on construction`() = runTest {
        subscriptions.subscribedShowsResult = PekResult.Success(listOf(TestData.show(id = "tvmaze_1")))
        shows.episodesById["1"] = listOf(TestData.episode("1", runtime = 60))

        val state = viewModel().uiState.value

        assertThat(state.stats.seriesCount).isEqualTo(1)
        assertThat(state.stats.totalHours).isEqualTo(1)
        assertThat(state.isLoadingStats).isFalse()
    }

    /** Stats are secondary here, so a failure must not blank the profile. */
    @Test
    fun `a stats failure degrades to zeroes without an error state`() = runTest {
        subscriptions.subscribedShowsResult = PekResult.Failure(DataError.Network)

        val state = viewModel().uiState.value

        assertThat(state.stats.seriesCount).isEqualTo(0)
        assertThat(state.isLoadingStats).isFalse()
    }

    @Test
    fun `saving a name trims it and closes the dialog`() = runTest {
        val vm = viewModel()
        vm.startEditingProfile()

        vm.saveDisplayName("  Ahmed  ")

        assertThat(profileRepository.displayNameUpdates).containsExactly("Ahmed")
        assertThat(vm.uiState.value.isEditingProfile).isFalse()
        assertThat(vm.uiState.value.isSavingProfile).isFalse()
    }

    @Test
    fun `a blank name clears the override so the provider value returns`() = runTest {
        viewModel().saveDisplayName("   ")

        assertThat(profileRepository.displayNameUpdates).containsExactly(null)
    }

    @Test
    fun `an over-long name is truncated rather than rejected`() = runTest {
        viewModel().saveDisplayName("x".repeat(MAX_DISPLAY_NAME_LENGTH + 20))

        assertThat(profileRepository.displayNameUpdates.single()).hasLength(MAX_DISPLAY_NAME_LENGTH)
    }

    /**
     * The toggle used to write a SharedPreferences boolean nothing read. It must
     * reach the topic layer, because delivery is driven by FCM topics.
     */
    @Test
    fun `the push toggle reaches the topic layer`() = runTest {
        val vm = viewModel()

        vm.setPushEnabled(false)

        assertThat(profileRepository.pushToggles).containsExactly(false)
        assertThat(vm.uiState.value.isTogglingPush).isFalse()
    }

    @Test
    fun `a failed toggle reports an error`() = runTest {
        profileRepository.pushResult = PekResult.Failure(DataError.Network)
        val vm = viewModel()

        vm.setPushEnabled(false)

        assertThat(vm.uiState.value.error).isEqualTo(DataError.Network)
    }

    @Test
    fun `dismissing clears the error`() = runTest {
        profileRepository.pushResult = PekResult.Failure(DataError.Network)
        val vm = viewModel()
        vm.setPushEnabled(false)

        vm.dismissError()

        assertThat(vm.uiState.value.error).isNull()
    }

    @Test
    fun `clearing the photo passes null through`() = runTest {
        viewModel().savePhotoUri(null)

        assertThat(profileRepository.photoUpdates).containsExactly(null)
    }
}
