package com.zotiko.spacelaunchnow.ui.upcominglaunches.list

import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.zotiko.spacelaunchnow.databinding.FragmentMainBinding
import com.zotiko.spacelaunchnow.di.modules.ViewModelFactory
import com.zotiko.spacelaunchnow.ui.extension.startWithFade
import com.zotiko.spacelaunchnow.ui.upcominglaunches.UpComingLaunchContract
import com.zotiko.spacelaunchnow.ui.upcominglaunches.UpComingLaunchesViewModel
import com.zotiko.spacelaunchnow.ui.upcominglaunches.list.adapter.LaunchEventListAdapter
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_main.*
import timber.log.Timber
import javax.inject.Inject

class LaunchEventListFragment : Fragment(), HasAndroidInjector {

    @Inject
    lateinit var androidInjector: DispatchingAndroidInjector<Any>

    @Inject
    lateinit var vmFactory: ViewModelFactory

    private lateinit var viewModel: UpComingLaunchesViewModel

    private lateinit var fragmentBinding: FragmentMainBinding

    private lateinit var launchEventListAdapter: LaunchEventListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, vmFactory).get(UpComingLaunchesViewModel::class.java)
        viewModel.fetchLaunchEvents()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragmentBinding = FragmentMainBinding.inflate(inflater, container, false)
        launchEventListAdapter = LaunchEventListAdapter {
            val action =
                LaunchEventListFragmentDirections.actionMainFragmentToDetailFragment(it)
            findNavController().navigate(action)
        }
        setUpRecyclerView()
        return fragmentBinding.root
    }

    private fun setUpRecyclerView() {
        fragmentBinding.launchEventRecyclerView.adapter = launchEventListAdapter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentBinding.apply {
            lifecycleOwner = viewLifecycleOwner
            uiViewModel = viewModel
        }
        (imv_space_background.drawable as AnimationDrawable).startWithFade()
        viewModel.viewState.observe(viewLifecycleOwner, Observer {
            Timber.d("Launch Event : $it")
            it?.let { render(it) }
        })
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fragmentBinding.launchEventRecyclerView.adapter = null
    }

    private fun render(viewState: UpComingLaunchContract.ViewState) {

        when (viewState.isLoading) {
            true -> fragmentBinding.pageLoadingIndicator.visibility = View.VISIBLE
            false -> fragmentBinding.pageLoadingIndicator.visibility = View.GONE
        }
        if (viewState.errorState != null) {
            showError(viewState.errorState.message.getText(requireContext()).toString())
            showListView(false)
        }

        viewState.activityData?.let {
            if (it.isNotEmpty()) {
                showListView(true)
                launchEventListAdapter.submitList(viewState.activityData)
            }
        }
    }

    private fun showError(errorMessage: String) {
        Snackbar.make(fragmentBinding.root, errorMessage, Snackbar.LENGTH_LONG).show()
    }

    private fun showListView(show: Boolean) {
        fragmentBinding.launchEventRecyclerView.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun androidInjector(): AndroidInjector<Any> = androidInjector
}
