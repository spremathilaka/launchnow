package com.zotiko.spacelaunchnow.ui.upcominglaunches

import com.zotiko.spacelaunchnow.dto.LaunchEventDTO
import com.zotiko.spacelaunchnow.ui.data.PageErrorState

interface UpComingLaunchContract {

    data class ViewState(
        val isLoading: Boolean = false,
        val activityData: List<LaunchEventDTO> ? = null,
        val errorState: PageErrorState? = null
    )
}