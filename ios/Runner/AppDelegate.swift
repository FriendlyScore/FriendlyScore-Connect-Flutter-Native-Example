import UIKit
import Flutter
import FriendlyScoreCore
import FriendlyScoreConnect


@UIApplicationMain
@objc class AppDelegate: FlutterAppDelegate {
  override func application(
    _ application: UIApplication,
    didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
  ) -> Bool {
    FriendlyScore.configureConnect()
    
    let controller : FlutterViewController = window?.rootViewController as! FlutterViewController
    let channel = FlutterMethodChannel(name: "friendlyscore/connect",
    binaryMessenger: controller.binaryMessenger)
    channel.setMethodCallHandler({
        [weak self] (call: FlutterMethodCall, result: @escaping FlutterResult) -> Void in
      // Note: this method is invoked on the UI thread.
      guard call.method == "startFriendlyScoreConnect" else {
        result(FlutterMethodNotImplemented)
        return
      }
        
        guard let params = call.arguments as? [String:String] else { return }
        self?.startFriendlyScoreConnect(params: params, flutterResult: result)
    
    })
    GeneratedPluginRegistrant.register(with: self)
    return super.application(application, didFinishLaunchingWithOptions: launchOptions)
  }
    
    
    private func startFriendlyScoreConnect(params:[String:String], flutterResult:@escaping FlutterResult) {
        guard let userRef = params["user-reference"] else { return }
        
        let myCredentials = Credentials(clientId: ClientId(stringLiteral: "[YOUR_CLIENT_ID]"), userReference: userRef, environment: .production)
           FriendlyScore.show(with: myCredentials)
           
           FriendlyScore.eventsHandler = { event in
               switch event {
               case .userClosedView:
                flutterResult(FlutterError(code: "userClosedView", message: nil, details: nil))
               default:
                   break
               }
           }
           
           FriendlyScore.errorsHandler = { error in
               switch error {
               case .userReferenceAuth:
                flutterResult(FlutterError(code: "userReferenceAuthError", message: nil, details: nil))
               case .server:
                flutterResult(FlutterError(code: "serverError", message: nil, details: nil))
               case .serviceDenied:
                flutterResult(FlutterError(code: "serviceDenied", message: nil, details: nil))

        
               @unknown default:
                   break
               }
           }
        
    }
    override func application(_ app: UIApplication, open url: URL, options: [UIApplication.OpenURLOptionsKey : Any] = [:]) -> Bool {
        FriendlyScore.handleQueryParameters(for: url)
        return true
    }
}
