package top.yukonga.hapticFeedBack

import android.os.Handler
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface
import java.lang.reflect.Method
import java.util.Arrays

private lateinit var module: ModuleMain
private lateinit var loadedPackageParam: XposedModuleInterface.PackageLoadedParam

class ModuleMain(base: XposedInterface, param: XposedModuleInterface.ModuleLoadedParam) : XposedModule(base, param) {

    init {
        module = this
    }

    override fun onPackageLoaded(param: XposedModuleInterface.PackageLoadedParam) {
        super.onPackageLoaded(param)
        loadedPackageParam = param
        if (param.packageName == "com.miui.home") {
            val hapticFeedbackCompatV2 = "com.miui.home.launcher.common.HapticFeedbackCompatV2"
            val gestureStubView = "com.miui.home.recents.GestureStubView"
            val gestureStubViewClass = param.classLoader.loadClass(gestureStubView)
            val hapticFeedbackCompatV2Class = param.classLoader.loadClass(hapticFeedbackCompatV2)
            hookMethods(hapticFeedbackCompatV2Class, PerformGestureReadyBackHook::class.java, "performGestureReadyBack")
            hookMethods(hapticFeedbackCompatV2Class, PerformGestureReadyBackLambdaHook::class.java, "lambda\$performGestureReadyBack\$11")
            hookMethods(hapticFeedbackCompatV2Class, PerformGestureBackHandUpHook::class.java, "performGestureBackHandUp")
            hookMethods(hapticFeedbackCompatV2Class, PerformGestureBackHandUpLambdaHook::class.java, "lambda\$performGestureBackHandUp\$12")
            hookMethods(gestureStubViewClass, InjectKeyEventHook::class.java, "injectKeyEvent")
        }
    }

    private fun hookMethods(clazz: Class<*>, hooker: Class<out XposedInterface.Hooker?>, vararg names: String) {
        val list = listOf(*names)
        Arrays.stream(clazz.declaredMethods)
            .filter { method: Method -> list.contains(method.name) }
            .forEach { method: Method? -> hook(method!!, hooker) }
    }

}

class PerformGestureReadyBackHook : XposedInterface.Hooker {
    companion object {
        @JvmStatic
        fun before() {
            if (BuildConfig.DEBUG) module.log("hooking performGestureReadyBack")
            val timeOutBlocker = "com.miui.home.recents.util.TimeOutBlocker"
            val backgroundThread = "com.miui.home.launcher.common.BackgroundThread"
            val getHandlerMethod = loadedPackageParam.classLoader.loadClass(backgroundThread).getDeclaredMethod("getHandler")
            val getHandler = getHandlerMethod.invoke(null)  // getHandler is a static method
            val startCountDownMethod = loadedPackageParam.classLoader.loadClass(timeOutBlocker)
                .getDeclaredMethod("startCountDown", Handler::class.java, Long::class.java, String::class.java)
            startCountDownMethod.invoke(null, getHandler, 140L, "BLOCKER_ID_FOR_HAPTIC_GESTURE_BACK")  // startCountDown is a static method
        }
    }
}

class PerformGestureReadyBackLambdaHook : XposedInterface.Hooker {
    companion object {
        @JvmStatic
        fun before(callback: XposedInterface.BeforeHookCallback) {
            if (BuildConfig.DEBUG) module.log("hooking lambda\$performGestureReadyBack\$11")
            val mHapticHelperField = callback.getThisObject()?.javaClass?.getDeclaredField("mHapticHelper")
            mHapticHelperField?.isAccessible = true
            val mHapticHelper = mHapticHelperField?.get(callback.getThisObject())
            val performExtHapticFeedback = mHapticHelper?.javaClass?.getDeclaredMethod("performExtHapticFeedback", Int::class.java)
            performExtHapticFeedback?.invoke(mHapticHelper, 0)
            callback.returnAndSkip(null)
        }
    }
}

class PerformGestureBackHandUpHook : XposedInterface.Hooker {
    companion object {
        @JvmStatic
        fun before(callback: XposedInterface.BeforeHookCallback) {
            if (BuildConfig.DEBUG) module.log("hooking performGestureBackHandUp")
            val timeOutBlocker = "com.miui.home.recents.util.TimeOutBlocker"
            val isBlockedMethod = loadedPackageParam.classLoader.loadClass(timeOutBlocker).getDeclaredMethod("isBlocked", String::class.java)
            val isBlocked = isBlockedMethod.invoke(null, "BLOCKER_ID_FOR_HAPTIC_GESTURE_BACK") as Boolean  // isBlocked is a static method
            if (isBlocked) callback.returnAndSkip(null)
        }
    }
}

class PerformGestureBackHandUpLambdaHook : XposedInterface.Hooker {
    companion object {
        @JvmStatic
        fun before(callback: XposedInterface.BeforeHookCallback) {
            if (BuildConfig.DEBUG) module.log("hooking lambda\$performGestureBackHandUp\$12")
            val mHapticHelperField = callback.getThisObject()?.javaClass?.getDeclaredField("mHapticHelper")
            mHapticHelperField?.isAccessible = true
            val mHapticHelper = mHapticHelperField?.get(callback.getThisObject())
            val performExtHapticFeedback = mHapticHelper?.javaClass?.getDeclaredMethod("performExtHapticFeedback", Int::class.java)
            performExtHapticFeedback?.invoke(mHapticHelper, 1)
            callback.returnAndSkip(null)
        }
    }
}

class InjectKeyEventHook : XposedInterface.Hooker {
    companion object {
        @JvmStatic
        fun before(callback: XposedInterface.BeforeHookCallback) {
            if (BuildConfig.DEBUG) module.log("hooking injectKeyEvent")
            callback.getArgs()[1] = true
        }
    }
}

