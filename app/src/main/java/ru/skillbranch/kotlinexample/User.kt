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

    private var salt: String? = null

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

    constructor(
        firstName: String,
        lastName: String?,
        email: String?,
        presettedSalt: String,
        hash: String,
        phone: String?
    ):this(firstName=firstName, lastName = lastName, email = email, rawPhone = phone, meta = mapOf("src" to "csv")){
        salt = presettedSalt
        passwordHash = hash
    }

    init{

        check(!firstName.isBlank()) {"First Name must be not blank"}
        check(email.isNullOrBlank() || rawPhone.isNullOrBlank()) {"Email or phone must be not blank"}

        phone = rawPhone

        if(!phone.isNullOrBlank() && phone!!.length != 12) throw IllegalArgumentException("Enter a valid phone number starting with a + and containing 11 digits")

        login = email ?: phone!!

        if(salt.isNullOrBlank()) salt = ByteArray(16).also { SecureRandom().nextBytes(it) }.toString()

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

        fun loadFromCsv(csv: String):User{
            val valueList = csv.split(";").map { if(it.isNullOrBlank()) null else it.trim() }
            require(valueList.size == 5) { "Illegal csv string format" }
            require(!valueList[0].isNullOrEmpty()) { "Illegal csv string format" }
            require(!valueList[2].isNullOrBlank() && valueList[2]!!.contains(":")) { "Illegal csv string format" }

            val(firstName, lastName) = valueList[0]!!.fullNameToPair()
            val email = valueList[1]
            val (salt, hash) = valueList[2]!!.split(":")
            val phone = valueList[3]

            return User(firstName, lastName, email, salt, hash, phone)
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
