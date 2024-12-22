import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "language_settings")

class LanguagePreferenceManager(private val context: Context) {

    companion object {
        private val LANGUAGE_KEY = stringPreferencesKey("language")
    }

    suspend fun saveLanguage(languageCode: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = languageCode
        }
    }

    val getLanguage: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[LANGUAGE_KEY] ?: "tr"
        }

    suspend fun getSavedLanguage(): String {
        return context.dataStore.data
            .map { preferences -> preferences[LANGUAGE_KEY] ?: "tr" }
            .first()
    }
}
