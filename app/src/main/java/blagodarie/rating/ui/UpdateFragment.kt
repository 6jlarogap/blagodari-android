package blagodarie.rating.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import blagodarie.rating.update.UpdateManager

class UpdateFragment : Fragment() {
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        UpdateManager.INSTANCE.toUpdate(requireContext())
        requireActivity().onBackPressed()
    }
}