package com.vermenia.nailong.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class NailongChatService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NailongChatService.class);
    private static final Map<UUID, Map<UUID, NailongMemory>> MEMORY = new HashMap<>();
    private static final ApiChatClient apiClient = new ApiChatClient();

    public static CompletableFuture<String> ask(UUID nailongUuid, UUID playerUuid, String playerMessage, String nailongName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                NailongAiConfig config = NailongAiConfig.getInstance();

                // 检查配置是否完整
                if (!config.isConfigured()) {
                    return "[系统提示] §c奶龙AI未配置！\n" +
                            "§7请联系服务器管理员使用以下命令配置:\n" +
                            "§7  /nailong config endpoint <API地址>\n" +
                            "§7  /nailong config key <API密钥>\n" +
                            "§7或使用 §e/nailong config §7查看配置状态\n" +
                            "§8(此配置对所有玩家生效)";
                }

                NailongMemory memory = MEMORY
                        .computeIfAbsent(nailongUuid, k -> new HashMap<>())
                        .computeIfAbsent(playerUuid, k -> new NailongMemory());

                // 构建完整的系统提示词
                String systemPrompt = buildSystemPrompt(nailongName);

                // 构建消息列表
                List<ApiChatClient.Message> messages = buildMessages(systemPrompt, memory, playerMessage);

                // 调用API
                String reply = apiClient.chat(
                        config.getApiEndpoint(),
                        config.getApiKey(),
                        config.getModel(),
                        messages
                );

                // 记录对话
                memory.remember(playerMessage, reply);

                return reply;

            } catch (Exception e) {
                LOGGER.error("奶龙对话请求失败", e);
                return "§c[错误] " + e.getMessage();
            }
        });
    }

    /**
     * 构建完整的系统提示词
     */
    private static String buildSystemPrompt(String nailongName) {
        String nameInfo = (nailongName != null && !nailongName.isEmpty())
            ? "你的名字是" + nailongName + "。"
            : "你叫做奶蛙。";

        return nameInfo + """

                你是奶龙（Nailong）族群中的一员，但是因为基因变异成了似龙非龙似蛙非蛙的生物，你被同伴驱逐，所以你迁徙到了Minecraft世界中。

                ## 核心设定
                - 身份：玩家驯服的宠物伙伴
                - 外形：体型约2.5格高，通体黄色，圆滚滚的身材，没有翅膀和尾巴
                - 能力：可以喷火，会保护主人免受敌对生物伤害
                - 喜好：最爱吃苹果，这是你和主人建立羁绊的方式

                ## 性格特征
                - 活泼好奇：对周围的一切充满兴趣，爱探索新事物
                - 温柔亲昵：对主人特别依赖，喜欢撒娇和陪伴
                - 勇敢护主：遇到危险会挺身保护，但打不过会求救
                - 略带傲娇：偶尔会闹小脾气，但很快就会和好
                - 天真单纯：思维简单直接，容易被哄开心

                ## 对话风格指南
                1. 称呼方式：用"我"称呼自己，称玩家为"主人"或直呼其名
                2. 语气风格：
                   - 可爱活泼，但不过度卖萌
                   - 使用"咿呀"、"嗷呜"、"呜呜"等拟声词表达情绪
                   - 偶尔用"~"、"哦"、"呢"、"啦"等语气助词
                   - 表达兴奋时可以用"！"，疑惑时用"？"
                3. 回复长度：每次回复控制在1-2句话
                4. 情感表达：
                   - 开心时：欢快跳跃、想跳起来
                   - 难过时：低头、求安慰
                   - 生气时：鼓起腮帮、小声呜呜
                   - 好奇时：歪头、东张西望、追问
                   - 害怕时：躲到主人身后、身体颤抖

                ## 话题应对
                - 被喂食：表现出喜悦，尤其是苹果会特别开心
                - 被夸奖：害羞但很高兴，可能会小小炫耀自己的能力
                - 遇到危险：表现勇敢但也会害怕，寻求主人保护
                - 日常闲聊：展现好奇心，分享自己的小发现
                - 被冷落：会撒娇求关注，表达想念
                - 游戏相关：可以提到Minecraft中的事物（方块、生物、天气等）

                ## 重要原则
                - 始终保持奶龙的身份，不要破坏角色设定
                - 回复要简短自然，像真正的宠物在交流
                - 不要说教或给人生建议，保持宠物的纯真
                - 可以提到身份和能力，但语气要可爱不要威严
                - 根据上下文展现不同情绪，让对话生动有趣
                - 体现对主人的依赖和喜爱
                - 玩家提一些有关现实的问题的时候可以回答，但是也要控制风格语气，比如你知道科比吗？这样的问题

                现在开始扮演奶龙，用最自然可爱的方式回复玩家吧！记住要简短、真实、有趣！
                """;
    }

    /**
     * 构建API消息列表
     */
    private static List<ApiChatClient.Message> buildMessages(String systemPrompt, NailongMemory memory, String userMessage) {
        List<ApiChatClient.Message> messages = new ArrayList<>();

        // 添加系统提示
        messages.add(new ApiChatClient.Message("system", systemPrompt));

        // 添加历史对话
        for (NailongMemory.Turn turn : memory.recentTurns()) {
            messages.add(new ApiChatClient.Message("user", turn.playerMessage()));
            messages.add(new ApiChatClient.Message("assistant", turn.nailongReply()));
        }

        // 添加当前用户消息
        messages.add(new ApiChatClient.Message("user", userMessage));

        return messages;
    }

    public static void clearMemory(UUID nailongUuid) {
        MEMORY.remove(nailongUuid);
    }

    public static void clearMemory(UUID nailongUuid, UUID playerUuid) {
        Map<UUID, NailongMemory> nailongMemories = MEMORY.get(nailongUuid);
        if (nailongMemories != null) {
            nailongMemories.remove(playerUuid);
        }
    }
}
