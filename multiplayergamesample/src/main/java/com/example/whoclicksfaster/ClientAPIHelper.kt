package com.example.whoclicksfaster

import android.util.Log
import com.google.gson.Gson
import network.celer.mobile.*
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.abi.datatypes.generated.Uint8
import java.util.*

object ClientAPIHelper {
    private var client: Client? = null

    lateinit var joinAddr: String

    private var opponentIndex = -1
    private var myIndex = -1
    private var myAddress: String? = null
    private var opponentAddress: String? = null

    var sessionId: String? = null

    val cApp = CApp()
    var callback = object : CAppCallback {
        override fun onStatusChanged(status: Long) {
            Log.e("whoclicksfaster", "createNewCAppSession onStatusChanged is: $status")
        }

        override fun onReceiveState(state: ByteArray?): Boolean {
            Log.e("whoclicksfaster", "createNewCAppSession onReceiveState : $state")
            state?.let {
                //                            currentCAppStateLive.postValue(state)
            }
            return true
        }
    }


    fun initCelerClient(keyStoreString: String, passwordStr: String, profileStr: String) {
        // Init Celer Client

        var keyStoreJson = Gson().fromJson(keyStoreString, KeyStoreData::class.java)
        joinAddr = "0x" + keyStoreJson.address



        try {
            client = Mobile.newClient(keyStoreString, passwordStr, profileStr)
        } catch (e: Exception) {
            Log.d("whoclicksfaster ", e.localizedMessage)
        }
    }

    fun joinCeler(clientSideDepositAmount: String, serverSideDepositAmount: String) {
        // Join Celer Network
        try {
            client?.joinCeler("0x0", clientSideDepositAmount, serverSideDepositAmount)
            Log.d("whoclicksfaster ", "Balance: ${client?.getBalance(1)?.available}")
        } catch (e: Exception) {
            Log.d("whoclicksfaster ", "Join Celer Network Error: ${e.localizedMessage}")

        }

    }


    fun initSession(gresp: GroupResp?) {
        gresp?.let {
            it?.g.let {

                val playerAddresses = it.users.split(",")

                if (playerAddresses.size == 2) {

                    if (playerAddresses[0].toLowerCase() == joinAddr!!.toLowerCase()) {
                        myAddress = playerAddresses[0]
                        opponentAddress = playerAddresses[1]
                        myIndex = 1
                        opponentIndex = 2
                    } else {
                        myAddress = playerAddresses[1]
                        opponentAddress = playerAddresses[0]
                        opponentIndex = 1
                        myIndex = 2
                    }



                    cApp.callback = callback

                    val constructor = FunctionEncoder.encodeConstructor(Arrays.asList(
                            Address(myAddress),
                            Address(opponentAddress),
                            Uint256(3),
                            Uint256(3),
                            Uint8(5),
                            Uint8(3)))


                    sessionId = client?.newCAppSession(cApp, constructor, gresp.round.id)
                    Log.e("whoclicksfaster", "sessionId : $sessionId")

//                    val delay = if (myIndex == 1) 2000L else 0L
//                    var stake = it.stake.split(".")[0]
//
//                    handler.postDelayed({
//                        sendPayWithConditions(stake, opponentIndex)
//                    }, 0)


                }


            }

        }


    }



    fun sendState(state: ByteArray) {
        client?.sendCAppState(sessionId, opponentAddress, state)

    }
}