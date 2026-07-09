package com.nithra.nithraresume.ui.generatedresumes


import android.content.Context
import androidx.lifecycle.ViewModel
import com.nithra.nithraresume.utils.SrDir
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import javax.inject.Inject

@HiltViewModel
class GeneratedResumesViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _files = MutableStateFlow<List<File>>(emptyList())
    val files: StateFlow<List<File>> = _files.asStateFlow()

    init { scan() }

    fun scan() {
        val dir = File(context.getExternalFilesDir(null), SrDir.GENERATED_RESUME)
        _files.value = dir.listFiles { f -> f.extension.equals("pdf", ignoreCase = true) }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()
    }

    fun delete(file: File) {
        runCatching { file.delete() }
        scan()
    }

    fun rename(file: File, newName: String): Boolean {
        val trimmed = newName.trim().removeSuffix(".pdf")
        if (trimmed.isBlank()) return false
        val dest = File(file.parentFile, "$trimmed.pdf")
        if (dest.exists()) return false
        return runCatching { file.renameTo(dest) }.getOrDefault(false).also { if (it) scan() }
    }
}
