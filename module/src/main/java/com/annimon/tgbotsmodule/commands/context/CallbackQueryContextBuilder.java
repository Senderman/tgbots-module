package com.annimon.tgbotsmodule.commands.context;

import com.annimon.tgbotsmodule.services.CommonAbsSender;
import org.telegram.telegrambots.meta.api.objects.Update;

public class CallbackQueryContextBuilder {

    private CommonAbsSender sender;
    private Update update;
    private String arguments;

    public CallbackQueryContextBuilder setSender(CommonAbsSender sender) {
        this.sender = sender;
        return this;
    }

    public CallbackQueryContextBuilder setUpdate(Update update) {
        this.update = update;
        return this;
    }

    public CallbackQueryContextBuilder setArguments(String arguments) {
        this.arguments = arguments;
        return this;
    }

    public CallbackQueryContext createContext() {
        return new CallbackQueryContext(sender, update, arguments);
    }
}
