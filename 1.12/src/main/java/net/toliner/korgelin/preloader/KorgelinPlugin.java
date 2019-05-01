package net.toliner.korgelin.preloader;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import java.util.Map;

class KorgelinPlugin implements IFMLLoadingPlugin {
    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return "net.toliner.korgelin.preloader.KorgelinSetup";
    }

    @Override
    public void injectData(Map<String, Object> data) {

    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}