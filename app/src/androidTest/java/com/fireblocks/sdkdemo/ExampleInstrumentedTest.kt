package com.fireblocks.sdkdemo

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fireblocks.sdkdemo.bl.core.extensions.getWIFFromPrivateKey

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @Test
    fun testBitcoinWif(){
        val privateKeyHex = "6a7df640f0263351fb91eb2d041f1ba10c0f84a93cf587a90845f7625331510c"
        val wif = privateKeyHex.getWIFFromPrivateKey(isMainNet = false)
        assertEquals(wif, "cR9i2oM7iYfcRifi6MhaTaFEgPgrRamvxbuLmCvXvRgw3t24nRNn")
    }

    @Test
    fun testGetWif(){
        val privateKeyHex = "6a7df640f0263351fb91eb2d041f1ba10c0f84a93cf587a90845f7625331510c"
        val wif =  FireblocksManager.getInstance().getWif(privateKeyHex)
        assertEquals(wif, "p2wpkh:cR9i2oM7iYfcRifi6MhaTaFEgPgrRamvxbuLmCvXvRgw3t24nRNn")
    }
}