package com.gugugaga233.dysoncubeprojectaddon.network;

import com.gugugaga233.dysoncubeprojectaddon.world.DysonSphereProgressSavedData;
import com.gugugaga233.dysoncubeprojectaddon.world.DysonSphereStructure;
import com.hrznstudio.titanium.network.Message;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 客户端 → 服务端：鸿蒙之气道痕烙印选择
 *
 * @param choiceIndex 0=根源共鸣(基数×10), 1=法则刻印(规则简化), 2=道脉共鸣(输出×10)
 */
public class ServerboundPrimordialQiChoicePacket extends Message {
    public int choiceIndex;

    public ServerboundPrimordialQiChoicePacket() {
        this.choiceIndex = 0;
    }

    public ServerboundPrimordialQiChoicePacket(int choiceIndex) {
        this.choiceIndex = choiceIndex;
    }

    @Override
    protected void handleMessage(IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            DysonSphereProgressSavedData data = DysonSphereProgressSavedData.get(player.level());
            if (data == null) return;

            DysonSphereStructure target = data.getSpheres().get(player.getUUID().toString());
            if (target == null || !target.consumePrimordialQiChoicePending()) {
                target = data.getSpheres().values().stream()
                        .filter(DysonSphereStructure::hasPrimordialQiChoicePending)
                        .findFirst().orElse(null);
                if (target == null || !target.consumePrimordialQiChoicePending()) return;
            }
            target.applyPrimordialQiEffect();
            com.gugugaga233.dysoncubeprojectaddon.overhaul.PrimordialQiManager
                    .clearChoicePrompt(player);

            // 根据选择类型给予不同反馈（简化版：统一应用根源共鸣）
            switch (choiceIndex) {
                case 0 -> player.displayClientMessage(
                        Component.translatable("gui.dysoncubeproject.primordial_qi.choice_root"), true);
                case 1 -> player.displayClientMessage(
                        Component.translatable("gui.dysoncubeproject.primordial_qi.choice_law"), true);
                case 2 -> player.displayClientMessage(
                        Component.translatable("gui.dysoncubeproject.primordial_qi.choice_vein"), true);
                default -> player.displayClientMessage(
                        Component.translatable("gui.dysoncubeproject.primordial_qi.choice_default"), true);
            }
            data.setDirty();
        });
    }
}

