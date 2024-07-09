package com.azimjonc.projects.data.remote.auth

import android.util.Log
import com.azimjonc.projects.domain.model.InvalidCredentialsException
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import io.reactivex.rxjava3.core.Completable
import java.util.concurrent.TimeUnit

class AuthFirebaseImpl : AuthFirebase {

    private val auth = FirebaseAuth.getInstance()
    lateinit var verificationId: String
    lateinit var token: ForceResendingToken
    override fun sendSmsCode(phone: String): Completable = Completable.create {

        val callbacks = object : OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(ceredential: PhoneAuthCredential) {}

            override fun onVerificationFailed(e: FirebaseException) {
                it.onError(e)
            }

            override fun onCodeSent(
                verificationId: String,
                token: ForceResendingToken
            ) {
                this@AuthFirebaseImpl.verificationId = verificationId
                this@AuthFirebaseImpl.token = token
                it.onComplete()
            }

        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    override fun verify(code: String): Completable = Completable.create {
        val credential = PhoneAuthProvider.getCredential(verificationId, code)

        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    it.onComplete()
                } else {
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        it.onError(InvalidCredentialsException())
                    }
                }
            }

    }
}