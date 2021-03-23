package edu.vt.cs.cs5254.dreamcatcher

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import edu.vt.cs.cs5254.dreamcatcher.database.Dream
import edu.vt.cs.cs5254.dreamcatcher.database.DreamDatabase
import edu.vt.cs.cs5254.dreamcatcher.database.DreamWithEntries
import java.io.File
import java.util.*
import java.util.concurrent.Executors

private const val DATABASE_NAME = "dream_database"
private const val TAG = "DreamRepository"

class DreamRepository private constructor(context: Context) {

    private val executor = Executors.newSingleThreadExecutor()
    private val filesDir = context.applicationContext.filesDir

    // GETTING DREAM PHOTO
    fun getPhotoFile(dream: Dream): File {
        return File(filesDir, dream.photoFileName)
    }

    private val repopulateRoomDatabaseCallback: RoomDatabase.Callback =
        object : RoomDatabase.Callback() {
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                Log.d(TAG, "repopulateRoomDatabaseCallback.onOpen")
                executor.execute {
                    dreamDao.apply {
                        reconstructSampleDatabase()
                    }
                }
            }
        }

    // INITIALIZING DATABASE ACCESS
    private val database: DreamDatabase =
        Room.databaseBuilder(
            context.applicationContext,
            DreamDatabase::class.java, DATABASE_NAME
        ).build()
/*    private val database: DreamDatabase =
        Room.databaseBuilder(
            context.applicationContext,
            DreamDatabase::class.java, DATABASE_NAME
        ).addCallback(repopulateRoomDatabaseCallback).build()*/

    private val dreamDao = database.dreamDao()

    // ONLY GETTING DREAMS
    fun getDreams(): LiveData<List<Dream>> = dreamDao.getDreams()

    // ONLY GETTING DREAM OBJECT
    fun getDream(dreamId: UUID): LiveData<Dream?> = dreamDao.getDream(dreamId)

    // GETTING BOTH DREAM AND DREAM ENTRY OBJECTS
    fun getDreamWithEntries(dreamId: UUID): LiveData<DreamWithEntries?> =
        dreamDao.getDreamWithEntries(dreamId)

    // UPDATING THE DREAM CLASS ALONG WITH THE ENTRY CLASS
    fun updateDreamWithEntries(dreamWithEntries: DreamWithEntries) {
        executor.execute {
            dreamDao.updateDreamWithEntries(dreamWithEntries)
        }
    }

    // ADD NEW DREAM OBJ
    fun addNewDream(dream: Dream) {
        executor.execute {
            dreamDao.addDream(dream)
        }
    }


    // ADD NEW DREAM AND ENTRIES
    fun addDreamWithEntries(dreamWithEntries: DreamWithEntries) {
        executor.execute {
            dreamDao.addDreamWithEntries(dreamWithEntries)
        }
    }

    //DELETE ALL DREAMS ---- FOR TESTING
    fun deleteAllDreams(){
        executor.execute {
            dreamDao.deleteAllDreams()
        }
    }


    fun reconstructSampleDatabase() = dreamDao.reconstructSampleDatabase()

    companion object {
        private var INSTANCE: DreamRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = DreamRepository(context)
            }
        }

        fun get(): DreamRepository {
            return INSTANCE
                ?: throw IllegalStateException("Dream Repository Must Be Initialized")
        }
    }
}