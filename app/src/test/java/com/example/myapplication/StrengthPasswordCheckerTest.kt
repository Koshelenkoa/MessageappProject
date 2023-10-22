package com.example.myapplication

    import com.example.myapplication.util.PasswordStrengthChecker
    import org.hamcrest.MatcherAssert.assertThat
    import org.junit.Assert.assertFalse
    import org.junit.Assert.assertTrue
    import org.junit.Before
    import org.junit.Test
    import java.lang.reflect.Method

    class PasswordStrengthCheckerTest {
        private val passwordStrengthChecker = PasswordStrengthChecker()

        @Before
        private fun makeMethodAccessible(methodName: String): Method {
            val method = PasswordStrengthChecker::class.java.getDeclaredMethod(methodName, CharSequence::class.java)
            method.isAccessible = true
            return method
        }

        @Test
        fun testHasLength() {
            val result = passwordStrengthChecker.hasLength("password123")
            assertTrue(result)
        }

        @Test
        fun testHasDigit() {
            val result = passwordStrengthChecker.hasDigit("password123")
            assertTrue(result)
        }

        @Test
        fun testHasUppercase() {
            val result = passwordStrengthChecker.hasUppercase("Password")
            assertTrue(result)
        }

        @Test
        fun testHasLowercase() {
            val result = passwordStrengthChecker.hasLowercase("password")
            assertTrue(result)
        }

        @Test
        fun testHasSymbol() {
            val result = passwordStrengthChecker.hasSymbol("!@#")
            assertTrue(result)
        }
    }
