package edu.vt.cs.cs5254.dreamcatcher

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import edu.vt.cs.cs5254.dreamcatcher.database.DreamEntry
import edu.vt.cs.cs5254.dreamcatcher.database.DreamEntryKind
import edu.vt.cs.cs5254.dreamcatcher.database.DreamWithEntries
import edu.vt.cs.cs5254.dreamcatcher.util.CameraUtil
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "DreamDetailFragment"
private const val ARG_DREAM_ID = "dream_id"
private const val REQUEST_COMMENT = 0
private const val REQUEST_PHOTO = 1
private const val DIALOG_COMMENT = "DialogComment"


class DreamDetailFragment : Fragment() {

    // Model Fields
    private lateinit var dreamWithEntries: DreamWithEntries
    private lateinit var photoFile: File
    private lateinit var photoUri: Uri

    //View fields
    private lateinit var dreamTitleField: EditText
    private lateinit var realizedCheckBox: CheckBox
    private lateinit var deferredCheckBox: CheckBox
    private lateinit var photoView: ImageView
    private lateinit var dreamIconView: ImageView
    private lateinit var viewModel: DreamDetailViewModel
    private lateinit var addCommentFAB: FloatingActionButton

    //Recycler View
    private lateinit var dreamEntryRecyclerView: RecyclerView
    private var adapter: DreamEntryAdapter? = DreamEntryAdapter(emptyList())


    //Callbacks
    private var callbacks: Callbacks? = null

    interface Callbacks {
        fun onDreamSelected(dreamId: UUID)
    }

    // Detail View Model
    private val dreamDetailViewModel: DreamDetailViewModel by lazy {
        ViewModelProvider(this@DreamDetailFragment).get(DreamDetailViewModel::class.java)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        val dreamId: UUID = arguments?.getSerializable(ARG_DREAM_ID) as UUID
        dreamDetailViewModel.loadDream(dreamId)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dream_detail, container, false)
        // Initializing View Fields
        dreamTitleField = view.findViewById(R.id.dream_title) as EditText
        realizedCheckBox = view.findViewById(R.id.dream_realized) as CheckBox
        deferredCheckBox = view.findViewById(R.id.dream_deferred) as CheckBox
        dreamIconView = view.findViewById(R.id.dream_fragment_icon) as ImageView
        photoView = view.findViewById(R.id.dream_photo) as ImageView
        addCommentFAB = view.findViewById(R.id.add_comment_fab) as FloatingActionButton

        //Initializing Recycler View
        dreamEntryRecyclerView = view.findViewById(R.id.dream_entry_recycler_view) as RecyclerView
        dreamEntryRecyclerView.layoutManager = LinearLayoutManager(context)
        dreamEntryRecyclerView.adapter = adapter

        //Initializing swipeToDelete function
        val itemTouchHelper = ItemTouchHelper(SwipeToDeleteCallback())
        itemTouchHelper.attachToRecyclerView(dreamEntryRecyclerView)

