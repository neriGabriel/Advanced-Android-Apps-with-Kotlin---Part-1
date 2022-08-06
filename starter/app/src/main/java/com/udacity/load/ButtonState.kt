package com.udacity.load

/**
 * Sealed class responsible to track the possible object states for the custom button
 *
 * [AVAILABLE OPTIONS]: [CLICKED, LOADING, COMPLETED]
 * */
sealed class ButtonState {
    object Clicked : ButtonState()
    object Loading : ButtonState()
    object Completed : ButtonState()
}