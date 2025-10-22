// Local: app/src/main/java/br/com/gestahub/util/DateUtils.kt
package br.com.gestahub.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun getTodayString(): String {
    return LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) // Retorna "yyyy-MM-dd"
}