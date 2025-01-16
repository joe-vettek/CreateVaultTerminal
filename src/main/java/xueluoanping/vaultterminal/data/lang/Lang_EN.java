package xueluoanping.vaultterminal.data.lang;

import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import xueluoanping.vaultterminal.ModContents;
import xueluoanping.vaultterminal.SafeReader;

public class Lang_EN extends LangHelper {
    public Lang_EN(PackOutput gen, ExistingFileHelper helper) {
        super(gen, helper, SafeReader.MOD_ID, "en_us");
    }


    @Override
    protected void addTranslations() {
        add(ModContents.vault_terminal.get(), "Vault Terminal");
        add("menu.create_safe_reader.tittle", "Connected - %s");
    }


}
