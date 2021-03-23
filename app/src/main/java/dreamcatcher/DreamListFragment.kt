package edu.vt.cs.cs5254.dreamcatcher

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import edu.vt.cs.cs5254.dreamcatcher.database.Dream
import edu.vt.cs.cs5254.dreamcatcher.database.DreamWithEntries
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "DreamListFragment"

class DreamListFragment : Fragment() {
    interface Callbacks {
        fun onDreamSelected(dreamId: UUID)
    }

    private var callbacks: Callbacks? = null

    // View Fields
    private lateinit var dreamRecyclerView: RecyclerView
    private var adapter: DreamAdapter? = DreamAdapter(emptyList())

    private val dreamListViewModel: DreamListViewModel by lazy {
        ViewModelProvider(this).get(DreamListViewModel::class.java)
    }

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    //model fields
    private var allDreams: List<Dream> = emptyList()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dream_list, container, false)
        dreamRecyclerView = view.findViewById(R.id.dream_recycler_view) as RecyclerView
        dreamRecyclerView.layoutManager = LinearLayoutManager(context)
        dreamRecyclerView.adapter = adapter
        navigationView = view.findViewById(R.id.nav_view)
        drawerLayout = view.findViewById(R.id.drawer_layout)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dreamListViewModel.dreamListLiveData.observe(
            viewLifecycleOwner,
            Observer { dreams ->
                dreams?.let {
                    Log.i(TAG, "Got dreams ${dreams.size}")
                    allDreams = dreams
                    updateUI(dreams)
                }
            })

        // NAVIGATION DRAWER FROM LEFT OF SCREEN
        navigationView.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            val dreamFilter = when (menuItem.itemId) {
                R.id.nav_all_dreams -> { dream: Dream -> true }
                R.id.nav_active_dreams -> { dream: Dream -> !dream.isRealized && !dream.isDeferred }
                R.id.nav_realized_dreams -> { dream: Dream -> dream.isRealized }
                R.id.nav_deferred_dreams -> { dream: Dream -> dream.isDeferred }
                else -> { dream: Dream -> false }
            }
            val dreamsToDisplay: List<Dream> = allDreams.filter { dream -> dreamFilter(dream) }
            updateUI(dreamsToDisplay)
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

    }

    // NEW DREAM MENU OPTION
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_dream_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.new_dream -> {
                val dream = Dream()
                dreamListViewModel.addNewDream(dream)
                callbacks?.onDreamSelected(dream.id)
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }


    // View Holder for Dream List
    inner class DreamHolder(view: View) : RecyclerView.ViewHolder(view),
        View.OnClickListener {

        private lateinit var dream: Dream
        private val dreamDescriptionView: TextView = itemView.findViewById(R.id.dream_title)
        private val dreamDateView: TextView = itemView.findViewById(R.id.dream_date)
        private val dreamImageView: ImageView = itemView.findViewById(R.id.dream_icon)

        init {
            itemView.setOnClickListener(this)
        }

        @SuppressLint("SimpleDateFormat")
        fun bind(dream: Dream) {
            this.dream = dream
            dreamDescriptionView.text = this.dream.description
            dreamDateView.text = SimpleDateFormat("MMM dd, yyy").format(this.dream.date).toString()

            // Adjusting the visibility of the required icon according to realized / deferred status
            when {
                dream.isDeferred -> {
                    dreamImageView.setImageResource(R.drawable.dream_deferred_icon)
                    dreamImageView.tag = R.drawable.dream_deferred_icon
                }
                dream.isRealized -> {
                    dreamImageView.setImageResource(R.drawable.dream_realized_icon)
                    dreamImageView.tag = R.drawable.dream_realized_icon
                }
                else -> {
                    dreamImageView.setImageResource(0)
                    dreamImageView.tag = 0
                }
            }
        }

        override fun onClick(v: View) {
            callbacks?.onDreamSelected(dream.id)
        }
    }


    // Adapter to create view holder and bind data to view holder from model layer
    private inner class DreamAdapter(var dreams: List<Dream>) :
        RecyclerView.Adapter<DreamHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DreamHolder {
            val view = layoutInflater.inflate(R.layout.list_item_dream_detail, parent, false)
            return DreamHolder(view)
        }

        override fun getItemCount(): Int {
            return dreams.size
        }

        override fun onBindViewHolder(holder: DreamHolder, position: Int) {
            val dream = dreams[position]
            holder.bind(dream)
        }
    }


    //UPDATE UI TO ACCOMMODATE FOR CHANGES AND ICONS
    private fun updateUI(dreams: List<Dream>) {
        adapter = DreamAdapter(dreams)
        dreamRecyclerView.adapter = adapter
    }


    companion object {
        fun newInstance() = DreamListFragment()
    }


}