package xueluoanping.vaultterminal.data.lang;

import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.LanguageProvider;

public abstract class LangHelper extends LanguageProvider {
    protected final ExistingFileHelper helper;
    protected final PackOutput output;


    public LangHelper(PackOutput output, ExistingFileHelper helper, String modid, String locale) {
        super(output, modid, locale);
        this.output = output;
        this.helper = helper;
        this.modid = modid;
        this.locale = locale;
    }


    // There is a lot of code here that is redundant, but indispensable. In order to make corrections
    protected abstract void addTranslations();

    private final String locale;
    public final String modid;

}
