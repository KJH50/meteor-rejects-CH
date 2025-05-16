package anticope.rejects.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;

public class ClearChatCommand extends Command {
    public ClearChatCommand() {
        super("clear-chat", "清除聊天记录。", "clear", "cls");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            mc.inGameHud.getChatHud().clear(false);
            return SINGLE_SUCCESS;
        });
    }
}