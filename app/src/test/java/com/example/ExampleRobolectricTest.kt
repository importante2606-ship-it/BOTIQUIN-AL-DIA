package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ProductEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.time.ZoneId

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Botiquín al Día", appName)
  }

  @Test
  fun `test 30 days expiry alert logic`() {
    val today = LocalDate.now(ZoneId.systemDefault())

    // Vence en 15 días: debe alertar en <=30 días
    val expiring15Days = today.plusDays(15).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val product15 = ProductEntity(
      name = "Alcohol 70%",
      quantity = 2,
      expiryDateMillis = expiring15Days
    )
    assertTrue("Debe alertar que vence en 30 días", product15.isExpiringIn30Days())
    assertFalse("No debe figurar como vencido", product15.isExpired())

    // Vencido hace 3 días
    val expired3Days = today.minusDays(3).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val productExpired = ProductEntity(
      name = "Ibuprofeno vencido",
      quantity = 1,
      expiryDateMillis = expired3Days
    )
    assertTrue("Debe figurar como vencido", productExpired.isExpired())
    assertFalse("No debe figurar como vigente por vencer", productExpired.isExpiringIn30Days())
    assertTrue("Debe figurar en lista de reposición", productExpired.needsRestock())

    // Vigente a 6 meses
    val safe6Months = today.plusMonths(6).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val productSafe = ProductEntity(
      name = "Paracetamol seguro",
      quantity = 10,
      expiryDateMillis = safe6Months
    )
    assertFalse("No debe estar vencido", productSafe.isExpired())
    assertFalse("No debe alertar en 30 días", productSafe.isExpiringIn30Days())
    assertFalse("No requiere reposición con 10 unidades", productSafe.needsRestock())
  }
}
