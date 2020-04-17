package com.goforer.grabph.presentation.vm.uploadquest

import androidx.lifecycle.MutableLiveData
import com.goforer.grabph.domain.Parameters
import com.goforer.grabph.presentation.vm.BaseViewModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UploadQuestViewModel
@Inject
constructor(): BaseViewModel<Parameters>() {
    internal val selectedKeyword = MutableLiveData<String>()
    internal val title = MutableLiveData<String>()
    internal val description = MutableLiveData<String>()
    internal val reward = MutableLiveData<String>()
    internal val duration = MutableLiveData<Long>()

    override fun setParameters(parameters: Parameters, type: Int) {
    }

    internal fun setKeyword(keyword: String) {
        selectedKeyword.value = keyword
    }

    internal fun setDescription(text: String) {
        description.value = text
    }

    internal fun setReward(value: String) {
        reward.value = value
    }

    internal fun setDuration(value: Long) {
        duration.value = value
    }
}