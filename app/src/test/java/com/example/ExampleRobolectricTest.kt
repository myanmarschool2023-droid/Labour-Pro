package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("LabourPro", appName)
  }

  @Test
  fun `worker id natural sort ordering`() {
    val list = listOf("W-10", "W-2", "W-1", "W-20", "W-3")
    val sorted = list.sortedWith { a, b -> com.example.data.model.compareWorkerIds(a, b) }
    assertEquals(listOf("W-1", "W-2", "W-3", "W-10", "W-20"), sorted)
  }
}
