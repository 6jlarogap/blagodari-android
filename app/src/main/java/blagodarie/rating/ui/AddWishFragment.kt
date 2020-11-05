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
import blagodarie.rating.databinding.AddWishFragmentBinding
import blagodarie.rating.model.IWish
import blagodarie.rating.model.entities.Wish
import blagodarie.rating.repository.AsyncServerRepository
import blagodarie.rating.server.BadAuthorizationTokenException
import java.util.*

class AddWishFragment : Fragment() {

    interface UserActionListener {
        fun onSaveClick()
    }

    companion object {
        private val TAG = AddWishFragment::class.java.simpleName
    }

    private lateinit var mBinding: AddWishFragmentBinding

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
        mBinding = AddWishFragmentBinding.inflate(inflater, container, false)
        mBinding.userActionListener = object : UserActionListener {
            override fun onSaveClick() {
                if (mBinding.etWishText.text.toString().isNotEmpty()) {
                    attemptToSaveWish()
                } else {
                    mBinding.etWishText.error = getString(R.string.err_msg_required_to_fill)
                }
            }
        }
    }

    private fun attemptToSaveWish() {
        AccountSource.getAccount(
                requireActivity(),
                true
        ) { account: Account? ->
            if (account != null) {
                AccountProvider.getAuthToken(
                        requireActivity(),
                        account
                ) { authToken: String? ->
                    if (authToken != null) {
                        val wish = Wish(UUID.randomUUID(), UUID.fromString(account.name), mBinding.etWishText.text.toString(), Date())
                        saveWish(wish, authToken)
                    } else {
                        Toast.makeText(requireContext(), R.string.info_msg_need_log_in, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun saveWish(
            wish: IWish,
            authToken: String
    ) {
        mAsyncRepository.setAuthToken(authToken)
        mAsyncRepository.upsertWish(
                wish,
                {
                    Toast.makeText(requireContext(), R.string.info_msg_wish_saved, Toast.LENGTH_LONG).show()
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