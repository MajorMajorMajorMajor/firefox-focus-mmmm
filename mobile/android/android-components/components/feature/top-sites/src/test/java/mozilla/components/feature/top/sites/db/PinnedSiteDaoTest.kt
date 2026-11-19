/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package mozilla.components.feature.top.sites.db

import androidx.room.Room
import kotlinx.coroutines.test.runTest
import mozilla.components.support.test.robolectric.testContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

private const val DATABASE_NAME = "pinned-sites-reorder-test"

@RunWith(RobolectricTestRunner::class)
class PinnedSiteDaoTest {
    private lateinit var database: TopSiteDatabase

    @Before
    fun setup() {
        testContext.deleteDatabase(DATABASE_NAME)
        database = openDatabase()
    }

    @After
    fun teardown() {
        database.close()
        testContext.deleteDatabase(DATABASE_NAME)
    }

    @Test
    fun `WHEN pinned sites are reordered THEN preserve their metadata and persist the new order`() = runTest {
        val dao = database.pinnedSiteDao()
        val sites =
            listOf(
                PinnedSiteEntity(title = "Mozilla", url = "https://mozilla.org", createdAt = 10),
                PinnedSiteEntity(title = "Firefox", url = "https://firefox.com", isDefault = true, createdAt = 20),
            )
        dao.insertAllPinnedSites(sites)
        val reordered = sites.reversed()
        val expected = reordered.map { it.copy(id = null) }

        dao.reorderPinnedSites(reordered)

        assertEquals(expected, dao.getPinnedSites().map { it.copy(id = null) })
        assertEquals(2, dao.getPinnedSitesCount())
        database.close()
        database = openDatabase()
        assertEquals(expected, database.pinnedSiteDao().getPinnedSites().map { it.copy(id = null) })
    }

    @Test
    fun `WHEN reordering an empty list THEN remove the previous pinned sites`() = runTest {
        val dao = database.pinnedSiteDao()
        dao.insertPinnedSite(PinnedSiteEntity(title = "Mozilla", url = "https://mozilla.org"))

        dao.reorderPinnedSites(emptyList())

        assertTrue(dao.getPinnedSites().isEmpty())
        assertEquals(0, dao.getPinnedSitesCount())
    }

    private fun openDatabase(): TopSiteDatabase =
        Room.databaseBuilder(testContext, TopSiteDatabase::class.java, DATABASE_NAME).build()
}
