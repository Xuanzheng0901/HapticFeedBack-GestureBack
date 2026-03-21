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
            //Hook HapticFeedbackCompat.doesSupportHapticV2 to always return false

            val hapticFeedbackCompat = "com.miui.home.common.hapticfeedback.HapticFeedbackCompat"
            val hapticFeedbackCompatClass = param.classLoader.loadClass(hapticFeedbackCompat)

                try {
                    val field = hapticFeedbackCompatClass.getDeclaredField("sIsSupportHapticV2")
                    field.isAccessible = true
                    field.setBoolean(null, false)
                    if (BuildConfig.DEBUG) module.log("set sIsSupportHapticV2 = false via reflection")
                } catch (e: Throwable) {
                    if (BuildConfig.DEBUG) module.log("failed to set sIsSupportHapticV2: ${e.message}")
                }
            hookMethods(hapticFeedbackCompatClass, DoesSupportHapticV2Hook::class.java, "doesSupportHapticV2")
        }
    }

    private fun hookMethods(clazz: Class<*>, hooker: Class<out XposedInterface.Hooker?>, vararg names: String) {
        val list = listOf(*names)
        Arrays.stream(clazz.declaredMethods)
            .filter { method: Method -> list.contains(method.name) }
            .forEach { method: Method? -> hook(method!!, hooker) }
    }
}

class DoesSupportHapticV2Hook : XposedInterface.Hooker {
    companion object {
        @JvmStatic
        fun before(callback: XposedInterface.BeforeHookCallback) {
            if (BuildConfig.DEBUG) module.log("hooking doesSupportHapticV2 -> true")
            // Force the method to return false and skip original implementation
            callback.returnAndSkip(true)
        }
    }
}
