package com.teachmeski.app.util

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.teachmeski.app.ui.component.ActiveRole
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class RolePreferencesClearTest {
    @get:Rule
    val tmp = TemporaryFolder()

    private fun newStore(name: String): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(produceFile = { File(tmp.root, "$name.preferences_pb") })

    @Test
    fun clearLastActiveRole_removesOnlyOwnEntry() = runTest {
        val store = newStore("clear-test")
        val prefs = RolePreferences(store)
        prefs.setLastActiveRole("user-a", ActiveRole.Student)
        prefs.setLastActiveRole("user-b", ActiveRole.Instructor)

        prefs.clearLastActiveRole("user-a")

        assertNull(prefs.getLastActiveRole("user-a"))
        assertEquals(ActiveRole.Instructor, prefs.getLastActiveRole("user-b"))
    }
}
