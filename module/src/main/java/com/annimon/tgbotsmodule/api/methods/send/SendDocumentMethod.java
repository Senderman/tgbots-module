package com.annimon.tgbotsmodule.api.methods.send;

import com.annimon.tgbotsmodule.api.methods.interfaces.MediaMessageMethod;
import com.annimon.tgbotsmodule.api.methods.interfaces.ParseModeMethod;
import com.annimon.tgbotsmodule.api.methods.interfaces.CaptionMethod;
import com.annimon.tgbotsmodule.api.methods.interfaces.ThumbMethod;
import com.annimon.tgbotsmodule.services.CommonAbsSender;
import java.io.InputStream;
import java.util.Objects;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class SendDocumentMethod implements
        MediaMessageMethod<SendDocumentMethod, Message>,
        ParseModeMethod<SendDocumentMethod, Message>,
        CaptionMethod<SendDocumentMethod, Message>,
        ThumbMethod<SendDocumentMethod, Message> {

    private final SendDocument method;

    public SendDocumentMethod() {
        this(new SendDocument());
    }

    public SendDocumentMethod(@NotNull SendDocument method) {
        this.method = method;
    }

    @Override
    public String getChatId() {
        return method.getChatId();
    }

    @Override
    public SendDocumentMethod setChatId(@NotNull String chatId) {
        method.setChatId(chatId);
        return this;
    }

    @Override
    public Integer getReplyToMessageId() {
        return method.getReplyToMessageId();
    }

    @Override
    public SendDocumentMethod setReplyToMessageId(Integer messageId) {
        method.setReplyToMessageId(messageId);
        return this;
    }

    @Override
    public boolean isNotificationDisabled() {
        return !Objects.requireNonNullElse(method.getDisableNotification(), false);
    }

    @Override
    public SendDocumentMethod enableNotification() {
        method.enableNotification();
        return this;
    }

    @Override
    public SendDocumentMethod disableNotification() {
        method.disableNotification();
        return this;
    }

    @Override
    public ReplyKeyboard getReplyMarkup() {
        return method.getReplyMarkup();
    }

    @Override
    public SendDocumentMethod setReplyMarkup(ReplyKeyboard replyMarkup) {
        method.setReplyMarkup(replyMarkup);
        return this;
    }

    @Override
    public InputFile getFile() {
        return method.getDocument();
    }

    @Override
    public SendDocumentMethod setFile(@NotNull String fileId) {
        method.setDocument(fileId);
        return this;
    }

    @Override
    public SendDocumentMethod setFile(@NotNull java.io.File file) {
        method.setDocument(file);
        return this;
    }

    @Override
    public SendDocumentMethod setFile(@NotNull String name, @NotNull InputStream inputStream) {
        method.setDocument(name, inputStream);
        return this;
    }

    @Override
    public SendDocumentMethod setFile(@NotNull InputFile file) {
        method.setDocument(file);
        return this;
    }

    @Override
    public String getParseMode() {
        return method.getParseMode();
    }

    @Override
    public SendDocumentMethod setParseMode(String parseMode) {
        method.setParseMode(parseMode);
        return this;
    }
    
    @Override
    public String getCaption() {
        return method.getCaption();
    }

    @Override
    public SendDocumentMethod setCaption(String caption) {
        method.setCaption(caption);
        return this;
    }

    @Override
    public InputFile getThumb() {
        return method.getThumb();
    }

    @Override
    public SendDocumentMethod setThumb(InputFile thumb) {
        method.setThumb(thumb);
        return this;
    }

    @Override
    public Message execute(@NotNull CommonAbsSender sender) {
        return sender.call(method);
    }

    @Override
    public void executeAsync(@NotNull CommonAbsSender sender,
                             @Nullable Consumer<? super Message> responseConsumer,
                             @Nullable Consumer<TelegramApiException> apiExceptionConsumer,
                             @Nullable Consumer<Exception> exceptionConsumer) {
        sender.callAsync(method, responseConsumer, apiExceptionConsumer);
    }
}