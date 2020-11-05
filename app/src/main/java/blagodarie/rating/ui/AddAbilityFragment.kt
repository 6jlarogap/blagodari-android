package blagodarie.rating.ui

import android.accounts.Account
import android.accounts.AccountManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import blagodarie.rating.AppExecutors
import blagodarie.rating.R
import blagodarie.rating.databinding.AddAbilityFragmentBinding
import blagodarie.rating.model.IAbility
import blagodarie.rating.model.entities.Ability
import blagodarie.rating.repository.AsyncServerRepository
import blagodarie.rating.server.BadAuthorizationTokenException
import blagodarie.rating.ui.AccountSource.getAccount
import java.util.*

class AddAbilityFragment : Fragment() {

    interface UserActionListener {
        fun onSaveClick()
    }

    companion object {
        private val TAG = AddAbilityFragment::class.java.simpleName
    }

    private lateinit var mBinding: AddAbilityFragmentBinding

    private val mAsyncRepository = AsyncServerRepository(AppExecutors.getInstance().networkIO(), AppExecutors.getInstance().mainThread())

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView")
        initBinding(inflater, container)
        return mBinding.root
    }

    private fun initBinding(
            inflater: LayoutInflater,
            container: ViewGroup?
    ) {
        Log.d(TAG, "initBinding")
        mBinding = AddAbilityFragmentBinding.inflate(inflater, container, false)
        mBinding.userActionListener = object : UserActionListener {
            override fun onSaveClick() {
                if (mBinding.etAbilityText.text.toString().isNotEmpty()) {
                    attemptToSaveAbility()
                } else {
                    mBinding.etAbilityText.error = getString(R.string.err_msg_required_to_fill)
                }
            }
        }
    }

    private fun attemptToSaveAbility() {
        getAccount(
                requireActivity(),
                true
        ) { account: Account? ->
            if (account != null) {
                AccountProvider.getAuthToken(
                        requireActivity(),
                        account
                ) { authToken: String? ->
                    if (authToken != null) {
                        val ability = Ability(UUID.randomUUID(), UUID.fromString(account.name), mBinding.etAbilityText.text.toString(), Date())
                        saveAbility(ability, authToken)
                    } else {
                        Toast.makeText(requireContext(), R.string.info_msg_need_log_in, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun saveAbility(
            ability: IAbility,
            authToken: String
    ) {
        mAsyncRepository.setAuthToken(authToken)
        mAsyncRepository.upsertAbility(
                ability,
                {
                    Toast.makeText(requireContext(), R.string.info_msg_ability_saved, Toast.LENGTH_LONG).show()
                    requireActivity().onBackPressed()
                }
        ) { throwable: Throwable ->
            if (throwable is BadAuthorizationTokenException) {
                AccountManager.get(requireContext()).invalidateAuthToken(getString(R.string.account_type), authToken)
                attemptToSaveAbility()
            } else {
                Log.e(TAG, Log.getStackTraceString(throwable))
                Toast.makeText(requireContext(), throwable.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}