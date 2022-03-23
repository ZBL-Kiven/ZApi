package com.zj.api.utils

import android.annotation.SuppressLint
import java.io.IOException
import java.io.InputStream
import java.security.*
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.*

@SuppressLint("CustomX509TrustManager")
object TrustAllCerts : X509TrustManager {

    @SuppressLint("TrustAllX509TrustManager")
    @Throws(CertificateException::class)
    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
    }

    @SuppressLint("TrustAllX509TrustManager")
    @Throws(CertificateException::class)
    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
    }

    override fun getAcceptedIssuers(): Array<X509Certificate?> {
        return arrayOfNulls(0)
    }

    fun createSSLSocketFactory(): SSLSocketFactory? {
        var factory: SSLSocketFactory? = null
        try {
            val context = SSLContext.getInstance("TLS")
            context.init(null, arrayOf<TrustManager>(TrustAllCerts), SecureRandom())
            factory = context.socketFactory
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return factory
    }
}

class TrustAllHostnameVerifier : HostnameVerifier {

    @SuppressLint("BadHostnameVerifier")
    override fun verify(hostname: String, session: SSLSession): Boolean {
        return true
    }
}


class SSLParams {
    var sSLSocketFactory: SSLSocketFactory? = null
    var trustManager: X509TrustManager? = null
}

fun getSslSocketFactory(certificates: Array<InputStream>, bksFile: InputStream?, password: String?): SSLParams {
    val sslParams = SSLParams()
    try {
        val trustManagers = prepareTrustManager(*certificates)
        val keyManagers = prepareKeyManager(bksFile, password)
        val sslContext = SSLContext.getInstance("TLS")
        val trustManager = if (trustManagers != null) {
            MyTrustManager(chooseTrustManager(trustManagers))
        } else {
            UnSafeTrustManager()
        }
        sslContext.init(keyManagers, arrayOf<TrustManager>(trustManager), null)
        sslParams.sSLSocketFactory = sslContext.socketFactory
        sslParams.trustManager = trustManager
        return sslParams
    } catch (e: NoSuchAlgorithmException) {
        throw AssertionError(e)
    } catch (e: KeyManagementException) {
        throw AssertionError(e)
    } catch (e: KeyStoreException) {
        throw AssertionError(e)
    }

}

private fun prepareTrustManager(vararg certificates: InputStream): Array<TrustManager>? {
    if (certificates.isNullOrEmpty()) return null
    try {
        val certificateFactory = CertificateFactory.getInstance("X.509")
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        keyStore.load(null)
        for ((index, certificate) in certificates.withIndex()) {
            val certificateAlias = index.toString()
            keyStore.setCertificateEntry(certificateAlias, certificateFactory.generateCertificate(certificate))
            try {
                certificate.close()
            } catch (e: IOException) {
            }
        }
        val trustManagerFactory: TrustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(keyStore)

        return trustManagerFactory.trustManagers
    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
    } catch (e: CertificateException) {
        e.printStackTrace()
    } catch (e: KeyStoreException) {
        e.printStackTrace()
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return null

}

private fun prepareKeyManager(bksFile: InputStream?, password: String?): Array<KeyManager>? {
    try {
        if (bksFile == null || password == null) return null

        val clientKeyStore = KeyStore.getInstance("BKS")
        clientKeyStore.load(bksFile, password.toCharArray())
        val keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
        keyManagerFactory.init(clientKeyStore, password.toCharArray())
        return keyManagerFactory.keyManagers

    } catch (e: KeyStoreException) {
        e.printStackTrace()
    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
    } catch (e: UnrecoverableKeyException) {
        e.printStackTrace()
    } catch (e: CertificateException) {
        e.printStackTrace()
    } catch (e: IOException) {
        e.printStackTrace()
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return null
}

private fun chooseTrustManager(trustManagers: Array<TrustManager>): X509TrustManager? {
    for (trustManager in trustManagers) {
        if (trustManager is X509TrustManager) {
            return trustManager
        }
    }
    return null
}

private class UnSafeTrustManager : X509TrustManager {
    @Throws(CertificateException::class)
    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
        LogUtils.d(chain.toString())
    }

    @Throws(CertificateException::class)
    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
        LogUtils.d(chain.toString())
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> {
        return arrayOf()
    }
}

private class MyTrustManager @Throws(NoSuchAlgorithmException::class, KeyStoreException::class)
constructor(private val localTrustManager: X509TrustManager?) : X509TrustManager {
    private val defaultTrustManager: X509TrustManager?

    init {
        val var4 = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        var4.init(null as KeyStore?)
        defaultTrustManager = chooseTrustManager(var4.trustManagers)
    }

    @Throws(CertificateException::class)
    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
        LogUtils.d(chain.toString())
    }

    @Throws(CertificateException::class)
    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
        try {
            defaultTrustManager?.checkServerTrusted(chain, authType)
        } catch (ce: CertificateException) {
            localTrustManager?.checkServerTrusted(chain, authType)
        }

    }

    override fun getAcceptedIssuers(): Array<X509Certificate> {
        return arrayOf()
    }
}
