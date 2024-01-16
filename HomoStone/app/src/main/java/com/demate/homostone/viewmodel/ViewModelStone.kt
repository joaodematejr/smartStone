package com.demate.homostone.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import br.com.stone.posandroid.providers.PosPrintProvider
import br.com.stone.posandroid.providers.PosPrintReceiptProvider
import br.com.stone.posandroid.providers.PosTransactionProvider
import com.demate.homostone.utils.Base64
import stone.application.StoneStart
import stone.application.enums.Action
import stone.application.enums.InstalmentTransactionEnum
import stone.application.enums.ReceiptType
import stone.application.enums.TransactionStatusEnum
import stone.application.enums.TypeOfTransactionEnum
import stone.application.interfaces.StoneActionCallback
import stone.application.interfaces.StoneCallbackInterface
import stone.database.transaction.TransactionDAO
import stone.database.transaction.TransactionObject
import stone.providers.ActiveApplicationProvider
import stone.providers.CancellationProvider
import stone.utils.Stone
import stone.utils.keys.StoneKeyType


class ViewModelStone : ViewModel() {

    private val transactionObject = TransactionObject()
    var transactionProvider: PosTransactionProvider? = null
    val TAG = "ViewModelStone"

    fun handleInit(context: Context) {
        try {
           val users = StoneStart.init(context, mapOf(
                StoneKeyType.QRCODE_AUTHORIZATION to "4f27f1c9-1740-40fc-bec0-6d4ee7d590bc",
                StoneKeyType.QRCODE_PROVIDERID to "Bearer 5dd05ae6-6f3a-4735-9fa5-410856439e58"
            ))
            Stone.setAppName("HomologDemo")
            if (!users.isNullOrEmpty()) {
                val activeApplicationProvider = ActiveApplicationProvider(context)
                activeApplicationProvider.connectionCallback = object : StoneCallbackInterface {
                    override fun onSuccess() {
                        Log.d(TAG, "onSuccess: ")
                    }

                    override fun onError() {
                        Log.d(TAG, "onError: ")
                    }
                }
                activeApplicationProvider.activate("569130284")
            }
        } catch (e: Exception) {
            showToast(context, e.toString())
        }
    }

    fun handlePrint(context: Context) {
        try {
            val base64Image = Base64().getBase64Image(context)
            val posPrintProvider = PosPrintProvider(context)
            posPrintProvider.addBase64Image(base64Image.toString())
            posPrintProvider.execute()
        } catch (e: Exception) {
            logErrorAndToast(context, "handlePrint", e)
        }
    }

    fun handleInformation(context: Context) {
        try {
            val serial = Stone.getPosAndroidDevice().getPosAndroidSerialNumber()
            val manufacture = Stone.getPosAndroidDevice().getPosAndroidManufacturer()
            showToast(context, "Serial: $serial\nManufacture: $manufacture")
        } catch (e: Exception) {
            logErrorAndToast(context, "handleInformation", e)
        }
    }


    fun handlePayment(context: Context) {
        try {
            setupTransaction()
            transactionProvider = PosTransactionProvider(context, transactionObject, Stone.getUserModel(0))
            transactionProvider!!.setConnectionCallback(object : StoneActionCallback {
                override fun onSuccess() {
                    when (val status = transactionProvider!!.transactionStatus) {
                        TransactionStatusEnum.APPROVED -> {
                            Log.d(TAG, "SUCCESS: $status")
                            //printer CLIENT
                            handlePrintReceiptClient(context, transactionObject)
                            //printer MERCHANT
                            //handlePrintReceiptMerchant(context, transaction)
                        }
                        TransactionStatusEnum.DECLINED -> {
                            val message = transactionProvider!!.messageFromAuthorize
                            Log.e(TAG, "DECLINED: $message")
                        }
                        TransactionStatusEnum.REJECTED -> {
                            val message = transactionProvider!!.messageFromAuthorize
                            Log.e(TAG, "REJECTED: $message")
                        }
                        else -> {
                            Log.d(TAG, "handlePayment: $status")
                        }
                    }
                }
                override fun onError() {
                    Log.e(TAG, "ERROR - ${transactionProvider!!.listOfErrors}")
                }
                override fun onStatusChanged(action: Action?) {
                    Log.d(TAG, "onStatusChanged - Exibir em Tela ${action?.name!! }")
                }
            })
            transactionProvider!!.execute()
        } catch (e: Exception) {
            Log.e(TAG, "handlePayment: ", e)
            showToast(context, e.toString())
        }
    }


