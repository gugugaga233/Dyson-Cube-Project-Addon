package com.gugugaga233.dysoncubeprojectaddon.overhaul.client.gui;

import com.gugugaga233.dysoncubeprojectaddon.DysonCubeProject;
import com.gugugaga233.dysoncubeprojectaddon.network.ServerboundCosmicHeartRewardPacket;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.CosmicHeart;
import com.gugugaga233.dysoncubeprojectaddon.overhaul.CosmicHeartRewardCount;
import com.hrznstudio.titanium.network.Message;
import java.math.BigInteger;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Plans current and future Cosmic Heart rewards in one interaction. */
public class CosmicHeartRewardScreen extends Screen {
    private static final int GUI_WIDTH = 560;
    private static final int GUI_HEIGHT = 330;
    private static final int ROW_HEIGHT = 29;

    private final String[] appliedCounts = new String[CosmicHeart.RuleModification.values().length];
    private final String requiredChoices;
    private final boolean requiredCountValid;
    private final EditBox[] countInputs = new EditBox[CosmicHeart.RuleModification.values().length];
    private final Button[] minusButtons = new Button[CosmicHeart.RuleModification.values().length];
    private final Button[] maxButtons = new Button[CosmicHeart.RuleModification.values().length];
    private final Button[] plusButtons = new Button[CosmicHeart.RuleModification.values().length];
    private Button confirmButton;
    private boolean hasSent;

    public CosmicHeartRewardScreen(String serializedAppliedCounts) {
        this(serializedAppliedCounts, "1");
    }

    public CosmicHeartRewardScreen(String serializedAppliedCounts, String requiredChoices) {
        super(Component.translatable("gui.dysoncubeproject.cosmic_heart.reward.title"));
        parseCounts(serializedAppliedCounts, this.appliedCounts);
        this.requiredChoices = requiredChoices == null || requiredChoices.isBlank()
                ? "1" : requiredChoices;
        this.requiredCountValid = isPositiveCount(this.requiredChoices);
    }

