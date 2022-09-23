package com.github.kitakkun.kottotto.extensions

import java.text.MessageFormat
import java.util.*

fun ResourceBundle.getString(key: String, vararg args: Any?): String =
    MessageFormat.format(getString(key), *args.map { it.toString() }.toTypedArray())
