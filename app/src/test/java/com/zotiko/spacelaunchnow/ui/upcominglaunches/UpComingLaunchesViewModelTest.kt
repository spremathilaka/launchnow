package com.zotiko.spacelaunchnow.ui.upcominglaunches

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.zotiko.spacelaunchnow.domain.upcominglaunches.GetUpComingLaunchesUC
import com.zotiko.spacelaunchnow.model.UpComingLaunches
import com.zotiko.spacelaunchnow.utils.TestUtils
import com.zotiko.spacelaunchnow.utils.any
import com.zotiko.spacelaunchnow.utils.lambdaMock
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import java.io.IOException

class UpComingLaunchesViewModelTest {

    @Rule
    @JvmField
    var rule: TestRule = InstantTaskExecutorRule()

    @Mock
    lateinit var fetchGetUpComingLaunchesUC: GetUpComingLaunchesUC

    private lateinit var viewModel: UpComingLaunchesViewModel

    private val viewStateObserver: Observer<UpComingLaunchContract.ViewState> = lambdaMock()

    @Captor
    private lateinit var viewStateCaptor: ArgumentCaptor<UpComingLaunchContract.ViewState>

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    private fun initViewModel() {
        viewModel = UpComingLaunchesViewModel(
            fetchGetUpComingLaunchesUC,
            Schedulers.trampoline()
        )
        viewModel.viewState.observeForever(viewStateObserver)
        viewModel.fetchLaunchEvents()
    }

    @Test
    fun `when api success should show result`() {
        // Given
        Mockito.`when`(fetchGetUpComingLaunchesUC.run(any()))
            .thenReturn(getDummyUpComingLaunches())
        // When
        initViewModel()
        // Then
        with(viewStateCaptor) {
            verify(viewStateObserver, times(2)).onChanged(capture())
            assertTrue(allValues[0].isLoading)
            assertFalse(allValues[1].isLoading)
            assertNotNull(allValues[1].activityData)
            assertNull(allValues[1].errorState)
        }
    }

    @Test
    fun `network error state is emitted when there is no network`() {
        // Given
        Mockito.`when`(fetchGetUpComingLaunchesUC.run(any()))
            .thenReturn(Single.error(IOException()))

        // When
        initViewModel()
        // Then
        with(viewStateCaptor) {
            verify(viewStateObserver, times(2)).onChanged(capture())
            assertTrue(allValues[0].isLoading)
            assertFalse(allValues[1].isLoading)
            assertNull(allValues[1].activityData)
            assertNotNull(allValues[1].errorState)
        }
    }

    @After
    fun tearDown() {
        viewModel.viewState.removeObserver(viewStateObserver)
    }

    private fun getDummyUpComingLaunches(): Single<GetUpComingLaunchesUC.ResponseValue> {
        val data = TestUtils.loadData(
            "json/getLaunchList_whenSuccess.json",
            UpComingLaunches::class.java
        )
        val results = data!!.launchEvents.map { launchEvent ->
            launchEvent.toDTO()
        }.toList()

        return Single.just(GetUpComingLaunchesUC.ResponseValue(results))
    }
}