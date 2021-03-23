package edu.vt.cs.cs5254.dreamcatcher.database

import androidx.lifecycle.LiveData
import androidx.room.*

import java.util.*

@Dao
interface DreamDao {
    @Update // UPDATE ONLY DREAM OBJECT
    fun updateDream(dream: Dream)

    @Insert // INSERT ONLY DREAM OBJECT
    fun addDream(dream: Dream)

    @Query("SELECT * FROM dream") // GET ONLY DREAM OBJECTS
    fun getDreams(): LiveData<List<Dream>>

    @Query("SELECT * FROM dream WHERE id=(:dreamId)") // GET ONLY SPECIFIC DREAM OBJECT
    fun getDream(dreamId: UUID): LiveData<Dream?>

    @Insert // INSERT NEW / UPDATED DREAM ENTRY
    fun addDreamEntry(dreamEntry: DreamEntry)

    @Query("SELECT * FROM dream WHERE id=(:dreamId)")  // GET BOTH DREAM AND DREAM ENTRY OBJECTS FOR SPECIFIC DREAM
    fun getDreamWithEntries(dreamId: UUID): LiveData<DreamWithEntries?>

    @Query("DELETE FROM dream_entry WHERE dreamId=(:dreamId)") // DELETE DREAM ENTRY OBJECTS FOR SPECIFIC DREAM
    fun deleteDreamEntries(dreamId: UUID)

    @Transaction  // UPDATE A DREAM AND DREAM ENTRY OBJECTS FOR A SPECIFIC DreamWithEntries OBJECT
    fun updateDreamWithEntries(dreamWithEntries: DreamWithEntries) {
        val theDream = dreamWithEntries.dream
        val theEntries = dreamWithEntries.dreamEntries
        updateDream(dreamWithEntries.dream)
        deleteDreamEntries(theDream.id)
        theEntries.forEach { e -> addDreamEntry(e) }
    }

    //TODO Check this function, you already have add dream which you use on listFragment
    @Transaction // ADD NEW DREAM AND DREAM ENTRIES
    fun addDreamWithEntries(dreamWithEntries: DreamWithEntries) {
        addDream(dreamWithEntries.dream)
        dreamWithEntries.dreamEntries.forEach { e -> addDreamEntry(e) }
    }

    // DELETE ALL DREAMS
    @Query("DELETE FROM dream")
    fun deleteAllDreams()

    //DELETE ALL DREAM ENTRIES
    @Query("DELETE FROM dream_entry")
    fun deleteAllDreamEntries()

    @Transaction
      fun reconstructSampleDatabase() {
          deleteAllDreams()

          val dream0 = Dream(
              description = "Dream #0",
              isRealized = false
          )
          val dream0Entries = listOf(
              DreamEntry(
                  dreamId = dream0.id,
                  kind = DreamEntryKind.REVEALED,
                  comment = "Dream Revealed"
              ),
              DreamEntry(
                  dreamId = dream0.id,
                  kind = DreamEntryKind.COMMENT,
                  comment = "Dream 0 Entry 1"
              ),
              DreamEntry(
                  dreamId = dream0.id,
                  kind = DreamEntryKind.COMMENT,
                  comment = "Dream 0 Entry 2"
              ),
              DreamEntry(
                  dreamId = dream0.id,
                  kind = DreamEntryKind.COMMENT,
                  comment = "Dream 0 Entry 3"
              )
          )
          addDreamWithEntries(DreamWithEntries(dream0, dream0Entries))


          val dream1 = Dream(
              description = "Dream #1",
              isDeferred = true
          )
          val dream1Entries = listOf(
              DreamEntry(
                  dreamId = dream1.id,
                  kind = DreamEntryKind.REVEALED,
                  comment = "Dream Revealed"
              ),
              DreamEntry(
                  dreamId = dream1.id,
                  kind = DreamEntryKind.COMMENT,
                  comment = "Dream 1 Entry 1"
              ),
              DreamEntry(
                  dreamId = dream1.id,
                  kind = DreamEntryKind.COMMENT,
                  comment = "Dream 1 Entry 2"
              ),
              DreamEntry(
                  dreamId = dream1.id,
                  kind = DreamEntryKind.COMMENT,
                  comment = "Dream 1 Entry 3"
              ),
              DreamEntry(
                  dreamId = dream1.id,
                  kind = DreamEntryKind.COMMENT,
                  comment = "Dream 1 Entry 4"
              ),
              DreamEntry(
                  dreamId = dream1.id,
                  kind = DreamEntryKind.DEFERRED,
                  comment = "Dream Deferred"
              )
          )
          addDreamWithEntries(DreamWithEntries(dream1, dream1Entries))


          val dream2 =
              Dream(description = "Dream #2", isRealized = true)
          val dream2Entries = listOf(
              DreamEntry(
                  dreamId = dream2.id,
                  kind = DreamEntryKind.REVEALED,
                  comment = "Dream Revealed"
              ),
              DreamEntry(
                  dreamId = dream2.id,
                  kind = DreamEntryKind.COMMENT,
                  comment = "Dream 2 Entry 1"
              ),
              DreamEntry(
                  dreamId = dream2.id,
                  kind = DreamEntryKind.COMMENT,
                  comment = "Dream 2 Entry 2"
              ),
              DreamEntry(
                  dreamId = dream2.id,
                  kind = DreamEntryKind.COMMENT,
                  comment = "Dream 2 Entry 3"
              ),
              DreamEntry(
                  dreamId = dream2.id,
                  kind = DreamEntryKind.COMMENT,
                  comment = "Dream 2 Entry 4"
              ),
              DreamEntry(
                  dreamId = dream2.id,
                  kind = DreamEntryKind.COMMENT,
                  comment = "Dream 2 Entry 5"
              ),
              DreamEntry(
                  dreamId = dream2.id,
                  kind = DreamEntryKind.COMMENT,
                  comment = "Dream 2 Entry 6"
              ),
              DreamEntry(
                  dreamId = dream2.id,
                  kind = DreamEntryKind.COMMENT,
                  comment = "Dream 2 Entry 7"
              ),
              DreamEntry(
                  dreamId = dream2.id,
                  kind = DreamEntryKind.REALIZED,
                  comment = "Dream Realized"
              )
          )
          addDreamWithEntries(DreamWithEntries(dream2, dream2Entries))

  /*        for (i in 3..50) {
              val dream =
                  Dream(
                      description = "Dream #$i",
                      isDeferred = (i % 3 == 1),
                      isRealized = (i % 3 == 2)
                  )
              var entries = listOf(
                  DreamEntry(
                      dreamId = dream.id,
                      kind = DreamEntryKind.REVEALED,
                      comment = "Dream Revealed"
                  )
              )
              if (dream.isDeferred) {
                  entries = entries + DreamEntry(
                      dreamId = dream.id,
                      kind = DreamEntryKind.DEFERRED,
                      comment = "Dream Deferred"
                  )
              }
              if (dream.isRealized) {
                  entries = entries + DreamEntry(
                      dreamId = dream.id,
                      kind = DreamEntryKind.REALIZED,
                      comment = "Dream Realized"
                  )
              }

              addDreamWithEntries(DreamWithEntries(dream, entries))
          }*/
    }
}