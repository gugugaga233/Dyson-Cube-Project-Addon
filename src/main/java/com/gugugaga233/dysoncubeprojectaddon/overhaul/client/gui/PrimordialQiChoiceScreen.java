package com.gugugaga233.dysoncubeprojectaddon.overhaul.client.gui;

import com.gugugaga233.dysoncubeprojectaddon.DysonCubeProject;
import com.gugugaga233.dysoncubeprojectaddon.network.ServerboundPrimordialQiChoicePacket;
import com.hrznstudio.titanium.network.Message;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/**
 * 鸿蒙之气道痕烙印选择界面
 * <p>
 * 每 10 次触发鸿蒙之气时弹出，玩家选择道痕烙印方向。
 * 三个选项：
 * <ul>
 *   <li>根源共鸣 — 基数 ×10（当前实现：统一应用）</li>
 *   <li>法则刻印 — 物理常数修改（简化实现）</li>
 *   <li>道脉共鸣 — 输出 ×10（当前实现：统一应用）</li>
 * </ul>
 */
public class PrimordialQiChoiceScreen extends Screen {

    private static final int GUI_WIDTH = 280;
    private static final int GUI_HEIGHT = 200;

    private final String qiCount;
    @Nullable
    private Button rootButton;
    @Nullable
    private Button lawButton;
    @Nullable
    private Button veinButton;

    public PrimordialQiChoiceScreen(String qiCount) {
        super(Component.translatable("gui.dysoncubeproject.primordial_qi.title"));
        this.qiCount = qiCount;
    }

    @Override
    protected void init() {
        int guiLeft = (this.width - GUI_WIDTH) / 2;
        int guiTop = (this.height - GUI_HEIGHT) / 2;

        // 根源共鸣按钮
        this.rootButton = Button.builder(
                Component.translatable("gui.dysoncubeproject.primordial_qi.choice_root"),
                btn -> onSelect(0)
        ).bounds(guiLeft + 20, guiTop + 50, GUI_WIDTH - 40, 30)
         .build();
        this.addRenderableWidget(this.rootButton);

        // 法则刻印按钮
        this.lawButton = Button.builder(
                Component.translatable("gui.dysoncubeproject.primordial_qi.choice_law"),
                btn -> onSelect(1)
        ).bounds(guiLeft + 20, guiTop + 90, GUI_WIDTH - 40, 30)
         .build();
        this.addRenderableWidget(this.lawButton);

        // 道脉共鸣按钮
        this.veinButton = Button.builder(
                Component.translatable("gui.dysoncubeproject.primordial_qi.choice_vein"),
                btn -> onSelect(2)
        ).bounds(guiLeft + 20, guiTop + 130, GUI_WIDTH - 40, 30)
         .build();
        this.addRenderableWidget(this.veinButton);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int guiLeft = (this.width - GUI_WIDTH) / 2;
        int guiTop = (this.height - GUI_HEIGHT) / 2;

        // 背景
        guiGraphics.fill(guiLeft, guiTop, guiLeft + GUI_WIDTH, guiTop + GUI_HEIGHT, 0xCC111111);
        // 边框
        guiGraphics.renderOutline(guiLeft, guiTop, GUI_WIDTH, GUI_HEIGHT, 0xFFFFAA00);

        // 标题
        guiGraphics.drawString(this.font,
                Component.translatable("gui.dysoncubeproject.primordial_qi.title")
                        .append(Component.translatable("gui.dysoncubeproject.primordial_qi.layer", qiCount)),
                guiLeft + GUI_WIDTH / 2 - 80, guiTop + 10, 0xFFFFAA00, false);

        // 描述
        guiGraphics.drawString(this.font,
                Component.translatable("gui.dysoncubeproject.primordial_qi.desc"),
                guiLeft + 20, guiTop + 35, 0xCCCCCC, false);

        // 底部提示
        guiGraphics.drawString(this.font,
                Component.translatable("gui.dysoncubeproject.close_esc"),
                guiLeft + GUI_WIDTH / 2 - 40, guiTop + GUI_HEIGHT - 20, 0x888888, false);
    }

    private void onSelect(int choiceIndex) {
        DysonCubeProject.NETWORK.sendToServer((Message)new ServerboundPrimordialQiChoicePacket(choiceIndex));
        this.onClose();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256 || keyCode == 257) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}

