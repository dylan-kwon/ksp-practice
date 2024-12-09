package dylan.kwon.kotlin.ksp.practice

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dylan.kwon.kotlin.ksp.practice.repository.Repository
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    fun getData(): String {
        return repository.get()
    }
}