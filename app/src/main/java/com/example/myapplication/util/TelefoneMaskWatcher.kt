package com.example.myapplication.util

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

/**
 * Máscara de telefone celular brasileiro: (00) 00000-0000
 * Basta adicionar como TextWatcher ao EditText de telefone.
 */
class TelefoneMaskWatcher(private val editText: EditText) : TextWatcher {

    private var isUpdating = false
    private val mask = "(##) #####-####"

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable?) {
        if (isUpdating) return
        isUpdating = true

        val digits = s.toString().replace(Regex("[^\\d]"), "")
        val sb     = StringBuilder()
        var i      = 0

        for (char in mask) {
            if (i >= digits.length) break
            sb.append(if (char == '#') digits[i++] else char)
        }

        editText.setText(sb.toString())
        editText.setSelection(sb.length)
        isUpdating = false
    }
}
