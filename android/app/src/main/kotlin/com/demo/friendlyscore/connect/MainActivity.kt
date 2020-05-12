package com.demo.friendlyscore.connect

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.friendlyscore.base.Environments
import com.friendlyscore.ui.obp.FriendlyScoreView
import com.friendlyscore.ui.obp.FriendlyScoreView.Companion.startFriendlyScoreView
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import org.json.JSONObject

class MainActivity: FlutterActivity() {
    private val CHANNEL = "friendlyscore/connect"
    var finalResult: MethodChannel.Result?=null
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler {
            call, result ->
            finalResult = result
            userReference = call.argument<String>("user-reference")!!
            if(call.method == "startFriendlyScoreConnect"){


                startFriendlyScoreConnect(call, result)
            }

        }
    }

    fun startFriendlyScoreConnect(call:MethodCall,result: MethodChannel.Result){
        Handler(Looper.getMainLooper()).post {
            // Call the desired channel message here.
            FriendlyScoreView.startFriendlyScoreView(this, getString(R.string.fs_client_id), userReference, REQUEST_CODE_FRIENDLY_SCORE, Environments.PRODUCTION)
        }
    }
    /**
     * In order to initialize FriendlyScore for your user you must have the `userReference` for that user.
     * The `userReference` uniquely identifies the user in your systems.
     * This `userReference` can then be used to access information from the FriendlyScore [api](https://friendlyscore.com/developers/api).
     */
    var userReference = "your_user_reference"

    /**
     * In order to listen when the user returns from the FriendlyScoreView in your `onActivityResult`, you must provide the `requestcode` that you will be using.
     */
    val REQUEST_CODE_FRIENDLY_SCORE = 11



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_FRIENDLY_SCORE) {
            if (data != null) {

                //Present if there was error in creating an access token for the supplied userReference.
                if (data.hasExtra("userReferenceAuthError")) {
                    finalResult?.error("userReferenceAuthError", "", "")
                    //Do Something
                }

                //Present if there was service denied.
                if (data.hasExtra("serviceDenied")) {

                    if (data.hasExtra("serviceDeniedMessage")) {
                        val serviceDeniedMessage = data.getStringExtra("serviceDeniedMessage")
                        if (serviceDeniedMessage != null) {
                            finalResult?.error("serviceDenied", serviceDeniedMessage, "")

                        }
                    }
                }
                //Present if the configuration on the server is incomplete.
                if (data != null && data.hasExtra("incompleteConfiguration")) {
                    if (data.hasExtra("incompleteConfigurationMessage")) {
                        val errorDescription = data.getStringExtra("incompleteConfigurationMessage")
                        if (errorDescription != null) finalResult?.error("incompleteConfiguration", errorDescription, "")

                    }
                }
                //Present if there was error in obtaining configuration from server
                if (data.hasExtra("serverError")) {
                    finalResult?.error("serverError", "", "")
                }
                //Present if the user closed the flow
                if (data.hasExtra("userClosedView")) {
                   finalResult?.success("userClosedView")

                }
            }
        }
    }

}
