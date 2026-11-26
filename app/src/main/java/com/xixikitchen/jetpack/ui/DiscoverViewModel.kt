package com.xixikitchen.jetpack.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xixikitchen.jetpack.data.Anniversary
import com.xixikitchen.jetpack.data.AnniversarySaveRequest
import com.xixikitchen.jetpack.data.Announcement
import com.xixikitchen.jetpack.data.CoupleNote
import com.xixikitchen.jetpack.data.CoupleNoteSaveRequest
import com.xixikitchen.jetpack.data.FoodDiary
import com.xixikitchen.jetpack.data.FoodDiaryCommentCreateRequest
import com.xixikitchen.jetpack.data.FoodDiarySaveRequest
import com.xixikitchen.jetpack.data.KitchenRepository
import com.xixikitchen.jetpack.data.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DiscoverUiState(
    val activeRequests: Int = 0,
    val message: String? = null,
    val errorDialogMessage: String? = null,
    val announcements: List<Announcement> = emptyList(),
    val announcementsLoaded: Boolean = false,
    val notes: List<CoupleNote> = emptyList(),
    val notesLoaded: Boolean = false,
    val anniversaries: List<Anniversary> = emptyList(),
    val diaryEntries: List<FoodDiary> = emptyList(),
    val diaryTotal: Long = 0,
    val diaryPage: Int = 1,
    val diaryLoaded: Boolean = false,
    val loadingMoreDiary: Boolean = false,
    val currentUser: User? = null
) {
    val loading: Boolean get() = activeRequests > 0
    val isAdmin: Boolean get() = currentUser?.role == "admin" || currentUser?.role == "super_admin"
    val isSuperAdmin: Boolean get() = currentUser?.role == "super_admin"
}

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val repo: KitchenRepository
) : ViewModel() {
    private val _state = MutableStateFlow(DiscoverUiState())
    val state: StateFlow<DiscoverUiState> = _state.asStateFlow()

    @Volatile
    private var currentToken: String = ""

    init {
        viewModelScope.launch {
            repo.session.collect { session ->
                currentToken = session.token
                if (!session.loggedIn) {
                    _state.value = DiscoverUiState()
                } else {
                    _state.update { it.copy(currentUser = session.user) }
                }
            }
        }
        viewModelScope.launch {
            repo.sessionExpired.collect {
                currentToken = ""
                _state.value = DiscoverUiState()
            }
        }
        viewModelScope.launch {
            repo.spaceRefreshEvents.collect {
                val hadNotes = _state.value.notesLoaded
                val hadDiary = _state.value.diaryLoaded
                _state.update {
                    it.copy(
                        notes = emptyList(),
                        notesLoaded = false,
                        anniversaries = emptyList(),
                        diaryEntries = emptyList(),
                        diaryTotal = 0,
                        diaryPage = 1,
                        diaryLoaded = false
                    )
                }
                if (currentToken.isNotBlank()) {
                    if (hadNotes) refreshNotesBoard()
                    if (hadDiary) refreshDiary()
                }
            }
        }
    }

    fun consumeMessage() = _state.update { it.copy(message = null) }
    fun consumeErrorDialog() = _state.update { it.copy(errorDialogMessage = null) }

    fun refreshAnnouncements() = runAction {
        val list = repo.announcements(token())
        _state.update { it.copy(announcements = list, announcementsLoaded = true) }
    }

    fun refreshNotesBoard() = runAction {
        refreshNotesBoardSync()
    }

    fun saveNote(id: Long?, content: String, pinned: Boolean) = runAction {
        val req = CoupleNoteSaveRequest(content = content, pinned = pinned)
        if (id == null || id == 0L) {
            repo.addNote(token(), req)
        } else {
            repo.updateNote(token(), id, req)
        }
        refreshNotesBoardSync()
        notify("小纸条已保存")
    }

    fun togglePinNote(note: CoupleNote) = runAction {
        repo.pinNote(token(), note.id, !note.pinned)
        refreshNotesBoardSync()
        notify(if (!note.pinned) "已置顶" else "已取消置顶")
    }

    fun deleteNote(id: Long) = runAction {
        repo.deleteNote(token(), id)
        refreshNotesBoardSync()
        notify("小纸条已删除")
    }

    fun saveAnniversary(id: Long?, req: AnniversarySaveRequest) = runAction {
        if (id == null || id == 0L) {
            repo.addAnniversary(token(), req)
        } else {
            repo.updateAnniversary(token(), id, req)
        }
        val list = repo.anniversaries(token())
        _state.update { it.copy(anniversaries = list, notesLoaded = true) }
        notify("纪念日已保存")
    }

    fun togglePinAnniversary(item: Anniversary) = runAction {
        repo.pinAnniversary(token(), item.id, !item.pinned)
        val list = repo.anniversaries(token())
        _state.update { it.copy(anniversaries = list) }
        notify(if (!item.pinned) "已置顶" else "已取消置顶")
    }

    fun deleteAnniversary(id: Long) = runAction {
        repo.deleteAnniversary(token(), id)
        val list = repo.anniversaries(token())
        _state.update { it.copy(anniversaries = list) }
        notify("纪念日已删除")
    }

    fun refreshDiary() = runAction {
        val pageData = repo.diaries(token(), page = 1, pageSize = 20)
        _state.update {
            it.copy(
                diaryEntries = pageData.list,
                diaryTotal = pageData.total,
                diaryPage = 1,
                diaryLoaded = true
            )
        }
    }

    fun loadMoreDiary() {
        val snapshot = _state.value
        if (snapshot.loadingMoreDiary || snapshot.diaryEntries.size >= snapshot.diaryTotal) return
        viewModelScope.launch {
            _state.update { it.copy(loadingMoreDiary = true) }
            try {
                val nextPage = snapshot.diaryPage + 1
                runCatching {
                    repo.diaries(token(), page = nextPage, pageSize = 20)
                }.onSuccess { pageData ->
                    _state.update { current ->
                        val merged = (current.diaryEntries + pageData.list).distinctBy { it.id }
                        current.copy(
                            diaryEntries = merged,
                            diaryTotal = pageData.total,
                            diaryPage = nextPage,
                            diaryLoaded = true
                        )
                    }
                }.onFailure { e ->
                    notifyError(resolveErrorMessage(e))
                }
            } finally {
                _state.update { it.copy(loadingMoreDiary = false) }
            }
        }
    }

    fun createDiary(req: FoodDiarySaveRequest, onSuccess: () -> Unit = {}) = runAction {
        val created = repo.createDiary(token(), req)
        _state.update { current ->
            val updated = (listOf(created) + current.diaryEntries).distinctBy { it.id }
            current.copy(
                diaryEntries = updated,
                diaryTotal = current.diaryTotal + 1,
                diaryLoaded = true
            )
        }
        onSuccess()
        notify("票根已生成")
    }

    fun updateDiary(id: Long, req: FoodDiarySaveRequest, onSuccess: () -> Unit = {}) = runAction {
        val updated = repo.updateDiary(token(), id, req)
        _state.update { current ->
            current.copy(
                diaryEntries = current.diaryEntries.map { if (it.id == updated.id) updated else it }
            )
        }
        onSuccess()
        notify("票根已更新")
    }

    fun deleteDiary(diary: FoodDiary) = runAction {
        repo.deleteDiary(token(), diary.id)
        _state.update { current ->
            val existed = current.diaryEntries.any { it.id == diary.id }
            current.copy(
                diaryEntries = current.diaryEntries.filterNot { it.id == diary.id },
                diaryTotal = if (existed) (current.diaryTotal - 1).coerceAtLeast(0) else current.diaryTotal
            )
        }
        notify("票根已删除")
    }

    fun addComment(diaryId: Long, content: String, replyToUserId: Long? = null) = runAction {
        val added = repo.addDiaryComment(
            token(),
            diaryId,
            FoodDiaryCommentCreateRequest(content = content, replyToUserId = replyToUserId)
        )
        _state.update { current ->
            current.copy(
                diaryEntries = current.diaryEntries.map { entry ->
                    if (entry.id == diaryId) entry.copy(comments = entry.comments + added) else entry
                }
            )
        }
        notify("评论已发送")
    }

    fun deleteComment(diaryId: Long, commentId: Long) = runAction {
        repo.deleteDiaryComment(token(), commentId)
        _state.update { current ->
            current.copy(
                diaryEntries = current.diaryEntries.map { entry ->
                    if (entry.id == diaryId) {
                        entry.copy(comments = entry.comments.filterNot { it.id == commentId })
                    } else {
                        entry
                    }
                }
            )
        }
        notify("评论已删除")
    }

    suspend fun uploadImageBlocking(bytes: ByteArray, fileName: String): String {
        return repo.uploadImageBytes(token(), bytes, fileName)
    }

    fun reportError(e: Throwable) {
        notifyError(resolveErrorMessage(e))
    }

    private suspend fun refreshNotesBoardSync() {
        coroutineScope {
            val notesDeferred = async { repo.notes(token()) }
            val annivDeferred = async { repo.anniversaries(token()) }
            val notes = notesDeferred.await()
            val anniversaries = annivDeferred.await()
            _state.update { it.copy(notes = notes, anniversaries = anniversaries, notesLoaded = true) }
        }
    }

    private fun runAction(block: suspend () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(activeRequests = it.activeRequests + 1, message = null, errorDialogMessage = null) }
            try {
                runCatching { block() }
                    .onFailure { e -> notifyError(resolveErrorMessage(e)) }
            } finally {
                _state.update { it.copy(activeRequests = (it.activeRequests - 1).coerceAtLeast(0)) }
            }
        }
    }

    private fun token(): String = currentToken

    private fun notify(message: String) {
        _state.update { it.copy(message = message) }
    }

    private fun notifyError(message: String) {
        _state.update { it.copy(errorDialogMessage = message) }
    }
}
