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
            val gesturesBackClass = param.classLoader.loadClass("com.miui.home.recents.GesturesBackTouchProcessor")
            //强制执行Feedback
            hookMethods(gesturesBackClass, IsDoFeedBackHook::class.java, "isDoFeedBack")}
    }

    private fun hookMethods(clazz: Class<*>, hooker: Class<out XposedInterface.Hooker?>, vararg names: String) {
        val list = listOf(*names)
        Arrays.stream(clazz.declaredMethods)
            .filter { method: Method -> list.contains(method.name) }
            .forEach { method: Method? -> hook(method!!, hooker) }
    }
}


class IsDoFeedBackHook : XposedInterface.Hooker {
    companion object {
        @JvmStatic
        fun before(callback: XposedInterface.BeforeHookCallback) {
            if (BuildConfig.DEBUG) module.log("hooking isDoFeedBack -> true")
            // 强制返回 true 并跳过原方法
            callback.returnAndSkip(true)
        }
    }
}