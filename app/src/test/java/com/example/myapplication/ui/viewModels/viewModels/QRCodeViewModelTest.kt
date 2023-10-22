package com.example.myapplication.ui.viewModels.viewModels

    import com.example.myapplication.ui.viewModels.QRCodeViewModel
    import com.example.myapplication.util.AuthManager
    import okhttp3.ResponseBody
    import org.junit.Before
    import org.junit.Rule
    import org.junit.Test
    import org.mockito.Mock
    import org.mockito.Mockito
    import org.mockito.MockitoAnnotations
    import retrofit2.Call
    import retrofit2.Callback
    import retrofit2.Response
    import java.security.KeyStore

    class QRCodeViewModelTest {

        private lateinit var viewModel: QRCodeViewModel

        @Before
        fun setup() {
            MockitoAnnotations.initMocks(this)
            viewModel = QRCodeViewModel()

        }


        @Test
        fun testCreateChat() {
            viewModel.updateChatId("chatId")
            viewModel.createChat()
        }
    }

