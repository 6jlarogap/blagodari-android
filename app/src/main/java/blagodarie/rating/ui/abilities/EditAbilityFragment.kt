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
import blagodarie.rating.databinding.EditAbilityFragmentBinding
import blagodarie.rating.model.IAbility
import blagodarie.rating.repository.AsyncServerRepository
import blagodarie.rating.server.BadAuthorizationTokenException
import blagodarie.rating.ui.AccountProvider
import blagodarie.rating.ui.AccountSource
import blagodarie.rating.ui.hideSoftKeyboard
import blagodarie.rating.ui.showSoftKeyboard
import blagodarie.rating.ui.abilities.EditAbilityFragment.UserActionListener

class EditAbilityFragment : Fragment() {

    fun interface UserActionListener {
        fun onSaveClick()
    }

    companion object {
        private val TAG = EditAbilityFragment::class.java.simpleName
    }

    private lateinit var mBinding: EditAbilityFragmentBinding

    private lateinit var mAbility: IAbility

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

    override fun onViewCreated(
            view: View,
            savedInstanceState: Bundle?
    ) {
        Log.d(TAG, "onViewCreated")
        super.onViewCreated(view, savedInstanceState)
        val args = EditAbilityFragmentArgs.fromBundle(requireArguments())
        mAbility = args.ability
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
        mBinding = EditAbilityFragmentBinding.inflate(inflater, container, false)
    }

    private fun setupBinding() {
        Log.d(TAG, "setupBinding")
        mBinding.userActionListener = UserActionListener { checkAndSaveAbility() }
        mBinding.etAbilityText.append(mAbility.text)
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
        val abilityText = mBinding.etAbilityText.text.toString().trim()
        if (abilityText.isNotBlank()) {
            hideSoftKeyboard(requireActivity())
            saveAbility(abilityText)
        } else {
            mBinding.etAbilityText.error = getString(R.string.err_msg_required_to_fill)
        }
    }

    private fun saveAbility(
            abilityText: String
    ) {
        AccountSource.requireAccount(
                requireActivity(),
        ) { account: Account? ->
            if (account != null) {
                mAbility.text = abilityText
                saveAbility(mAbility, account)
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
                    Toast.makeText(requireContext(), R.string.info_msg_ability_saved, Toast.LENGTH_LONG).show()
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