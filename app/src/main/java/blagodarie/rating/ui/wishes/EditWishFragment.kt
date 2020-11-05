package blagodarie.rating.ui.wishes

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
import blagodarie.rating.databinding.EditWishFragmentBinding
import blagodarie.rating.model.IWish
import blagodarie.rating.repository.AsyncServerRepository
import blagodarie.rating.server.BadAuthorizationTokenException
import blagodarie.rating.ui.AccountProvider
import blagodarie.rating.ui.AccountSource.getAccount
import blagodarie.rating.ui.wishes.EditWishFragment.UserActionListener
import java.util.*

class EditWishFragment : Fragment() {

    fun interface UserActionListener {
        fun onSaveClick()
    }

    companion object {
        private val TAG = EditWishFragment::class.java.simpleName
    }

    private lateinit var mBinding: EditWishFragmentBinding

    private lateinit var mWish: IWish

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
        val args = EditWishFragmentArgs.fromBundle(requireArguments())
        mWish = args.wish
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupBinding()
    }

    private fun initBinding(
            inflater: LayoutInflater,
            container: ViewGroup?
    ) {
        Log.d(TAG, "initBinding")
        mBinding = EditWishFragmentBinding.inflate(inflater, container, false)
    }

    private fun setupBinding() {
        Log.d(TAG, "setupBinding")
        mBinding.wish = mWish
        mBinding.userActionListener = UserActionListener {
            if (mWish.text.isNotEmpty()) {
                mWish.lastEdit = Date()
                attemptToSaveWish()
            } else {
                mBinding.etAbilityText.setError(getString(R.string.err_msg_required_to_fill))
            }
        }
    }

    private fun attemptToSaveWish() {
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
                        saveWish(authToken)
                    } else {
                        Toast.makeText(requireContext(), R.string.info_msg_need_log_in, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun saveWish(
            authToken: String
    ) {
        mAsyncRepository.setAuthToken(authToken)
        mAsyncRepository.upsertWish(
                mWish,
                {
                    Toast.makeText(requireContext(), R.string.info_msg_ability_saved, Toast.LENGTH_LONG).show()
                    requireActivity().onBackPressed()
                }
        ) { throwable: Throwable ->
            if (throwable is BadAuthorizationTokenException) {
                AccountManager.get(requireContext()).invalidateAuthToken(getString(R.string.account_type), authToken)
                attemptToSaveWish()
            } else {
                Log.e(TAG, Log.getStackTraceString(throwable))
                Toast.makeText(requireContext(), throwable.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}