    @Override
    protected void init() {
        int guiLeft = (this.width - GUI_WIDTH) / 2;
        int guiTop = (this.height - GUI_HEIGHT) / 2;

        CosmicHeart.RuleModification[] rules = CosmicHeart.RuleModification.values();
        int startY = guiTop + 64;
        for (int i = 0; i < rules.length; i++) {
            final int index = i;
            int y = startY + i * ROW_HEIGHT;
            this.minusButtons[i] = addRenderableWidget(Button.builder(
                    Component.literal("-"), button -> changeSelection(index, -1))
                    .bounds(guiLeft + 397, y + 3, 24, 22).build());
            this.maxButtons[i] = addRenderableWidget(Button.builder(
                    Component.translatable("gui.dysoncubeproject.cosmic_heart.reward.max"),
                    button -> selectMaximum(index))
                    .bounds(guiLeft + 358, y + 3, 35, 22).build());
            EditBox input = new EditBox(this.font, guiLeft + 424, y + 5, 90, 18,
                    Component.translatable("gui.dysoncubeproject.cosmic_heart.reward.count"));
            input.setMaxLength(CosmicHeartRewardCount.MAX_INPUT_CHARACTERS);
            input.setFilter(CosmicHeartRewardScreen::isPotentialCount);
            input.setValue("0");
            input.setResponder(ignored -> refreshButtons());
            this.countInputs[i] = addRenderableWidget(input);
            this.plusButtons[i] = addRenderableWidget(Button.builder(
                    Component.literal("+"), button -> changeSelection(index, 1))
                    .bounds(guiLeft + 517, y + 3, 24, 22).build());
        }

        this.confirmButton = addRenderableWidget(Button.builder(
                Component.translatable("gui.dysoncubeproject.cosmic_heart.reward.confirm", 0),
                button -> submitPlan())
                .bounds(guiLeft + 158, guiTop + GUI_HEIGHT - 32, 170, 22).build());
        Button waitingButton = addRenderableWidget(Button.builder(
                Component.translatable("gui.dysoncubeproject.cosmic_heart.reward.choose_required"),
                button -> { })
                .bounds(guiLeft + 336, guiTop + GUI_HEIGHT - 32, 140, 22).build());
        waitingButton.active = false;
        refreshButtons();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        int guiLeft = (this.width - GUI_WIDTH) / 2;
        int guiTop = (this.height - GUI_HEIGHT) / 2;

        graphics.fill(guiLeft, guiTop, guiLeft + GUI_WIDTH, guiTop + GUI_HEIGHT, 0xEC111820);
        graphics.renderOutline(guiLeft, guiTop, GUI_WIDTH, GUI_HEIGHT, 0xFFFFD700);

        CosmicHeart.RuleModification[] rules = CosmicHeart.RuleModification.values();
        int startY = guiTop + 55;
        for (int i = 0; i < rules.length; i++) {
            int y = startY + i * ROW_HEIGHT;
            graphics.fill(guiLeft + 10, y, guiLeft + GUI_WIDTH - 10, y + 28,
                    i % 2 == 0 ? 0xA8242B33 : 0xA81D242C);
            graphics.renderOutline(guiLeft + 10, y, GUI_WIDTH - 20, 28, 0xFF394652);
        }

        // Render native widgets before custom text so the font draw state is reset
        // after the background blur pass used by Screen.renderBackground.
        super.render(graphics, mouseX, mouseY, partialTick);

        Component title = Component.translatable("gui.dysoncubeproject.cosmic_heart.reward.title");
        graphics.drawString(this.font, title,
                guiLeft + (GUI_WIDTH - this.font.width(title)) / 2, guiTop + 10, 0xFFFFD700, false);
        graphics.drawString(this.font,
                Component.translatable("gui.dysoncubeproject.cosmic_heart.reward.desc"),
                guiLeft + 14, guiTop + 31, 0xFFC8D1DC, false);
        graphics.drawString(this.font,
                Component.translatable("gui.dysoncubeproject.cosmic_heart.reward.required",
                        this.requiredChoices),
                guiLeft + 14, guiTop + 43, 0xFFFFD166, false);
        Component hoveredRule = null;
        for (int i = 0; i < rules.length; i++) {
            int y = startY + i * ROW_HEIGHT;

            Component ruleName = Component.translatable(
                    "gui.dysoncubeproject.cosmic_heart.reward.rule." + i);
            int color = !rules[i].repeatable && isNonZero(this.appliedCounts[i])
                    ? 0xFF7F8A94 : 0xFFF0F3F6;
            drawFitted(graphics, ruleName, guiLeft + 18, y + 6, 330, color);
            if (mouseX >= guiLeft + 10 && mouseX < guiLeft + 390
                    && mouseY >= y && mouseY < y + 28) {
                hoveredRule = ruleName;
            }
            if (isNonZero(this.appliedCounts[i])) {
                graphics.drawString(this.font, Component.translatable(
                                "gui.dysoncubeproject.cosmic_heart.reward.applied",
                                this.appliedCounts[i]),
                        guiLeft + 18, y + 16, 0xFF7FD6A3, false);
            }
        }
        if (hoveredRule != null) graphics.renderTooltip(this.font, hoveredRule, mouseX, mouseY);
    }

    private void changeSelection(int index, int delta) {
        CosmicHeart.RuleModification rule = CosmicHeart.RuleModification.values()[index];
        if (!rule.repeatable && isNonZero(this.appliedCounts[index])) return;
        BigInteger current;
        try {
            current = parseCount(this.countInputs[index].getValue());
        } catch (IllegalArgumentException exception) {
            current = BigInteger.ZERO;
        }
        BigInteger next = current.add(BigInteger.valueOf(delta)).max(BigInteger.ZERO);
        if (!rule.repeatable && next.compareTo(BigInteger.ONE) > 0) next = BigInteger.ONE;
        if (next.toString().length() <= CosmicHeartRewardCount.MAX_DECIMAL_DIGITS) {
            this.countInputs[index].setValue(next.toString());
        }
        refreshButtons();
    }

