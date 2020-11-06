package blagodarie.rating.ui

import android.accounts.*
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import blagodarie.rating.R
import java.io.IOException

object AccountSource {

    fun interface GetAccountListener {
        fun onGetAccount(account: Account?)
    }

    private val TAG: String = AccountSource::class.java.name
    private const val LAST_ACCOUNT_PREFERENCE = "blagodarie.rating.ui.AccountSource.pref"
    private const val LAST_ACCOUNT_NAME = "currentAccountName"

    fun getAccount(
            activity: Activity,
            requireCreateAccount: Boolean = true,
            getAccountListener: GetAccountListener
    ) {
        Log.d(TAG, "getAccount")
        val sharedPreferences = activity.getSharedPreferences(LAST_ACCOUNT_PREFERENCE, Context.MODE_PRIVATE)
        val accountManager = AccountManager.get(activity)
        val accounts = accountManager.getAccountsByType(activity.getString(R.string.account_type))
        when {
            accounts.isEmpty() -> {
                sharedPreferences.edit().remove(LAST_ACCOUNT_NAME).apply()
                if (requireCreateAccount) {
                    attemptToCreateAccount(activity, getAccountListener)
                } else {
                    getAccountListener.onGetAccount(null)
                }
            }
            accounts.size == 1 -> {
                sharedPreferences.edit().putString(LAST_ACCOUNT_NAME, accounts[0].name).apply()
                getAccountListener.onGetAccount(accounts[0])
            }
            else -> {
                val lastAccount = getLastAccount(activity)
                var currentAccount: Account? = null
                if (lastAccount != null) {
                    for (account in accounts) {
                        if (account == lastAccount) {
                            currentAccount = account
                        }
                    }
                }
                if (currentAccount == null) {
                    showAccountPicker(activity, accounts, getAccountListener)
                } else {
                    getAccountListener.onGetAccount(currentAccount)
                }
            }
        }
    }

    private fun showAccountPicker(
            context: Context,
            accounts: Array<Account>,
            getAccountListener: GetAccountListener
    ) {
        Log.d(TAG, "showAccountPicker accounts=" + accounts.contentToString())
        val names = arrayOfNulls<String>(accounts.size)
        for (i in accounts.indices) {
            names[i] = accounts[i].name
        }
        AlertDialog.Builder(context).setTitle(R.string.rqst_choose_account).setCancelable(false).setAdapter(
                ArrayAdapter(
                        context,
                        android.R.layout.simple_list_item_1, names)
        ) { _: DialogInterface?, which: Int ->
            val sharedPreferences = context.getSharedPreferences(LAST_ACCOUNT_PREFERENCE, Context.MODE_PRIVATE)
            sharedPreferences.edit().putString(LAST_ACCOUNT_NAME, accounts[which].name).apply()
            getAccountListener.onGetAccount(accounts[which])
        }.setCancelable(false).create().show()
    }

    private fun getLastAccount(
            context: Context
    ): Account? {
        Log.d(TAG, "getLastAccount")
        val sharedPreferences = context.getSharedPreferences(LAST_ACCOUNT_PREFERENCE, Context.MODE_PRIVATE)
        var account: Account? = null
        if (sharedPreferences.contains(LAST_ACCOUNT_NAME)) {
            val lastAccountName = sharedPreferences.getString(LAST_ACCOUNT_NAME, null)
            if (lastAccountName != null) {
                account = Account(lastAccountName, context.getString(R.string.account_type))
            }
        }
        return account
    }

    private fun attemptToCreateAccount(
            activity: Activity,
            getAccountListener: GetAccountListener
    ) {
        AccountManager.get(activity).addAccount(
                activity.getString(R.string.account_type),
                activity.getString(R.string.token_type),
                null,
                null,
                activity,
                { accountManagerFuture: AccountManagerFuture<Bundle> ->
                    try {
                        val bundle = accountManagerFuture.result
                        val account = Account(
                                bundle.getString(AccountManager.KEY_ACCOUNT_NAME),
                                bundle.getString(AccountManager.KEY_ACCOUNT_TYPE)
                        )
                        getAccountListener.onGetAccount(account)
                    } catch (e: OperationCanceledException) {
                        Log.e(TAG, Log.getStackTraceString(e))
                        Toast.makeText(activity, activity.getString(R.string.err_msg_account_not_created), Toast.LENGTH_LONG).show()
                        getAccountListener.onGetAccount(null)
                    } catch (e: AuthenticatorException) {
                        Log.e(TAG, Log.getStackTraceString(e))
                        Toast.makeText(activity, activity.getString(R.string.err_msg_authentication_error), Toast.LENGTH_LONG).show()
                        getAccountListener.onGetAccount(null)
                    } catch (e: IOException) {
                        Log.e(TAG, Log.getStackTraceString(e))
                        Toast.makeText(activity, activity.getString(R.string.err_msg_connection_error), Toast.LENGTH_LONG).show()
                        getAccountListener.onGetAccount(null)
                    }
                },
                null
        )
    }

}