        return view
    }

    /*
        Passing through dreamWithEntries object to observer allows you to easily access both
        Dream() and dreamEntries = List<DreamEntry>()
        Passing through photo file path with URI
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dreamDetailViewModel.dreamLiveData.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { dreamWithEntries ->
                dreamWithEntries?.let {
                    this.dreamWithEntries = it
                    photoFile = dreamDetailViewModel.getPhotoFile(dreamWithEntries)
                    photoUri = FileProvider.getUriForFile(
                        requireActivity(),
                        "edu.vt.cs.cs5254.dreamcatcher.fileprovider",
                        photoFile
                    )
                    updateUI()
                }
            }
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(DreamDetailViewModel::class.java)
    }


    override fun onStart() {
        super.onStart()
        // Dream Description text watcher, tracks any changes in description/title name and updates the dream
        val dreamTitleWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //Left intentionally blank
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                dreamWithEntries.dream.description = s.toString()
            }

            override fun afterTextChanged(s: Editable?) {
                //Left intentionally blank
            }
        }
        dreamTitleField.addTextChangedListener(dreamTitleWatcher)

        // Realized Checkbox listener, will call the onRealizedClick from view model to update state of the checkbox
        // Will call updateDreamWithEntry() from view model to save any changes and then updateUI()
        realizedCheckBox.apply {
            setOnCheckedChangeListener { _, isChecked ->
                viewModel.onRealizedClicked(isChecked)
                viewModel.updateDreamWithEntry(dreamWithEntries)
                updateUI()
            }
        }

        // Deferred Checkbox listener, will call the onDeferredClick from view model to update state of the checkbox
        // Will call updateDreamWithEntry() from view model to save any changes and then updateUI()
        deferredCheckBox.apply {
            setOnCheckedChangeListener { _, isChecked ->
                viewModel.onDeferredClicked(isChecked)
                viewModel.updateDreamWithEntry(dreamWithEntries)
                updateUI()
            }
        }

        //Add Comment FAB Listener
        addCommentFAB.setOnClickListener {
            val fragmentManager = this@DreamDetailFragment.fragmentManager
            val dialog = AddDreamEntryFragment()
            dialog.setTargetFragment(this@DreamDetailFragment, REQUEST_COMMENT)
            dialog.show(fragmentManager!!, DIALOG_COMMENT)
        }
    }

    // CREATING MENU OPTIONS
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_dream_detail, menu)

        val cameraAvailable = CameraUtil.isCameraAvailable(requireActivity())
        val menuItem = menu.findItem(R.id.take_dream_photo)
        menuItem.apply {
            Log.d(TAG, "Camera Available: $cameraAvailable")
            isEnabled = cameraAvailable
            isVisible = cameraAvailable
        }
        val menuShare = menu.findItem(R.id.share_dream)
    }

    // SELECTING MENU OPTIONS
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            // SELECTING THE CAMERA OPTION
            R.id.take_dream_photo -> {
                val captureImageIntent =
                    CameraUtil.createCaptureImageIntent(requireActivity(), photoUri)
                startActivity(captureImageIntent)
                true
            }
            // SELECTING THE SHARE OPTION
            R.id.share_dream -> {
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, getDreamReport())
                    putExtra(
                        Intent.EXTRA_SUBJECT,
                        getString(R.string.dream_report)
                    )
                }.also { intent ->
                    val chooserIntent =
                        Intent.createChooser(intent, getString(R.string.send_report))
                    startActivity(chooserIntent)
                }
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    // CONNECTING THE ACTIVITIES WITH RESPECTIVE UTILITY OR DIALOG FRAGMENT
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            resultCode != Activity.RESULT_OK -> return
            // GETTING THE PHOTO AND UPDATING THE VIEW
            requestCode == REQUEST_PHOTO -> {
                requireActivity().revokeUriPermission(
                    photoUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                dreamDetailViewModel.saveDream()
                updatePhotoView()
            }
            // GETTING NEW COMMENT, ADDING THE ENTRY AND UPDATING VIEW
            requestCode == REQUEST_COMMENT -> {
                val newComment =
                    data?.getSerializableExtra(AddDreamEntryFragment.EXTRA_COMMENT) as String
                val entry = DreamEntry(
                    dreamId = dreamWithEntries.dream.id,
                    kind = DreamEntryKind.COMMENT,
                    comment = newComment
                )
                adapter?.addEntry(entry)
                updateUI()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        dreamDetailViewModel.saveDream()
    }

    override fun onDetach() {
        super.onDetach()
        requireActivity().revokeUriPermission(photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    }


    //DREAM ENTRY HOLDER
    inner class DreamEntryHolder(view: View) :
        RecyclerView.ViewHolder(view), View.OnClickListener {
        private lateinit var dreamEntry: DreamEntry
        private val dreamEntryButton: Button = itemView.findViewById(R.id.dream_entry_button)

        init {
            itemView.setOnClickListener(this)
        }

        @SuppressLint("SimpleDateFormat")
        fun bind(dreamEntry: DreamEntry) {
            this.dreamEntry = dreamEntry
            // SETTING RESPECTIVE BUTTON TEXT AND COLOR
            val date = SimpleDateFormat("MMM dd, yyy").format(dreamEntry.dateCreated).toString()
            val text = dreamEntry.comment + " ($date)"
            when (dreamEntry.kind) {
                DreamEntryKind.REVEALED -> {
                    updateButtonUI(dreamEntryButton, text, 205, 160, 235)
                }
                DreamEntryKind.REALIZED -> {
                    updateButtonUI(dreamEntryButton, text, 117, 201, 91)
                }
                DreamEntryKind.DEFERRED -> {
                    updateButtonUI(dreamEntryButton, text, 232, 90, 90)
                }
                DreamEntryKind.COMMENT -> {
                    updateButtonUI(dreamEntryButton, text, 237, 203, 92)
                }
            }
        }

        override fun onClick(v: View) {
            callbacks?.onDreamSelected(dreamEntry.id)
        }
    }


    //DREAM ENTRY ADAPTER
    private inner class DreamEntryAdapter(var dreamEntries: List<DreamEntry>) :
        RecyclerView.Adapter<DreamEntryHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DreamEntryHolder {
            val view = layoutInflater.inflate(R.layout.list_item_dream_entry, parent, false)
            return DreamEntryHolder(view)
        }

        override fun getItemCount(): Int {
            return dreamEntries.size
        }

        override fun onBindViewHolder(holder: DreamEntryHolder, position: Int) {
            val dreamEntry = dreamEntries[position]
            holder.bind(dreamEntry)
        }

        // DELETING ENTRY ON SWIPE
        fun deleteItem(position: Int) {
            val dreamEntryToDelete = dreamEntries[position]
            Log.d(TAG, "Current Entries: ${dreamEntries.size}")
            if (dreamEntryToDelete.kind == DreamEntryKind.COMMENT) {
                dreamEntries = dreamEntries.minus(dreamEntryToDelete)
                dreamDetailViewModel.updateDreamEntries(dreamEntries)
                Log.d(TAG, "Entries left: ${dreamEntries.size}")
                notifyItemRemoved(position)
            } else {
                notifyItemChanged(position)
            }
        }

       // ADDING NEW ENTRY
        fun addEntry(dreamEntry: DreamEntry) {
           Log.d(TAG, "New Entry: $dreamEntry")
            dreamEntries = dreamEntries.plus(dreamEntry)
            dreamDetailViewModel.updateDreamEntries(dreamEntries)
        }

    }

    // SWIPE LEFT TO DELETE COMMENT
    private inner class SwipeToDeleteCallback :
        ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            adapter?.deleteItem(position)
        }
    }

    // THIS METHOD UPDATES THE UI BY CHECKING FOR CHANGES IN THE DEFERRED AND REALIZED STATUS
    // AND UPDATES THE ENTRY BUTTONS ACCORDINGLY
    private fun updateUI() {
        dreamTitleField.setText(dreamWithEntries.dream.description)
        updateCheckbox()
        // NO COMMENTS CAN BE ADDED ONCE DREAM IS REALIZED
        addCommentFAB.isEnabled = !dreamWithEntries.dream.isRealized
        updatePhotoView()
        updateDreamEntriesButton()
        updateIcon()
    }

    // UPDATE BUTTON TEXT AND COLOR
    private fun updateButtonUI(button: Button, buttonText: String, Red: Int, Green: Int, Blue: Int) {
        button.apply { text = buttonText
            setBackgroundColor(Color.rgb(Red, Green, Blue))
        }
    }
    // UPDATING THE DREAM ENTRY BUTTONS
    private fun updateDreamEntriesButton() {
        adapter = DreamEntryAdapter(dreamWithEntries.dreamEntries)
        dreamEntryRecyclerView.adapter = adapter

        val itemTouchHelper = ItemTouchHelper(SwipeToDeleteCallback())
        itemTouchHelper.attachToRecyclerView(dreamEntryRecyclerView)
    }

    //UPDATE CHECKBOXES
    private fun updateCheckbox() {
        realizedCheckBox.apply {
            isChecked = dreamWithEntries.dream.isRealized
            isEnabled = !dreamWithEntries.dream.isDeferred
            jumpDrawablesToCurrentState()
        }
        deferredCheckBox.apply {
            isChecked = dreamWithEntries.dream.isDeferred
            isEnabled = !dreamWithEntries.dream.isRealized
            jumpDrawablesToCurrentState()
        }
    }

    //UPDATE THE IMAGE VIEW
    private fun updatePhotoView() {
        if (photoFile.exists()) {
            val bitmap = CameraUtil.getScaledBitmap(photoFile.path, requireActivity())
            photoView.setImageBitmap(bitmap)
        } else {
            photoView.setImageDrawable(null)
        }
    }

    // UPDATE ICON
    private fun updateIcon() {
        when {
            dreamWithEntries.dream.isRealized -> {

                dreamIconView.setImageResource(R.drawable.dream_realized_icon)
                dreamIconView.tag = R.drawable.dream_realized_icon
            }
            dreamWithEntries.dream.isDeferred -> {
                dreamIconView.setImageResource(R.drawable.dream_deferred_icon)
                dreamIconView.tag = R.drawable.dream_deferred_icon
            }
            else -> {
                dreamIconView.setImageResource(0)
                dreamIconView.tag = 0
            }
        }
    }


    // DREAM REPORT FOR SHARING
    private fun getDreamReport(): String {
        var report = "# " + dreamWithEntries.dream.description + "\n"
        dreamWithEntries.dreamEntries.forEach {
            val date = SimpleDateFormat("MMM dd yyy").format(it.dateCreated).toString()
            report += it.comment + " (" + date + ")\n"
        }
        return report
    }

    companion object {
        fun newInstance(dreamId: UUID): DreamDetailFragment {
            val args = Bundle().apply {
                putSerializable(ARG_DREAM_ID, dreamId)
            }
            return DreamDetailFragment().apply {
                arguments = args
            }
        }
    }


}
