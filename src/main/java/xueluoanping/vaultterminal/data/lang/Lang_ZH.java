package xueluoanping.vaultterminal.data.lang;


import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import xueluoanping.vaultterminal.ModContents;
import xueluoanping.vaultterminal.SafeReader;

public class Lang_ZH extends LangHelper {
    public Lang_ZH(PackOutput gen, ExistingFileHelper helper) {
        super(gen, helper, SafeReader.MOD_ID, "zh_cn");
    }


    @Override
    protected void addTranslations() {
        add(ModContents.vault_terminal.get(), "保险箱终端");
        add("menu.create_safe_reader.tittle","读取中 - %s");

    }


}
