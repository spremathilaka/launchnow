package com.zotiko.spacelaunchnow.ui.upcominglaunches

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zotiko.spacelaunchnow.di.modules.OBSERVER_ON
import com.zotiko.spacelaunchnow.domain.upcominglaunches.GetUpComingLaunchesUC
import com.zotiko.spacelaunchnow.dto.LaunchEventDTO
import com.zotiko.spacelaunchnow.ui.base.BaseViewModel
import com.zotiko.spacelaunchnow.ui.data.PageErrorState
import io.reactivex.Scheduler
import io.reactivex.observers.DisposableSingleObserver
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import javax.inject.Named

class UpComingLaunchesViewModel(
    private val getUpComingLaunchUseCase: GetUpComingLaunchesUC,
    @Named(OBSERVER_ON) private val observerOn: Scheduler
) : BaseViewModel() {

    private val mutableViewState = MutableLiveData<UpComingLaunchContract.ViewState>()

    val viewState: LiveData<UpComingLaunchContract.ViewState> = mutableViewState

    init {
        //TODO check unit test with this
        //fetchLaunchEvents()
    }

    private fun currentViewState(): UpComingLaunchContract.ViewState =
        viewState.value ?: UpComingLaunchContract.ViewState()

    fun fetchLaunchEvents() {

        addDisposable(
            getUpComingLaunchUseCase.run(GetUpComingLaunchesUC.RequestValues())
                .observeOn(observerOn)
                .doOnSubscribe {
                    mutableViewState.update(isLoading = true)
                }
                .subscribeWith(
                    object : DisposableSingleObserver<GetUpComingLaunchesUC.ResponseValue>() {
                        override fun onSuccess(apiResponse: GetUpComingLaunchesUC.ResponseValue) {
                            Timber.d("Up Coming Events = ${apiResponse.upComingLaunchEventList.size}")
                            mutableViewState.update(activityData = apiResponse.upComingLaunchEventList)
                        }

                        override fun onError(error: Throwable) {
                            if (error is IOException) {
                                mutableViewState.update(errorState = PageErrorState.NO_NETWORK)
                            } else if (error is HttpException) {
                                mutableViewState.update(errorState = PageErrorState.SERVER_ERROR)
                            }
                            Timber.e(error)
                        }
                    }
                )
        )
    }

    fun MutableLiveData<UpComingLaunchContract.ViewState>.update(
        isLoading: Boolean = false,
        activityData: List<LaunchEventDTO> ?= null,
        errorState: PageErrorState? = null
    ) {
        this.value = currentViewState().copy(
            isLoading = isLoading,
            activityData = activityData,
            errorState = errorState
        )
    }
}
