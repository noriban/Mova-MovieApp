package com.prmto.mova_movieapp.util

import com.prmto.mova_movieapp.presentation.util.UiText

sealed class Resource<T>(val data: T? = null, val uiText: UiText? = null) {
    class Success<T>(data: T) : Resource<T>(data = data)
    class Error<T>(uiText: UiText, data: T? = null) : Resource<T>(data = data, uiText = uiText)
}