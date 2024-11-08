package top.yukonga.hapticFeedBack

import android.os.Handler
import art.qqlittleice.xposedcompat.transition.LoadedPackageParam
import art.qqlittleice.xposedcompat.transition.ModulePackageParam
import art.qqlittleice.xposedcompat.transition.UniversalBridge
import art.qqlittleice.xposedcompat.util.hookBeforeMethod
import art.qqlittleice.xposedcompat.util.replaceMethod
import art.qqlittleice.xposedloader.UniversalLoader

class HookEntry(bridge: UniversalBridge) : UniversalLoader(bridge) {

    override fun onPackageLoaded(
        modulePackageParam: ModulePackageParam,
        loadedPackageParam: LoadedPackageParam
    ) {
        if (loadedPackageParam.packageName == "com.miui.home") {
            bridge.log("hooking com.miui.home")
            val hapticFeedbackCompatV2 = "com.miui.home.launcher.common.HapticFeedbackCompatV2"
            val timeOutBlocker = "com.miui.home.recents.util.TimeOutBlocker"
            val backgroundThread = "com.miui.home.launcher.common.BackgroundThread"
            val gestureStubView = "com.miui.home.recents.GestureStubView"

            gestureStubView.hookBeforeMethod(loadedPackageParam.classLoader, "injectKeyEvent", Int::class.java, Boolean::class.java) {
                bridge.log("hooking injectKeyEvent")
                it.getArgs()?.set(1, true)
            }

            hapticFeedbackCompatV2.hookBeforeMethod(loadedPackageParam.classLoader, "performGestureReadyBack") {
                bridge.log("hooking performGestureReadyBack")
                val getHandlerMethod = loadedPackageParam.classLoader.loadClass(backgroundThread).getDeclaredMethod("getHandler")
                val getHandler = getHandlerMethod.invoke(null)  // getHandler is a static method
                val startCountDownMethod = loadedPackageParam.classLoader.loadClass(timeOutBlocker)
                    .getDeclaredMethod("startCountDown", Handler::class.java, Long::class.java, String::class.java)
                startCountDownMethod.invoke(null, getHandler, 140L, "BLOCKER_ID_FOR_HAPTIC_GESTURE_BACK")  // startCountDown is a static method
            }

            hapticFeedbackCompatV2.replaceMethod(loadedPackageParam.classLoader, "lambda\$performGestureReadyBack\$11") {
                bridge.log("hooking lambda\$performGestureReadyBack\$11")
                val mHapticHelperField = it.getThisObject()?.javaClass?.getDeclaredField("mHapticHelper")
                mHapticHelperField?.isAccessible = true
                val mHapticHelper = mHapticHelperField?.get(it.getThisObject())
                val performExtHapticFeedback = mHapticHelper?.javaClass?.getDeclaredMethod("performExtHapticFeedback", Int::class.java)
                performExtHapticFeedback?.invoke(mHapticHelper, 0)
            }

            hapticFeedbackCompatV2.hookBeforeMethod(loadedPackageParam.classLoader, "performGestureBackHandUp") {
                bridge.log("hooking performGestureBackHandUp")
                val isBlockedMethod = loadedPackageParam.classLoader.loadClass(timeOutBlocker).getDeclaredMethod("isBlocked", String::class.java)
                val isBlocked = isBlockedMethod.invoke(null, "BLOCKER_ID_FOR_HAPTIC_GESTURE_BACK") as Boolean  // isBlocked is a static method
                if (isBlocked) it.returnAndSkip(null)
            }

            hapticFeedbackCompatV2.replaceMethod(loadedPackageParam.classLoader, "lambda\$performGestureBackHandUp\$12") {
                bridge.log("hooking lambda\$performGestureBackHandUp\$12")
                val mHapticHelperField = it.getThisObject()?.javaClass?.getDeclaredField("mHapticHelper")
                mHapticHelperField?.isAccessible = true
                val mHapticHelper = mHapticHelperField?.get(it.getThisObject())
                val performExtHapticFeedback = mHapticHelper?.javaClass?.getDeclaredMethod("performExtHapticFeedback", Int::class.java)
                performExtHapticFeedback?.invoke(mHapticHelper, 1)
            }
        }
    }
}