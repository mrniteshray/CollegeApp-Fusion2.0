package xcom.niteshray.apps.collegeapp.UiScreens.Budgets

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import xcom.niteshray.apps.collegeapp.R

class EventDetailsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_event_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = EventDetailsFragmentArgs.fromBundle(requireArguments())
        val event = args.event
        Toast.makeText(requireContext(), "Event Name: ${event.title}", Toast.LENGTH_SHORT).show()
    }


}