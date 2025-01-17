package xueluoanping.vaultterminal.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class General {
    public static ForgeConfigSpec SERVER_CONFIG;
    public static ForgeConfigSpec.BooleanValue collectItemNearby;

    public static boolean isValidRegex(Object o) {
        if (!(o instanceof String regex)) {
            return false;
        }
        try {
            Pattern.compile(regex);
            return true;
        } catch (PatternSyntaxException e) {
            return false;
        }
    }

    static {
        ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();

        COMMON_BUILDER.comment("Play settings").push("Play");
        collectItemNearby = COMMON_BUILDER.comment("Can Place item in.")
                .define("CanPlaceItem", true);

        COMMON_BUILDER.pop();

        SERVER_CONFIG = COMMON_BUILDER.build();


    }


}
