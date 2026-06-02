package top.yukonga.hapticFeedBack

import android.util.Log
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface
import java.lang.reflect.Executable
import java.lang.reflect.Method
import java.util.Arrays

private lateinit var module: ModuleMain
private lateinit var loadedPackageParam: XposedModuleInterface.PackageReadyParam

class ModuleMain : XposedModule() {

    override fun onModuleLoaded(param: XposedModuleInterface.ModuleLoadedParam) {
        super.onModuleLoaded(param)
        module = this
    }

    override fun onPackageReady(param: XposedModuleInterface.PackageReadyParam) {
        super.onPackageReady(param)
        loadedPackageParam = param
        if (param.packageName == "com.miui.home") {
            val gesturesBackClass = param.classLoader.loadClass("com.miui.home.recents.GesturesBackTouchProcessor")
            hookMethods(gesturesBackClass, IsDoFeedBackHook(), "isDoFeedBack")
        }
    }

    private fun hookMethods(clazz: Class<*>, hooker: XposedInterface.Hooker, vararg names: String) {
        val list = listOf(*names)
        Arrays.stream(clazz.declaredMethods)
            .filter { method: Method -> list.contains(method.name) }
            .forEach { method: Executable -> hook(method).intercept(hooker) }
    }
}

class IsDoFeedBackHook : XposedInterface.Hooker {
    override fun intercept(chain: XposedInterface.Chain): Any {
        if (BuildConfig.DEBUG) module.log(Log.DEBUG, "HapticFeedBack", "hooking isDoFeedBack -> true")
        return true
    }
}
