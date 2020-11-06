package blagodarie.rating.ui.abilities

import android.accounts.Account
import android.accounts.AccountManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import blagodarie.rating.AppExecutors
import blagodarie.rating.R
import blagodarie.rating.databinding.AddAbilityFragmentBinding
import blagodarie.rating.model.IAbility
import blagodarie.rating.model.entities.Ability
import blagodarie.rating.repository.AsyncServerRepository
import blagodarie.rating.server.BadAuthorizationTokenException
import blagodarie.rating.ui.AccountProvider
import blagodarie.rating.ui.AccountSource
import blagodarie.rating.ui.abilities.AddAbilityFragment.UserActionListener
import blagodarie.rating.ui.hideSoftKeyboard
import blagodarie.rating.ui.showSoftKeyboard
import java.util.*

class AddAbilityFragment : Fragment() {

    fun interface UserActionListener {
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.d(TAG, "onActivityCreated")
        super.onActivityCreated(savedInstanceState)
        setupBinding()
        showSoftKeyboard(requireContext(), mBinding.etAbilityText)
    }

    private fun initBinding(
            inflater: LayoutInflater,
            container: ViewGroup?
    ) {
        Log.d(TAG, "initBinding")
        mBinding = AddAbilityFragmentBinding.inflate(inflater, container, false)
    }

    private fun setupBinding() {
        Log.d(TAG, "setupBinding")
        mBinding.userActionListener = UserActionListener { checkAndSaveAbility() }
        mBinding.etAbilityText.setOnEditorActionListener { _, actionId, _ ->
            var handled = false
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                checkAndSaveAbility()
                handled = true
            }
            handled
        }
    }

    private fun checkAndSaveAbility() {
        val wishText = mBinding.etAbilityText.text.toString().trim()
        if (wishText.isNotBlank()) {
            hideSoftKeyboard(requireActivity())
            saveAbility(wishText)
        } else {
            mBinding.etAbilityText.error = getString(R.string.err_msg_required_to_fill)
        }
    }

    private fun saveAbility(
            abilityText: String
    ) {
        AccountSource.getAccount(
                requireActivity(),
                true
        ) { account: Account? ->
            if (account != null) {
                val ability = Ability(UUID.randomUUID(), UUID.fromString(account.name), abilityText, Date())
                saveAbility(ability, account)
            }
        }
    }

    private fun saveAbility(
            ability: IAbility,
            account: Account
    ) {
        AccountProvider.getAuthToken(
                requireActivity(),
                account
        ) { authToken: String? ->
            if (authToken != null) {
                saveAbility(ability, authToken)
            } else {
                Toast.makeText(requireContext(), R.string.info_msg_need_log_in, Toast.LENGTH_LONG).show()
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
                    Toast.makeText(requireContext(), R.string.info_msg_wish_saved, Toast.LENGTH_LONG).show()
                    requireActivity().onBackPressed()
                }
        ) { throwable: Throwable ->
            if (throwable is BadAuthorizationTokenException) {
                AccountManager.get(requireContext()).invalidateAuthToken(getString(R.string.account_type), authToken)
                saveAbility(ability.text)
            } else {
                Log.e(TAG, Log.getStackTraceString(throwable))
                Toast.makeText(requireContext(), throwable.message, Toast.LENGTH_LONG).show()
            }
        }
    }

}