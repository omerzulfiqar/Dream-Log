package edu.vt.cs.cs5254.dreamcatcher

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import edu.vt.cs.cs5254.dreamcatcher.database.DreamEntry
import edu.vt.cs.cs5254.dreamcatcher.database.DreamEntryKind
import edu.vt.cs.cs5254.dreamcatcher.database.DreamWithEntries
import java.io.File
import java.util.UUID

private const val TAG = "DreamDetailViewModel"

class DreamDetailViewModel : ViewModel() {
    private val dreamRepository = DreamRepository.get()
    private val dreamIdLiveData = MutableLiveData<UUID>()

    // PASSING THROUGH LIVE DREAM DATA
    var dreamLiveData: LiveData<DreamWithEntries?> =
        Transformations.switchMap(dreamIdLiveData) { dreamId ->
            dreamRepository.getDreamWithEntries(dreamId)
        }

    // LOADING CURRENT DREAM ID SENT FROM FRAGMENT
    fun loadDream(dreamId: UUID) {
        dreamIdLiveData.value = dreamId
    }

    // SAVING THE DREAM CHANGES
    fun saveDream() {
        dreamLiveData.value?.let {
            dreamRepository.updateDreamWithEntries(it)
        }
    }

    // GET THE PHOTO
    fun getPhotoFile(dreamWithEntries: DreamWithEntries): File {
        return dreamRepository.getPhotoFile(dreamWithEntries.dream)
    }

    // UPDATE BOTH DREAM AND DREAMENTRIES
    fun updateDreamWithEntry(dreamWithEntries: DreamWithEntries) {
        dreamRepository.updateDreamWithEntries(dreamWithEntries)
    }

    // UPDATING DREAM ENTRIES
    fun updateDreamEntries(dreamEntries: List<DreamEntry>){
        Log.d("Entries Left:", "${dreamEntries.size}")
        dreamLiveData.value?.let{
            it.dreamEntries = dreamEntries
            dreamRepository.updateDreamWithEntries(it)
        }

    }

    // REALIZED CHECKBOX ONCLICK HANDLER AND UPDATER
    fun onRealizedClicked(isChecked: Boolean) {
        dreamLiveData.value?.let {
            it.dream.isRealized = isChecked

            it.dreamEntries =
                it.dreamEntries.minus(it.dreamEntries.filter { entry -> entry.kind == DreamEntryKind.REALIZED || entry.kind == DreamEntryKind.DEFERRED })
            if (isChecked) {
                it.dreamEntries = it.dreamEntries + DreamEntry(
                    dreamId = it.dream.id,
                    kind = DreamEntryKind.REALIZED,
                    comment = "Dream Realized"
                )
            }
        }
    }


    // DEFERRED CHECK BOX ONCLICK HANDLER
    fun onDeferredClicked(isChecked: Boolean) {
        dreamLiveData.value?.let {
            it.dream.isDeferred = isChecked

            it.dreamEntries =
                it.dreamEntries.minus(it.dreamEntries.filter { entry -> entry.kind == DreamEntryKind.REALIZED || entry.kind == DreamEntryKind.DEFERRED })
            if (isChecked) {
                it.dreamEntries = it.dreamEntries + DreamEntry(
                    dreamId = it.dream.id,
                    kind = DreamEntryKind.DEFERRED,
                    comment = "Dream Deferred"
                )
            }
        }
    }

}
