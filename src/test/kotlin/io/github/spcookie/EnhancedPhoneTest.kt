package io.github.spcookie

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class EnhancedPhoneTest {

    @Test
    fun testBasicPhoneGeneration() {
        val phoneNumber = MockRandom.phoneNumber()
        println("Generated phone number: $phoneNumber")
        assertNotNull(phoneNumber)
        assertTrue(phoneNumber.isNotEmpty())
    }

    @Test
    fun testPhoneTypeGeneration() {
        val mobilePhone = MockRandom.phoneNumber(MockRandom.PhoneType.MOBILE)
        val landlinePhone = MockRandom.phoneNumber(MockRandom.PhoneType.LANDLINE)
        val tollFreePhone = MockRandom.phoneNumber(MockRandom.PhoneType.TOLL_FREE)
        val premiumPhone = MockRandom.phoneNumber(MockRandom.PhoneType.PREMIUM)

        println("Mobile phone: $mobilePhone")
        println("Landline phone: $landlinePhone")
        println("Toll-free phone: $tollFreePhone")
        println("Premium phone: $premiumPhone")

        assertNotNull(mobilePhone)
        assertNotNull(landlinePhone)
        assertNotNull(tollFreePhone)
        assertNotNull(premiumPhone)

        assertTrue(mobilePhone.isNotEmpty())
        assertTrue(landlinePhone.isNotEmpty())
        assertTrue(tollFreePhone.isNotEmpty())
        assertTrue(premiumPhone.isNotEmpty())
    }

    @Test
    fun testAreaCodeGeneration() {
        val areaCode = MockRandom.areaCode()
        println("Generated area code: $areaCode")
        assertNotNull(areaCode)
        assertTrue(areaCode.isNotEmpty())
    }
}