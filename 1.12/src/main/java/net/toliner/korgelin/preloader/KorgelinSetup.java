package net.toliner.korgelin.preloader;

import net.minecraftforge.fml.relauncher.IFMLCallHook;

import java.util.Map;

public class KorgelinSetup implements IFMLCallHook {
    @Override
    public void injectData(Map<String, Object> data) {
        ClassLoader loader = (ClassLoader)data.get("classLoader");
        try {
            loader.loadClass("net.toliner.korgelin.KotlinAdapter");
            loader.loadClass("kotlin.jvm.internal.Intrinsics");
        } catch (ClassNotFoundException e) {
            // this should never happen
            throw new RuntimeException("Couldn't find Korgelin langague adapter, this shouldn't be happening", e);
        }
    }

    @Override
    public Void call() throws Exception {
        return null;
    }
}
