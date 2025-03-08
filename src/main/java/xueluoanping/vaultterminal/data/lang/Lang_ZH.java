package xueluoanping.vaultterminal.data.lang;


import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import xueluoanping.vaultterminal.ModContents;
import xueluoanping.vaultterminal.VaultTerminal;

public class Lang_ZH extends LangHelper {
    public Lang_ZH(PackOutput gen, ExistingFileHelper helper) {
        super(gen, helper, VaultTerminal.MOD_ID, "zh_cn");
    }


    @Override
    protected void addTranslations() {
        add(ModContents.vault_terminal.get(), "保险箱终端");
        add("menu.create_vault_terminal.tittle","\"%s\"");
        add("hint.create_vault_terminal.not_open","无法打开存储空间！");


    }


}
