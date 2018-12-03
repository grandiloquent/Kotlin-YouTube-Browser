package euphoria.psycho.browser

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    private val experimentsLiveData = MutableLiveData<Boolean>()

    init {
        experimentsLiveData.value = false
    }

    fun showExperiments() {
        experimentsLiveData.value = true
    }

    fun getExperimentsLiveData(): LiveData<Boolean> {
        return experimentsLiveData
    }
}
