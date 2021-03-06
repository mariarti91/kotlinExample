package ru.skillbranch.kotlinexample

import androidx.annotation.VisibleForTesting
import java.math.BigInteger
import java.security.MessageDigest
import java.security.SecureRandom
import kotlin.IllegalArgumentException

class User private constructor(
    private val firstName: String,
    private val lastName: String?,
    email: String? = null,
    rawPhone: String? = null,
    meta: Map<String, Any>? = null
){
    val userInfo: String
    private val fullName: String
        get() = listOfNotNull(firstName, lastName).joinToString(" ").capitalize()
    private val initials: String
        get() = listOfNotNull(firstName, lastName).map { it.first().toUpperCase() }.joinToString(" ")
    private var phone: String? = null
        set(value){
            field = value?.replace("[^+\\d]".toRegex(), "")
        }
    private var _login: String? = null

    internal var login: String
        set(value){
            _login = value.toLowerCase()
        }
        get() = _login!!
    private val salt: String by lazy {
        ByteArray(16).also { SecureRandom().nextBytes(it) }.toString()
    }

    private lateinit var passwordHash: String

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    var accessCode: String? = null

    constructor(
        firstName: String,
        lastName: String?,
        email: String,
        password: String
    ):this(firstName=firstName, lastName = lastName, email = email, meta = mapOf("auth" to "password")){
        passwordHash = getHash(password)
    }

    constructor(
        firstName: String,
        lastName: String?,
        rawPhone: String
    ):this(firstName=firstName, lastName = lastName, rawPhone = rawPhone, meta = mapOf("auth" to "sms")){
        val code = generateAccessCode()
        passwordHash = getHash(code)
        accessCode = code
        sendAccessCodeToUser(rawPhone, code)
    }

    init{

        check(!firstName.isBlank()) {"First Name must be not blank"}
        check(email.isNullOrBlank() || rawPhone.isNullOrBlank()) {"Email or phone must be not blank"}

        phone = rawPhone

        if(!phone.isNullOrBlank() && phone!!.length != 12) throw IllegalArgumentException("Illegal phone number")

        login = email ?: phone!!

        userInfo = """
            firstName: $firstName
            lastName: $lastName
            login: $login
            fullName: $fullName
            initials: $initials
            email: $email
            phone: $phone
            meta: $meta
        """.trimIndent()
    }

    fun checkPassword(password: String) = getHash(password) == passwordHash

    fun changePassword(oldPass: String, newPass: String){
        if(checkPassword(oldPass)) passwordHash = getHash(newPass)
        else throw IllegalArgumentException("Wrong old password")
    }

    fun changeCode(){
        accessCode = generateAccessCode()
        passwordHash = getHash(accessCode!!)
    }

    private fun getHash(password: String): String = salt.plus(password).md5()

    private fun String.md5(): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(toByteArray())
        val hexString = BigInteger(1, digest).toString(16)
        return hexString.padStart(32, '0')
    }

    private fun generateAccessCode(): String {
        val possible = "ABCabc123" //slim version
        return StringBuilder().apply {
            repeat(6){
                (possible.indices).random().also { index ->
                    append(possible[index])
                }
            }
        }.toString()
    }

    private fun sendAccessCodeToUser(phone: String, code: String) {
        println("sending access code: $code on $phone")
    }

    companion object Factory{
        fun makeUser(
            fullName: String,
            email: String? = null,
            password: String? = null,
            phone: String? = null
        ):User{
            val (firstName, lastName) = fullName.fullNameToPair()

            return when{
                !phone.isNullOrBlank() -> User(firstName, lastName, rawPhone = phone)
                !email.isNullOrBlank() && !password.isNullOrBlank() -> User(firstName, lastName, email = email, password = password)
                else -> throw IllegalArgumentException("Email or phone must be not null or blank")
            }
        }

        private fun String.fullNameToPair(): Pair<String, String?> {
            return this.split(" ")
                .filter { it.isNotBlank() }
                .run {
                    when(size){
                        1 -> first() to null
                        2 -> first() to last()
                        else -> throw IllegalArgumentException("Fullname must contain only first name " +
                                "and last name, current split result ${this@fullNameToPair}")
                    }
                }
        }

    }

}