    /** Assigns the whole current settlement to one repeatable reward. */
    private void selectMaximum(int index) {
        CosmicHeart.RuleModification rule = CosmicHeart.RuleModification.values()[index];
        boolean unavailable = !rule.repeatable && isNonZero(this.appliedCounts[index]);
        if (this.hasSent || unavailable) return;

        if (!this.requiredCountValid) return;
        for (int i = 0; i < this.countInputs.length; i++) {
            this.countInputs[i].setValue(i == index ? this.requiredChoices : "0");
        }
        refreshButtons();
    }

    private void refreshButtons() {
        CosmicHeart.RuleModification[] rules = CosmicHeart.RuleModification.values();
        BigInteger total = BigInteger.ZERO;
        boolean valid = true;
        for (int i = 0; i < rules.length; i++) {
            boolean unavailable = !rules[i].repeatable && isNonZero(this.appliedCounts[i]);
            this.countInputs[i].active = !unavailable;
            BigInteger count;
            try {
                count = parseCount(this.countInputs[i].getValue());
                if (!rules[i].repeatable && count.compareTo(BigInteger.ONE) > 0) valid = false;
                total = total.add(count);
            } catch (IllegalArgumentException exception) {
                count = BigInteger.ZERO;
                valid = false;
            }
            this.minusButtons[i].active = !unavailable && count.signum() > 0;
            this.maxButtons[i].active = !unavailable && rules[i].repeatable
                    && !this.hasSent && this.requiredCountValid;
            this.plusButtons[i].active = !unavailable;
        }
        this.confirmButton.active = valid && total.signum() > 0 && !this.hasSent;
        this.confirmButton.setMessage(Component.translatable(
                "gui.dysoncubeproject.cosmic_heart.reward.confirm", compact(total)));
    }

    private static boolean isPositiveCount(String value) {
        try {
            return parseCount(value).signum() > 0;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private void submitPlan() {
        if (this.hasSent || !this.confirmButton.active) return;
        this.hasSent = true;
        refreshButtons();
        StringBuilder serialized = new StringBuilder();
        for (int i = 0; i < this.countInputs.length; i++) {
            if (i > 0) serialized.append(';');
            serialized.append(this.countInputs[i].getValue());
        }
        DysonCubeProject.NETWORK.sendToServer(
                (Message) new ServerboundCosmicHeartRewardPacket(serialized.toString()));
        onClose();
    }

    private static void parseCounts(String serialized, String[] destination) {
        java.util.Arrays.fill(destination, "0");
        if (serialized == null) return;
        String[] parts = serialized.split(",", -1);
        if (parts.length != destination.length) return;
        System.arraycopy(parts, 0, destination, 0, parts.length);
    }

    private static BigInteger parseCount(String input) {
        return CosmicHeartRewardCount.parseBigInteger(input);
    }

    private static boolean isPotentialCount(String value) {
        if (value == null || value.length() > CosmicHeartRewardCount.MAX_INPUT_CHARACTERS) return false;
        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            if (!Character.isDigit(character) && character != 'e' && character != 'E'
                    && character != '+' && character != '.') return false;
        }
        return true;
    }

    private static boolean isNonZero(String value) {
        return value != null && !value.equals("0") && !value.isBlank();
    }

    private static String compact(BigInteger value) {
        String decimal = value.toString();
        if (decimal.length() <= 12) return decimal;
        return decimal.substring(0, 4) + "E" + (decimal.length() - 1);
    }

    private void drawFitted(GuiGraphics graphics, Component text, int x, int y,
                            int maximumWidth, int color) {
        if (this.font.width(text) <= maximumWidth) {
            graphics.drawString(this.font, text, x, y, color, false);
            return;
        }
        String suffix = "...";
        String clipped = this.font.plainSubstrByWidth(
                text.getString(), maximumWidth - this.font.width(suffix));
        graphics.drawString(this.font, Component.literal(clipped + suffix),
                x, y, color, false);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // The server keeps this player protected until a valid reward is chosen.
        if (keyCode == 256) return true;
        if (keyCode == 257 && this.confirmButton.active) {
            submitPlan();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        // In singleplayer this prevents the world from ticking behind the mandatory choice.
        // Multiplayer remains protected by the server-side selection guard.
        return true;
    }
}
