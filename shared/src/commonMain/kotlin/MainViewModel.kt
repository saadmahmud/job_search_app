import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
//import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import model.BirdImage
import model.User

data class AppUiState(
    val images: List<BirdImage> = emptyList(),
    val selectedCategory: String? = null
) {
    val categories = images.map { it.category }.toSet()
    val selectedImages = images.filter { it.category == selectedCategory }
}

class AppViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<AppUiState>(AppUiState())
    val uiState = _uiState.asStateFlow()

    private val httpClient = HttpClient {
//        install(ContentNegotiation) {
//            json()
//        }
    }

    val client = HttpClient()

    init {
        updateImages()
    }

    override fun onCleared() {
        httpClient.close()
    }

    fun selectCategory(category: String) {
        _uiState.update {
            it.copy(selectedCategory = category)
        }
    }

    fun updateImages() {
        viewModelScope.launch {
            val images = getImages()
            _uiState.update {
                it.copy(images = images)
            }
        }
    }

    private suspend fun getImages(): List<BirdImage> {
        val images = httpClient
            .get("https://sebastianaigner.github.io/demo-image-api/pictures.json")
            .bodyAsText()

        return listOf(
            BirdImage(
                "name 1",
                "cate 1",
                "https://sebi.io/demo-image-api/pigeon/vladislav-nikonov-yVYaUSwkTOs-unsplash.jpg"
            ),
            BirdImage(
                "name 2",
                "cate 2",
                "https://sebi.io/demo-image-api/pigeon/vladislav-nikonov-yVYaUSwkTOs-unsplash.jpg"
            )
        )
    }

    suspend fun getUsers(): List<User> {
        val firebaseFirestore = Firebase.firestore
        try {
            val userResponse =
                firebaseFirestore.collection("USERS").get()
            return userResponse.documents.map {
                it.data()
            }
        } catch (e: Exception) {
            throw e
        }
    }
}