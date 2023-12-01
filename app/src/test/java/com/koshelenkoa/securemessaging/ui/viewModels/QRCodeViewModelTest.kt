package com.koshelenkoa.securemessaging.ui.viewModels

    import com.koshelenkoa.securemessaging.viewModels.QRCodeViewModel
    import org.junit.Before
    import org.junit.Test
    import org.mockito.MockitoAnnotations

class QRCodeViewModelTest {

        private lateinit var viewModel: QRCodeViewModel

        @Before
        fun setup() {
            MockitoAnnotations.initMocks(this)

        }


        @Test
        fun testCreateChat() {
            viewModel.updateChatId("chatId")
            viewModel.createChat()
        }
    }

