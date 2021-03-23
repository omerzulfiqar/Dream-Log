package edu.vt.cs.cs5254.dreamcatcher

import android.util.Log
import androidx.lifecycle.ViewModel
import edu.vt.cs.cs5254.dreamcatcher.database.Dream
import edu.vt.cs.cs5254.dreamcatcher.database.DreamEntry
import edu.vt.cs.cs5254.dreamcatcher.database.DreamEntryKind
import edu.vt.cs.cs5254.dreamcatcher.database.DreamWithEntries

private const val TAG = "DreamListFraggment"

class DreamListViewModel : ViewModel() {
    private val dreamRepository = DreamRepository.get()
    val dreamListLiveData = dreamRepository.getDreams()


    // ADDING NEW DREAM WITH THE INITIAL REVEALED ENTRY
    fun addNewDream(dream: Dream) {
        var dreamEntries = listOf<DreamEntry>()
        dreamEntries = dreamEntries.plus(
            DreamEntry(
                dreamId = dream.id,
                kind = DreamEntryKind.REVEALED,
                comment = "Dream Revealed")
        )
        dreamRepository.addDreamWithEntries(DreamWithEntries(dream, dreamEntries))
        Log.d(TAG, "New Dream Entry: ${dreamEntries.toString()}")
    }


}
