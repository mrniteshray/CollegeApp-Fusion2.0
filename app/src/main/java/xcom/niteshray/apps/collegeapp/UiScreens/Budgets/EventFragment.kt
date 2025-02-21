package xcom.niteshray.apps.collegeapp.UiScreens.Budgets

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import xcom.niteshray.apps.collegeapp.databinding.FragmentEventBinding
import xcom.niteshray.apps.collegeapp.model.Event

class EventFragment : Fragment() {

    private lateinit var binding : FragmentEventBinding
    private val db = FirebaseFirestore.getInstance()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEventBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.addEvent.setOnClickListener{
            val action = EventFragmentDirections.actionEventFragmentToAddEventFragment()
            findNavController().navigate(action)
        }

        binding.recv.layoutManager = LinearLayoutManager(requireContext())
        db.collection("events").get().addOnSuccessListener{ document ->
            val eventlist = mutableListOf<Event>()
            for (documentr in document){
                val event = documentr.toObject(Event::class.java)
                eventlist.add(event)
            }
            binding.recv.adapter = EventAdapter(requireContext(),eventlist){
                val action = EventFragmentDirections.actionEventFragmentToEventDetailsFragment(it)
                findNavController().navigate(action)
            }
        }
    }
}