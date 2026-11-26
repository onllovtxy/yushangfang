package com.xixikitchen.jetpack.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xixikitchen.jetpack.data.Anniversary
import com.xixikitchen.jetpack.data.AnniversarySaveRequest
import com.xixikitchen.jetpack.data.Announcement
import com.xixikitchen.jetpack.data.CheckInCreateRequest
import com.xixikitchen.jetpack.data.CheckInUpdateRequest
import com.xixikitchen.jetpack.data.CoupleNote
import com.xixikitchen.jetpack.data.CoupleNoteSaveRequest
import com.xixikitchen.jetpack.data.KitchenRepository
import com.xixikitchen.jetpack.data.OrderCheckIn
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
    val diaryEntries: List<OrderCheckIn> = emptyList(),
    val diaryTotal: Long = 0,
    val diaryPage: Int = 1,
    val diaryLoaded: Boolean = false,
    val loadingMoreDiary: Boolean = false,
    val orderCheckIns: Map<Long, List<OrderCheckIn>> = emptyMap(),
    val currentUser: User? = null
) {
    val loading: Boolean get() = activeRequests > 0
    val isAdmin: Boolean get() = currentUser?.role == "admin" || currentUser?.role == "super_admin"
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
        val pageData = repo.checkIns(token(), page = 1, pageSize = 20)
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
                    repo.checkIns(token(), page = nextPage, pageSize = 20)
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

    fun loadOrderCheckIns(orderId: Long) = runAction {
        val list = repo.orderCheckIns(token(), orderId)
        _state.update { current ->
            current.copy(orderCheckIns = current.orderCheckIns + (orderId to list))
        }
    }

    fun addCheckIn(orderId: Long, imageUrl: String, note: String) = runAction {
        val created = repo.addCheckIn(token(), CheckInCreateRequest(orderId, imageUrl, note))
        _state.update { current ->
            val updatedOrderList = (current.orderCheckIns[orderId].orEmpty() + created)
                .distinctBy { it.id }
            val updatedDiary = if (current.diaryLoaded) {
                (listOf(created) + current.diaryEntries).distinctBy { it.id }
            } else {
                current.diaryEntries
            }
            current.copy(
                orderCheckIns = current.orderCheckIns + (orderId to updatedOrderList),
                diaryEntries = updatedDiary,
                diaryTotal = if (current.diaryLoaded) current.diaryTotal + 1 else current.diaryTotal
            )
        }
        notify("成品打卡成功")
    }

    fun updateCheckIn(checkIn: OrderCheckIn, imageUrl: String, note: String) = runAction {
        val updated = repo.updateCheckIn(token(), checkIn.id, CheckInUpdateRequest(imageUrl = imageUrl, note = note))
        _state.update { current ->
            val orderList = current.orderCheckIns[checkIn.orderId]?.map {
                if (it.id == updated.id) updated else it
            }
            val updatedMap = if (orderList != null) {
                current.orderCheckIns + (checkIn.orderId to orderList)
            } else {
                current.orderCheckIns
            }
            current.copy(
                orderCheckIns = updatedMap,
                diaryEntries = current.diaryEntries.map { if (it.id == updated.id) updated else it }
            )
        }
        notify("打卡已更新")
    }

    fun deleteCheckIn(checkIn: OrderCheckIn) = runAction {
        repo.deleteCheckIn(token(), checkIn.id)
        _state.update { current ->
            val orderList = current.orderCheckIns[checkIn.orderId]?.filterNot { it.id == checkIn.id }
            val updatedMap = if (orderList != null) {
                current.orderCheckIns + (checkIn.orderId to orderList)
            } else {
                current.orderCheckIns
            }
            val hadInDiary = current.diaryEntries.any { it.id == checkIn.id }
            current.copy(
                orderCheckIns = updatedMap,
                diaryEntries = current.diaryEntries.filterNot { it.id == checkIn.id },
                diaryTotal = if (hadInDiary) (current.diaryTotal - 1).coerceAtLeast(0) else current.diaryTotal
            )
        }
        notify("打卡已删除")
    }

    fun uploadImage(
        bytes: ByteArray,
        fileName: String,
        onSuccess: (String) -> Unit,
        onFailure: () -> Unit
    ) = runAction {
        try {
            val url = repo.uploadImageBytes(token(), bytes, fileName)
            onSuccess(url)
        } catch (e: Exception) {
            onFailure()
            throw e
        }
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
