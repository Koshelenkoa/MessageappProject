package com.koshelenkoa.securemessaging.ui.viewModels

import com.koshelenkoa.securemessaging.viewModels.AuthenticationViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule

class AuthenticationViewModelTest {

    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    @Mock
    private lateinit var mockFirebaseAuth: FirebaseAuth

    @Mock
    private lateinit var mockFirebaseUser: FirebaseUser

    private lateinit var authenticationViewModel: AuthenticationViewModel

    @Before
    fun setUp() {
        Mockito.`when`(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
        authenticationViewModel = AuthenticationViewModel()
    }

    @Test
    fun `updatePassword updates password`() {
        val newPassword = "newPassword"
        authenticationViewModel.updatePassword(newPassword)
        assert(authenticationViewModel.password == newPassword)
    }

    @Test
    fun `updateLogin updates login`() {
        val newLogin = "newLogin"
        authenticationViewModel.updateLogin(newLogin)
        assert(authenticationViewModel.login == newLogin)
    }

    @Test
    fun `updateUser updates user`() {
        val newUser: FirebaseUser? = Mockito.mock(FirebaseUser::class.java)
        authenticationViewModel.updateUser(newUser)
        assert(authenticationViewModel.user == newUser)
    }

    @Test
    fun `declineLogin sets loginAutomatically to false`() {
        authenticationViewModel.declineLogin()
        assert(!authenticationViewModel.loginAutomatically)
    }
}