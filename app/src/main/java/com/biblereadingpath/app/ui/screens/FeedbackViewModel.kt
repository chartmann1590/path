package com.biblereadingpath.app.ui.screens

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biblereadingpath.app.BuildConfig
import com.biblereadingpath.app.data.feedback.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FeedbackViewModel(context: Context) : ViewModel() {
    private val repo = BugReportRepo(context)

    val configValid: Boolean = GithubClient.hasValidConfig
    val configMessage: String = if (!configValid) GithubClient.missingConfigMessage else ""

    private val _bugReports = MutableStateFlow<List<BugReport>>(emptyList())
    val bugReports: StateFlow<List<BugReport>> = _bugReports.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri.asStateFlow()

    private val _includeDiagnostics = MutableStateFlow(true)
    val includeDiagnostics: StateFlow<Boolean> = _includeDiagnostics.asStateFlow()

    private val _numAttachments = MutableStateFlow(0)
    val numAttachments: StateFlow<Int> = _numAttachments.asStateFlow()

    private val _selectedReport = MutableStateFlow<BugReport?>(null)
    val selectedReport: StateFlow<BugReport?> = _selectedReport.asStateFlow()

    private val _issueDetail = MutableStateFlow<GithubIssue?>(null)
    val issueDetail: StateFlow<GithubIssue?> = _issueDetail.asStateFlow()

    private val _comments = MutableStateFlow<List<GithubComment>>(emptyList())
    val comments: StateFlow<List<GithubComment>> = _comments.asStateFlow()

    private val _isLoadingComments = MutableStateFlow(false)
    val isLoadingComments: StateFlow<Boolean> = _isLoadingComments.asStateFlow()

    private val _commentText = MutableStateFlow("")
    val commentText: StateFlow<String> = _commentText.asStateFlow()

    private val _commentImageUri = MutableStateFlow<Uri?>(null)
    val commentImageUri: StateFlow<Uri?> = _commentImageUri.asStateFlow()

    private val _isPostingComment = MutableStateFlow(false)
    val isPostingComment: StateFlow<Boolean> = _isPostingComment.asStateFlow()

    init {
        loadBugReports()
    }

    private fun loadBugReports() {
        viewModelScope.launch {
            repo.bugReports.collect { reports ->
                _bugReports.value = reports
            }
        }
    }

    fun setSelectedImageUri(uri: Uri?) {
        _selectedImageUri.value = uri
        _numAttachments.value = if (uri != null) 1 else 0
    }

    fun setIncludeDiagnostics(include: Boolean) {
        _includeDiagnostics.value = include
    }

    fun submitReport(
        context: Context,
        title: String,
        description: String,
        name: String,
        email: String
    ) {
        if (!configValid) {
            _statusMessage.value = "Cannot submit: $configMessage"
            return
        }
        if (title.isBlank()) {
            _statusMessage.value = "Title is required"
            return
        }
        if (description.isBlank()) {
            _statusMessage.value = "Description is required"
            return
        }

        viewModelScope.launch {
            _isSubmitting.value = true
            _statusMessage.value = null
            try {
                var body = buildString {
                    appendLine("## Description")
                    appendLine()
                    appendLine(description)
                    appendLine()
                    appendLine("## Contact Info")
                    appendLine()
                    appendLine("- Name: ${if (name.isBlank()) "Not provided" else name}")
                    appendLine("- Email: ${if (email.isBlank()) "Not provided" else email}")
                }

                // Upload image first if attached
                val imageUri = _selectedImageUri.value
                if (imageUri != null) {
                    val base64 = ImageUploadHelper.uriToBase64(context, imageUri)
                    if (base64 != null) {
                        val filename = ImageUploadHelper.generateAssetFilename(null)
                        val uploadRequest = UploadAssetRequest(
                            message = "Upload screenshot for feedback",
                            content = base64
                        )
                        val uploadResponse = GithubClient.api.uploadAsset(
                            GithubClient.owner, GithubClient.repo,
                            BuildConfig.FEEDBACK_ASSETS_DIR, filename,
                            uploadRequest
                        )
                        if (uploadResponse.isSuccessful) {
                            val downloadUrl = uploadResponse.body()?.content?.downloadUrl ?: ""
                            body += "\n\n## Attachment\n\n![Screenshot]($downloadUrl)"
                        } else {
                            val errorMsg = when (uploadResponse.code()) {
                                422 -> "Upload failed: file may already exist or is invalid."
                                else -> "Upload failed: HTTP ${uploadResponse.code()}"
                            }
                            _statusMessage.value = errorMsg
                            _isSubmitting.value = false
                            return@launch
                        }
                    } else {
                        _statusMessage.value = "Failed to convert image."
                        _isSubmitting.value = false
                        return@launch
                    }
                }

                // Append diagnostics if included
                if (_includeDiagnostics.value) {
                    body += "\n\n${DiagnosticsHelper.collectDiagnostics(context)}"
                }

                val issueRequest = CreateIssueRequest(
                    title = "[Feedback] $title",
                    body = body
                )

                val response = GithubClient.api.createIssue(
                    GithubClient.owner, GithubClient.repo, issueRequest
                )

                if (response.isSuccessful) {
                    val issue = response.body()!!
                    val report = BugReport(
                        number = issue.number,
                        title = issue.title,
                        status = issue.state,
                        createdAt = issue.createdAt,
                        htmlUrl = issue.htmlUrl
                    )
                    repo.saveBugReport(report)
                    _statusMessage.value = "Report #${issue.number} submitted successfully!"
                    _selectedImageUri.value = null
                    _numAttachments.value = 0
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    _statusMessage.value = "Failed to create issue: ${response.code()}\n$errorBody"
                }
            } catch (e: Exception) {
                _statusMessage.value = "Error: ${e.message}"
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun selectReport(report: BugReport) {
        _selectedReport.value = report
        _issueDetail.value = null
        _comments.value = emptyList()
        _commentText.value = ""
        _commentImageUri.value = null
        fetchIssueDetails(report.number)
    }

    fun clearSelectedReport() {
        _selectedReport.value = null
        _issueDetail.value = null
        _comments.value = emptyList()
    }

    private fun fetchIssueDetails(number: Int) {
        if (!configValid) return
        viewModelScope.launch {
            _isLoadingComments.value = true
            try {
                val issueResponse = GithubClient.api.getIssue(
                    GithubClient.owner, GithubClient.repo, number
                )
                if (issueResponse.isSuccessful) {
                    val issue = issueResponse.body()!!
                    _issueDetail.value = issue
                    // Update local status if changed
                    val currentReport = _bugReports.value.find { it.number == number }
                    if (currentReport != null && currentReport.status != issue.state) {
                        repo.saveBugReport(currentReport.copy(status = issue.state))
                    }
                }
                val commentsResponse = GithubClient.api.getComments(
                    GithubClient.owner, GithubClient.repo, number
                )
                if (commentsResponse.isSuccessful) {
                    _comments.value = commentsResponse.body() ?: emptyList()
                }
            } catch (e: Exception) {
                _statusMessage.value = "Failed to fetch issue: ${e.message}"
            } finally {
                _isLoadingComments.value = false
            }
        }
    }

    fun setCommentText(text: String) {
        _commentText.value = text
    }

    fun setCommentImageUri(uri: Uri?) {
        _commentImageUri.value = uri
    }

    fun postComment(context: Context) {
        val report = _selectedReport.value ?: return
        val text = _commentText.value.trim()
        if (text.isBlank()) {
            _statusMessage.value = "Comment cannot be empty"
            return
        }
        if (!configValid) return

        viewModelScope.launch {
            _isPostingComment.value = true
            try {
                var body = buildString {
                    appendLine("## Reply")
                    appendLine()
                    appendLine(text)
                }

                val imageUri = _commentImageUri.value
                if (imageUri != null) {
                    val base64 = ImageUploadHelper.uriToBase64(context, imageUri)
                    if (base64 != null) {
                        val filename = ImageUploadHelper.generateAssetFilename(report.number)
                        val uploadRequest = UploadAssetRequest(
                            message = "Upload screenshot for comment on #${report.number}",
                            content = base64
                        )
                        val uploadResponse = GithubClient.api.uploadAsset(
                            GithubClient.owner, GithubClient.repo,
                            BuildConfig.FEEDBACK_ASSETS_DIR, filename,
                            uploadRequest
                        )
                        if (uploadResponse.isSuccessful) {
                            val downloadUrl = uploadResponse.body()?.content?.downloadUrl ?: ""
                            body += "\n\n## Attachment\n\n![Screenshot]($downloadUrl)"
                        } else {
                            _statusMessage.value = "Image upload failed: HTTP ${uploadResponse.code()}"
                            _isPostingComment.value = false
                            return@launch
                        }
                    } else {
                        _statusMessage.value = "Failed to convert image."
                        _isPostingComment.value = false
                        return@launch
                    }
                }

                val response = GithubClient.api.postComment(
                    GithubClient.owner, GithubClient.repo,
                    report.number,
                    PostCommentRequest(body)
                )

                if (response.isSuccessful) {
                    _commentText.value = ""
                    _commentImageUri.value = null
                    _statusMessage.value = "Reply posted!"
                    fetchIssueDetails(report.number)
                } else {
                    _statusMessage.value = "Failed to post comment: ${response.code()}"
                }
            } catch (e: Exception) {
                _statusMessage.value = "Error posting comment: ${e.message}"
            } finally {
                _isPostingComment.value = false
            }
        }
    }
}
