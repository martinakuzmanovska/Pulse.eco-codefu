import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class StepCounterViewModel : ViewModel() {
    private val _stepCount = MutableLiveData<Int>()
    fun getStepCountValue():Int?
    {
        return _stepCount.value
    }

    fun updateStepCount() {
        // Check if _stepCount.value is null
        if (_stepCount.value == null) {
            _stepCount.value = 0 // Initialize the value to 0 if it is null
        } else {
            _stepCount.value = _stepCount.value!! + 1 // Safely increment
        }
    }

    fun setStepCount(number:Int)
    {
        _stepCount.value=number
    }

    fun getStepCount():MutableLiveData<Int>
    {
        return this._stepCount
    }
}
