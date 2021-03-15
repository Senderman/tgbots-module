package com.annimon.tgbotsmodule.commands;

import com.annimon.tgbotsmodule.BotHandler;
import com.annimon.tgbotsmodule.analytics.UpdateHandler;
import com.annimon.tgbotsmodule.commands.authority.Authority;
import com.annimon.tgbotsmodule.commands.context.CallbackQueryContext;
import com.annimon.tgbotsmodule.commands.context.CallbackQueryContextBuilder;
import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.annimon.tgbotsmodule.commands.context.MessageContextBuilder;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.api.objects.Update;

@SuppressWarnings({"UnusedReturnValue", "unused"})
public class CommandRegistry<TRole extends Enum<TRole>> implements UpdateHandler {

    private final BotHandler handler;
    private final String botUsername;
    private final ListMultimap<String, TextCommand> textCommands;
    private final List<RegexCommand> regexCommands;
    private final ListMultimap<String, CallbackQueryCommand> callbackCommands;
    private final Authority<TRole> authority;

    private String callbackCommandSplitPattern;

    public CommandRegistry(@NotNull BotHandler handler, @NotNull Authority<TRole> authority) {
        this.handler = handler;
        this.authority = authority;
        this.botUsername = "@" + handler.getBotUsername().toLowerCase(Locale.ENGLISH);
        textCommands = ArrayListMultimap.create();
        regexCommands = new ArrayList<>();
        callbackCommands = ArrayListMultimap.create();

        callbackCommandSplitPattern = ":";
    }

    public CommandRegistry<TRole> register(@NotNull TextCommand command) {
        Objects.requireNonNull(command);
        Stream.concat(Stream.of(command.command()), command.aliases().stream())
                .map(this::stringToCommand)
                .forEach(key -> textCommands.put(key, command));
        return this;
    }

    public CommandRegistry<TRole> register(@NotNull RegexCommand command) {
        Objects.requireNonNull(command);
        regexCommands.add(command);
        return this;
    }

    public CommandRegistry<TRole> register(@NotNull CallbackQueryCommand command) {
        Objects.requireNonNull(command);
        callbackCommands.put(command.command(), command);
        return this;
    }

    public CommandRegistry<TRole> registerBundle(@NotNull CommandBundle<TRole> bundle) {
        Objects.requireNonNull(bundle);
        bundle.register(this);
        return this;
    }

    /**
     * Splits {@code callback.data} by whitespace ({@code "cmd:args"})
     * @return this
     */
    public CommandRegistry<TRole> splitCallbackCommandByColon() {
        return splitCallbackCommandByPattern(":");
    }

    /**
     * Splits {@code callback.data} by whitespace ({@code "cmd args"})
     * @return this
     */
    public CommandRegistry<TRole> splitCallbackCommandByWhitespace() {
        return splitCallbackCommandByPattern("\\s+");
    }

    /**
     * Treats whole {@code callback.data} as command ({@code "cmd"})
     * @return this
     */
    public CommandRegistry<TRole> doNotSplitCallbackCommands() {
        return splitCallbackCommandByPattern("$");
    }

    public CommandRegistry<TRole> splitCallbackCommandByPattern(@NotNull String pattern) {
        this.callbackCommandSplitPattern = Objects.requireNonNull(pattern);
        return this;
    }

    @Override
    public boolean handleUpdate(@NotNull Update update) {
        if (update.hasMessage()) {
            // Text commands
            if (update.getMessage().hasText()) {
                if ((!textCommands.isEmpty()) && handleTextCommands(update)) {
                    return true;
                }
                if ((!regexCommands.isEmpty()) && handleRegexCommands(update)) {
                    return true;
                }
            }
        } else if (update.hasCallbackQuery()) {
            // Callback commands
            final var data = update.getCallbackQuery().getData();
            if (data != null && !data.isEmpty()) {
                if ((!callbackCommands.isEmpty()) && handleCallbackQueryCommands(update)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean handleTextCommands(@NotNull Update update) {
        final var message = update.getMessage();
        final var args = message.getText().split("\\s+", 2);
        final var command = stringToCommand(args[0]);
        final var commands = Stream.ofNullable(textCommands.get(command))
                .flatMap(Collection::stream)
                .filter(cmd -> authority.hasRights(update, message.getFrom(), cmd.authority()))
                .collect(Collectors.toList());
        if (commands.isEmpty()) {
            return false;
        }

        final MessageContext context = new MessageContextBuilder()
                .setSender(handler)
                .setUpdate(update)
                .setText(args.length >= 2 ? args[1] : "")
                .createMessageContext();
        for (TextCommand cmd : commands) {
            cmd.accept(context);
        }
        return true;
    }

    protected boolean handleRegexCommands(@NotNull Update update) {
        final var message = update.getMessage();
        final var text = message.getText();
        final long count = regexCommands.stream()
                .map(cmd -> Map.entry(cmd, cmd.pattern().matcher(text)))
                .filter(e -> e.getValue().find())
                .filter(e -> authority.hasRights(update, message.getFrom(), e.getKey().authority()))
                .map(e -> Map.entry(e.getKey(), new MessageContextBuilder()
                        .setSender(handler)
                        .setUpdate(update)
                        .setText(text)
                        .createRegexContext(e.getValue())))
                .peek(e -> e.getKey().accept(e.getValue()))
                .count();
        return (count > 0);
    }

    protected boolean handleCallbackQueryCommands(@NotNull Update update) {
        final var query = update.getCallbackQuery();
        final var args = query.getData().split(callbackCommandSplitPattern, 2);
        final var command = args[0];
        final var commands = Stream.ofNullable(callbackCommands.get(command))
                .flatMap(Collection::stream)
                .filter(cmd -> authority.hasRights(update, query.getFrom(), cmd.authority()))
                .collect(Collectors.toList());
        if (commands.isEmpty()) {
            return false;
        }

        final CallbackQueryContext context = new CallbackQueryContextBuilder()
                .setSender(handler)
                .setUpdate(update)
                .setArgumentsAsString(args.length >= 2 ? args[1] : "")
                .createContext();
        for (CallbackQueryCommand cmd : commands) {
            cmd.accept(context);
        }
        return true;
    }

    protected String stringToCommand(String str) {
        return str.toLowerCase(Locale.ENGLISH).replace(botUsername, "");
    }
}
