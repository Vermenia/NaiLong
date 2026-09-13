package com.vermenia.nailong.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.vermenia.nailong.ai.NailongAiConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/**
 * 奶龙配置命令
 */
public class NailongCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("nailong")
                .then(Commands.literal("config")
                    // 查看配置 - 所有玩家都可以
                    .executes(NailongCommand::showConfig)
                    // 修改配置 - 仅限管理员（OP等级2或更高）
                    .then(Commands.literal("endpoint")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("url", StringArgumentType.greedyString())
                            .executes(ctx -> setEndpoint(ctx, StringArgumentType.getString(ctx, "url")))
                        )
                    )
                    .then(Commands.literal("key")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("apikey", StringArgumentType.greedyString())
                            .executes(ctx -> setApiKey(ctx, StringArgumentType.getString(ctx, "apikey")))
                        )
                    )
                    .then(Commands.literal("model")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("modelname", StringArgumentType.greedyString())
                            .executes(ctx -> setModel(ctx, StringArgumentType.getString(ctx, "modelname")))
                        )
                    )
                )
        );
    }

    private static int showConfig(CommandContext<CommandSourceStack> context) {
        NailongAiConfig config = NailongAiConfig.getInstance();
        CommandSourceStack source = context.getSource();
        boolean isAdmin = source.hasPermission(2);

        source.sendSuccess(() -> Component.literal("§6========== 奶龙AI配置 =========="), false);

        // API端点状态
        String endpoint = config.getApiEndpoint();
        source.sendSuccess(() -> Component.literal("§eAPI端点: §f" +
                (endpoint.isEmpty() ? "§c未配置" : endpoint)), false);

        // API密钥状态 - 仅管理员可见详情
        String apiKey = config.getApiKey();
        if (isAdmin) {
            String keyDisplay = apiKey.isEmpty() ? "§c未配置" :
                    "§a已配置 (***" + apiKey.substring(Math.max(0, apiKey.length() - 4)) + ")";
            source.sendSuccess(() -> Component.literal("§eAPI密钥: " + keyDisplay), false);
        } else {
            source.sendSuccess(() -> Component.literal("§eAPI密钥: " +
                    (apiKey.isEmpty() ? "§c未配置" : "§a已配置 ✓")), false);
        }

        // 模型名称
        source.sendSuccess(() -> Component.literal("§e模型名称: §f" + config.getModel()), false);

        // 配置状态
        source.sendSuccess(() -> Component.literal("§e配置状态: " +
                (config.isConfigured() ? "§a完整 ✓" : "§c不完整 ✗")), false);

        source.sendSuccess(() -> Component.literal("§6================================"), false);

        // 如果配置不完整，显示帮助信息
        if (!config.isConfigured()) {
            source.sendSuccess(() -> Component.literal(""), false);
            source.sendSuccess(() -> Component.literal("§c⚠ 配置不完整！" +
                    (isAdmin ? "请完成以下配置:" : "请联系服务器管理员配置")), false);
            if (isAdmin) {
                if (endpoint.isEmpty()) {
                    source.sendSuccess(() -> Component.literal("§7  /nailong config endpoint <API地址>"), false);
                }
                if (apiKey.isEmpty()) {
                    source.sendSuccess(() -> Component.literal("§7  /nailong config key <API密钥>"), false);
                }
                source.sendSuccess(() -> Component.literal(""), false);
                source.sendSuccess(() -> Component.literal("§7示例配置:"), false);
                source.sendSuccess(() -> Component.literal("§7  /nailong config endpoint https://api.openai.com/v1/chat/completions"), false);
                source.sendSuccess(() -> Component.literal("§7  /nailong config key sk-xxxxxxxxxxxx"), false);
                source.sendSuccess(() -> Component.literal("§7  /nailong config model gpt-5.6-luna"), false);
            }
        } else {
            source.sendSuccess(() -> Component.literal(""), false);
            source.sendSuccess(() -> Component.literal("§a✓ 配置完成！所有玩家都可以和奶龙聊天了"), false);
            source.sendSuccess(() -> Component.literal("§7在聊天中提到\"奶龙\"即可触发对话"), false);
        }

        return 1;
    }

    private static int setEndpoint(CommandContext<CommandSourceStack> context, String endpoint) {
        NailongAiConfig config = NailongAiConfig.getInstance();
        config.setApiEndpoint(endpoint);
        config.save();

        context.getSource().sendSuccess(() ->
            Component.literal("§a✓ 已设置API端点: §f" + endpoint), false);
        context.getSource().sendSuccess(() ->
            Component.literal("§7此配置对所有玩家生效"), false);

        // 检查配置是否完整
        if (!config.isConfigured()) {
            context.getSource().sendSuccess(() ->
                Component.literal("§e⚠ 还需要配置API密钥: §7/nailong config key <密钥>"), false);
        } else {
            context.getSource().sendSuccess(() ->
                Component.literal("§a✓ 配置完成！所有玩家都可以和奶龙聊天了"), false);
        }

        return 1;
    }

    private static int setApiKey(CommandContext<CommandSourceStack> context, String apiKey) {
        NailongAiConfig config = NailongAiConfig.getInstance();
        config.setApiKey(apiKey);
        config.save();

        String keyDisplay = "***" + apiKey.substring(Math.max(0, apiKey.length() - 4));
        context.getSource().sendSuccess(() ->
            Component.literal("§a✓ 已设置API密钥: §f" + keyDisplay), false);
        context.getSource().sendSuccess(() ->
            Component.literal("§7此配置对所有玩家生效"), false);

        // 检查配置是否完整
        if (!config.isConfigured()) {
            context.getSource().sendSuccess(() ->
                Component.literal("§e⚠ 还需要配置API端点: §7/nailong config endpoint <地址>"), false);
        } else {
            context.getSource().sendSuccess(() ->
                Component.literal("§a✓ 配置完成！所有玩家都可以和奶龙聊天了"), false);
        }

        return 1;
    }

    private static int setModel(CommandContext<CommandSourceStack> context, String model) {
        NailongAiConfig config = NailongAiConfig.getInstance();
        config.setModel(model);
        config.save();

        context.getSource().sendSuccess(() ->
            Component.literal("§a✓ 已设置模型: §f" + model), false);
        context.getSource().sendSuccess(() ->
            Component.literal("§7此配置对所有玩家生效"), false);
        return 1;
    }
}
