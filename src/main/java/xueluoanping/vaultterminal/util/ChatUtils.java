package xueluoanping.vaultterminal.util;

import com.google.gson.*;
import net.minecraft.network.chat.Component;

public class ChatUtils {
    public static Component parseJson(String json) {
        Gson gson = new Gson();
        try {
            JsonElement element = gson.fromJson(json, JsonElement.class);
            if (element.isJsonObject()) {
                return parseJsonObject(element.getAsJsonObject());
            } else if (element.isJsonArray()) {
                return parseJsonArray(element.getAsJsonArray());
            } else {
                return Component.translatable(element.getAsString());
            }
        } catch (JsonSyntaxException e) {
            return Component.translatable(json);
        }
    }

    private static Component parseJsonObject(JsonObject jsonObject) {
        var component = Component.empty();

        if (jsonObject.has("text")) {
            component = component.append(jsonObject.get("text").getAsString());
        }

        if (jsonObject.has("extra")) {
            JsonArray extra = jsonObject.getAsJsonArray("extra");
            for (JsonElement element : extra) {
                component = component.append(parseJsonElement(element));
            }
        }

        if (jsonObject.has("color")) {
            component.withStyle(style -> style.withColor(net.minecraft.ChatFormatting.valueOf(jsonObject.get("color").getAsString().toUpperCase())));
        }

        return component;
    }

    private static Component parseJsonArray(JsonArray jsonArray) {
        var component = Component.empty();
        for (JsonElement element : jsonArray) {
            component = component.append(parseJsonElement(element));
        }
        return component;
    }

    private static Component parseJsonElement(JsonElement element) {
        if (element.isJsonObject()) {
            return parseJsonObject(element.getAsJsonObject());
        } else if (element.isJsonArray()) {
            return parseJsonArray(element.getAsJsonArray());
        } else {
            return Component.translatable(element.getAsString());
        }
    }
}
