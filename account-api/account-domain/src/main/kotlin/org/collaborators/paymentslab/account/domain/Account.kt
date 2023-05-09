package org.collaborators.paymentslab.account.domain

import jakarta.persistence.*
import org.collaborator.paymentlab.common.KeyGenerator
import org.collaborator.paymentlab.common.Role
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "ACCOUNTS")
class Account protected constructor(
    var accountKey: String? = null,
    var email: String,
    var password: String,
    var username: String,
    var emailCheckToken: String? = null,
    var emailCheckTokenGeneratedAt: LocalDateTime? = null,
    var emailVerified: Boolean = false,
    var joinedAt: LocalDateTime? = null,
    @UpdateTimestamp
    val lastModifiedAt: LocalDateTime?,
    val withdraw: Boolean? = false,
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    var roles: MutableSet<Role>
) {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
    private val PREFIX = "act_"
    private constructor(email: String, password: String, username: String) : this(
        KeyGenerator.generate("act_"), email, password, username, null,
        null, false, null, null, false, hashSetOf(Role.USER)) {
        this.email = email
        this.password = password
        this.username = username
    }

    companion object {
        fun register(email: String, encodedPassword: String, username: String): Account {
            val account = Account(email, encodedPassword, username)
            account.generateEmailCheckToken()
            return account
        }
    }

    private fun generateEmailCheckToken() {
        this.emailCheckToken = UUID.randomUUID().toString()
        this.emailCheckTokenGeneratedAt = LocalDateTime.now()
    }

    fun completeRegister() {
        this.emailVerified = true
        this.joinedAt = LocalDateTime.now()
    }

    fun isValidToken(token: String): Boolean {
        return this.emailCheckToken.equals(token)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Account

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}