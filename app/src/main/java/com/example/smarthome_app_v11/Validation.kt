package com.example.smarthome_app_v11

object Validation {
    fun email(value: String): Boolean = Regex("^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)+$").matches(value.trim())
    fun device(name: String, ip: String): String? = when {
        name.trim().isEmpty() -> "Podaj nazwę urządzenia"
        name.trim().length > 80 -> "Nazwa może mieć najwyżej 80 znaków"
        !ipv4(ip.trim()) -> "Podaj poprawny adres IPv4, np. 192.168.1.100"
        else -> null
    }
    fun ipv4(value: String): Boolean {
        val parts = value.split('.')
        return parts.size == 4 && parts.all {
            it.isNotEmpty() && it.length <= 3 && it.all { c -> c in '0'..'9' } &&
                (it.length == 1 || it.first() != '0') && (it.toIntOrNull() ?: -1) in 0..255
        }
    }
}
