package blagodarie.rating.ui.contacts

import android.util.Log
import androidx.paging.DataSource
import androidx.paging.PositionalDataSource
import blagodarie.rating.model.IKeyPair
import blagodarie.rating.model.IProfile
import blagodarie.rating.server.GetUsersRequest
import blagodarie.rating.server.ServerApiClient

class ProfilesDataSource(
        private val textFilter: String?,
        private val keysFilter: List<IKeyPair>?
) : PositionalDataSource<IProfile>() {

    companion object {
        val TAG: String = ProfilesDataSource::class.java.name
    }

    override fun loadInitial(
            params: LoadInitialParams,
            callback: LoadInitialCallback<IProfile>
    ) {
        Log.d(TAG, "loadInitial from=" + params.requestedStartPosition + ", pageSize=" + params.pageSize)
        val client = ServerApiClient()
        val request = GetUsersRequest(textFilter, keysFilter, params.requestedStartPosition, params.pageSize)
        try {
            val response = client.execute(request)
            callback.onResult(response.users, 0)
        } catch (e: Exception) {
            Log.e(TAG, Log.getStackTraceString(e))
        }
    }

    override fun loadRange(
            params: LoadRangeParams,
            callback: LoadRangeCallback<IProfile>
    ) {
        Log.d(TAG, "loadInitial from=" + params.startPosition + ", pageSize=" + params.loadSize)
        val client = ServerApiClient()
        val request = GetUsersRequest(textFilter, keysFilter, params.startPosition, params.loadSize)
        try {
            val response = client.execute(request)
            callback.onResult(response.users)
        } catch (e: Exception) {
            Log.e(TAG, Log.getStackTraceString(e))
        }
    }

    internal class ProfilesDataSourceFactory(
            private val textFilter: String?,
            private val keysFilter: List<IKeyPair>?
    ) : Factory<Int, IProfile>() {
        override fun create(): DataSource<Int, IProfile> {
            return ProfilesDataSource(textFilter, keysFilter)
        }
    }
}