    fun handlePrintReceiptClient(context: Context, transaction: TransactionObject) {
        val posPrintReceiptProvider = PosPrintReceiptProvider(context, transaction, ReceiptType.CLIENT);
        posPrintReceiptProvider.connectionCallback = object : StoneActionCallback {
            override fun onSuccess() {
                Log.d(TAG, "SUCCESS - PRINTER")
                transactionProvider = null
            }

            override fun onError() {
                Log.d(TAG, "onError")
            }

            override fun onStatusChanged(p0: Action?) {
                Log.d(TAG, "onStatusChanged")
            }

        }
        posPrintReceiptProvider.execute()
    }

    fun handlePrintReceiptMerchant(context: Context, transaction: TransactionObject) {
        val posPrintReceiptProvider = PosPrintReceiptProvider(context, transaction, ReceiptType.MERCHANT);
        posPrintReceiptProvider.connectionCallback = object : StoneActionCallback {
            override fun onSuccess() {
                Log.d("SUCCESS - PRINTER", transaction.toString())
                transaction.apply {  }
            }
            override fun onError() {
                Log.e("ERROR_PRINT", transaction.toString())
            }
            override fun onStatusChanged(p0: Action?) {
                Log.e(TAG, "STATUS: $p0")
            }
        }
        posPrintReceiptProvider.execute()
    }

    private fun setupTransaction() {
        transactionObject.apply {
            amount = (1000..9000).random().toString()
            instalmentTransaction = InstalmentTransactionEnum.EIGHTEEN_INSTALMENT_WITH_INTEREST
            typeOfTransaction = TypeOfTransactionEnum.CREDIT
            isCapture = true
            initiatorTransactionKey = null
        }
    }

    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun logErrorAndToast(context: Context, methodName: String, exception: Exception) {
        Log.e("ViewModelStone", "$methodName: ", exception)
        showToast(context, exception.toString())
    }
    //https://sdkandroid.stone.com.br/docs/o-que-e-a-sdk-android

    fun getListTransaction(context: Context) {
        val transactionDAO = TransactionDAO(context)
        val transactionObjects = transactionDAO.getAllTransactionsOrderByIdDesc()
        transactionObjects.forEach {
            Log.d(TAG, "getTransaction: $it")
        }
    }

    fun getIdTransaction(context: Context) {
        val transactionDAO = TransactionDAO(context)
        val transactionId = transactionDAO.getLastTransactionId()
        Log.d(TAG, "getIdTransaction: $transactionId")
        val transactionObject = transactionDAO.findTransactionWithId(transactionId)
        Log.d(TAG, "transactionObject: $transactionObject")
    }

    fun cancelPayment(context: Context) {
        val provider = CancellationProvider(context, transactionObject)
        provider.connectionCallback = object : StoneCallbackInterface {
            override fun onSuccess() {
                //Transacao cancelada com sucesso
                //Método que retorna o código da transação cancelada
                Log.d(TAG, "onSuccess: " + provider.messageFromAuthorize)
            }

            override fun onError() {
                //Ocorreu um erro no cancelamento da transacao
                //Método que retorna o código referente ao erro da operação
                //getActionCode()
                Log.d(TAG, "onError: " + provider.listOfErrors)

            }
        }
        provider.execute()
    }

    fun abortPayment(context: Context) {
        if (transactionProvider != null)  {
            transactionProvider?.abortPayment()
        }
    }
}
