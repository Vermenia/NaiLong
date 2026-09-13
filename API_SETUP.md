# 奶龙AI配置指南

## 权限说明

- **配置API**：仅限服务器管理员（OP等级2或更高）
- **查看配置**：所有玩家都可以查看
- **使用AI对话**：所有玩家共享同一配置，无需单独设置

## 快速开始

### 1. 管理员配置API

首次使用时，服务器管理员需要配置API。使用以下命令：

```
/nailong config endpoint <API地址>
/nailong config key <API密钥>
/nailong config model <模型名称>
```

**配置完成后，所有玩家都可以与奶龙对话。**

### 2. 配置示例

#### OpenAI兼容API (推荐)
```
/nailong config endpoint https://api.openai.com/v1/chat/completions
/nailong config key sk-your-api-key-here
/nailong config model gpt-5.6-luna
```

#### 其他兼容服务
任何支持OpenAI Chat Completions API格式的服务都可以使用：

- OpenAI
- Azure OpenAI
- Anthropic (通过兼容层)
- 本地部署的LLM服务 (如Ollama、LM Studio等)
- 各种代理服务

### 3. 查看配置状态

```
/nailong config
```

这会显示当前配置状态和缺失的配置项。

## 配置文件

配置会自动保存到 `config/nailong_ai.json`，内容如下：

```json
{
  "apiEndpoint": "https://api.openai.com/v1/chat/completions",
  "apiKey": "your-api-key",
  "model": "gpt-5.6-luna"
}
```

你也可以直接编辑此文件，然后重启游戏或重载配置。

## 使用方式

配置完成后，在聊天中提到"奶龙"即可触发对话：

```
你好奶龙
奶龙在干嘛
奶龙，来吃苹果
```

## 注意事项

1. **API密钥安全**：请妥善保管API密钥，不要分享配置文件
2. **消息长度限制**：每次消息不超过160字
3. **并发限制**：同时最多4个奶龙在处理请求
4. **距离限制**：需要在奶龙附近才能对话

## 故障排除

### 配置未生效
- 确保API端点URL正确（需要完整的URL）
- 检查API密钥是否有效
- 查看游戏日志中是否有错误信息

### 奶龙不回复
- 使用 `/nailong config` 检查配置状态
- 确保所有必需项都已配置
- 检查网络连接是否正常

### API调用失败
- 确认API服务可访问
- 检查API密钥是否有足够的额度
- 查看游戏日志了解具体错误

## 模型选择建议

- **gpt-5.6-luna**: 性价比高，响应快，推荐日常使用
- **gpt-5.6-terra**: 更智能，对话质量更高，但成本较高
- **deepseek-flash**: 经济实惠的选择
- 本地模型：零成本，但需要自己部署服务

## 更新日志

- OpenAI兼容API
- 优化系统提示词，提升对话质量
- 添加配置检查和友好提示
- 对话历史管理
