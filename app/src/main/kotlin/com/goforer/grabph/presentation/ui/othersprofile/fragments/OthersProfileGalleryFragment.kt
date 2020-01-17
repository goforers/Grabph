package com.goforer.grabph.presentation.ui.othersprofile.fragments

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.goforer.base.presentation.utils.CommonUtils.betterSmoothScrollToPosition
import com.goforer.base.presentation.view.customs.layout.CustomStaggeredGridLayoutManager
import com.goforer.base.presentation.view.decoration.GapItemDecoration
import com.goforer.base.presentation.view.fragment.BaseFragment
import com.goforer.grabph.R
import com.goforer.grabph.data.datasource.model.cache.data.entity.photog.Photo
import com.goforer.grabph.data.datasource.network.resource.NetworkBoundResource.Companion.BOUND_FROM_LOCAL
import com.goforer.grabph.data.datasource.network.resource.NetworkBoundResource.Companion.LOAD_PHOTOG_PHOTO
import com.goforer.grabph.data.datasource.network.response.Status
import com.goforer.grabph.domain.Parameters
import com.goforer.grabph.presentation.common.utils.AutoClearedValue
import com.goforer.grabph.presentation.ui.othersprofile.OthersProfileActivity
import com.goforer.grabph.presentation.ui.othersprofile.adapter.OthersProfileAdapter
import com.goforer.grabph.presentation.vm.othersprofile.OthersProfileViewModel
import kotlinx.android.synthetic.main.fragment_home_profile_photos.*
import javax.inject.Inject

@Suppress("UNCHECKED_CAST")
class OthersProfileGalleryFragment: BaseFragment() {
    private val profileActivity: OthersProfileActivity by lazy { activity as OthersProfileActivity }

    private var adapter: OthersProfileAdapter? = null

    private lateinit var acvAdapterGallery: AutoClearedValue<OthersProfileAdapter>
    private lateinit var gridLayoutManager: CustomStaggeredGridLayoutManager

    private lateinit var userId: String

    @field:Inject
    lateinit var viewModel: OthersProfileViewModel

    companion object {
        private const val USER_ID_KEY = "searp:user_id_in_gallery_fragment"

        fun newInstance(user: String): OthersProfileGalleryFragment{
            val args = Bundle()
            args.putString(USER_ID_KEY, user)

            val fragment = OthersProfileGalleryFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val acvView =
            AutoClearedValue(this, inflater.inflate(R.layout.fragment_home_profile_photos, container, false))
        return acvView.get()?.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState != null) {
            savedInstanceState.getString(USER_ID_KEY)?.let { userId = it }
        } else {
            arguments?.getString(USER_ID_KEY)?.let { userId = it }
        }

        createAdapter()
        observeLiveData()
        setFloatingButtonClickListener()
        setListScrollBehavior()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(USER_ID_KEY, userId)
    }

    private fun observeLiveData() {
        viewModel.setParametersPhotos(Parameters(userId, -1, LOAD_PHOTOG_PHOTO, BOUND_FROM_LOCAL))

        val liveData = viewModel.photos
        liveData.observe(profileActivity, Observer { resource ->

            when (resource?.getStatus()) {
                Status.SUCCESS -> {
                    resource.getData()?.let { list ->
                        val gallery = list as? PagedList<Photo>
                        gallery?.let {
                            if (userId == list[0]?.owner) submitGallery(it)
                            showEmptyMessage(it.isEmpty())
                            this.recycler_profile_photos.visibility = View.VISIBLE
                            this.layout_before_loading_gallery.visibility = View.GONE
                        }
                    }

                    resource.getMessage()?.let {
                        profileActivity.showNetworkError(resource)
                        liveData.removeObservers(this)
                    }
                }

                Status.LOADING -> {  }

                Status.ERROR -> {
                    liveData.removeObservers(this)
                }

                else -> {
                    liveData.removeObservers(this)
                }
            }
        })
    }

    private fun createAdapter() {
        adapter = adapter ?: OthersProfileAdapter(profileActivity)
        acvAdapterGallery = AutoClearedValue(this, adapter)
        this.recycler_profile_photos.setHasFixedSize(true)
        this.recycler_profile_photos.isVerticalScrollBarEnabled = false
        gridLayoutManager = CustomStaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        gridLayoutManager.enabledSrcoll = true
        gridLayoutManager.isItemPrefetchEnabled = true
        this.recycler_profile_photos.layoutManager = gridLayoutManager
        this.recycler_profile_photos.addItemDecoration(createItemDecoration())
        this.recycler_profile_photos.adapter = acvAdapterGallery.get()
    }

    private fun createItemDecoration(): RecyclerView.ItemDecoration {
        return object :
            GapItemDecoration(VERTICAL_LIST, resources.getDimensionPixelSize(R.dimen.space_4)) {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.left = 4
                outRect.right = 4
                outRect.bottom = 4
                outRect.top = 4

                // Add top margin only for the first item to avoid double space between items
                if (parent.getChildAdapterPosition(view) == 0 || parent.getChildAdapterPosition(view) == 1) {
                }
            }
        }
    }

    private fun submitGallery(gallery: PagedList<Photo>) {
        acvAdapterGallery.get()?.submitList(gallery)
    }

    private fun showEmptyMessage(isEmpty: Boolean) {
        if (isEmpty) {
            this.tv_empty_list.visibility = View.VISIBLE
            this.tv_empty_list.text = "No photos in gallery"
        } else {
            this.tv_empty_list.visibility = View.GONE
        }
    }

    private fun setListScrollBehavior() {
        val fab = this.fam_gallery_top
        this.recycler_profile_photos.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0 && fab.isShown) fab.hide(true)
                if (dy < 0 && fab.isHidden) fab.show(true)
            }
        })
    }

    private fun setFloatingButtonClickListener() {
        this.fam_gallery_top.setOnClickListener {
            betterSmoothScrollToPosition(this.recycler_profile_photos, 0)
        }
    }
}