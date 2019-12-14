package ru.skillbranch.kotlinexample

import androidx.annotation.VisibleForTesting

object UserHolder {
    private val map = mutableMapOf<String, User>()

    fun registerUser(
        fullName: String,
        email: String,
        password: String
    ):User{
        return User.makeUser(fullName, email = email, password = password)
            .also {
                user ->
                require(!map.contains(user.login)) { "A user with this email already exists" }
                map[user.login] = user
            }
    }

    fun registerUserByPhone(
        fullName: String,
        rawPhone: String
    ):User{
        return User.makeUser(fullName, phone = rawPhone)
            .also {
                user ->
                require(!map.contains(user.login)) { "A user with this phone already exists" }
                map[user.login] = user
            }
    }

    fun loginUser(login:String, password: String):String?{

        val lg = castLoginIfPhone(login)
        println("login $lg")
        return map[lg]?.run {
            if(checkPassword(password)) this.userInfo
            else null
        }
    }

    fun requestAccessCode(login: String){
        val lg = castLoginIfPhone(login)
        map[lg]?.run {
            changeCode()
        }
    }

    fun importUsers(list: List<String>): List<User> = list.map{ User.loadFromCsv(it).also {
        user ->
        check(!map.contains(user.login)){"User already exists"}
        map[user.login] = user
    }}

    private fun castLoginIfPhone(login: String): String {
        val lg = login.replace("[^+\\d]".toRegex(), "")
        return if("\\+\\d{11}".toRegex().matches(lg)) lg else login
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun clearHolder(){
        map.clear()
    }

}