/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.hrznstudio.titanium.api.client.AssetTypes
 *  com.hrznstudio.titanium.api.client.IAsset
 *  com.hrznstudio.titanium.client.screen.addon.BasicScreenAddon
 *  com.hrznstudio.titanium.client.screen.asset.IAssetProvider
 *  com.hrznstudio.titanium.network.Message
 *  com.hrznstudio.titanium.util.AssetUtil
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 */
package com.gugugaga233.dysoncubeprojectaddon.client.gui;

import com.gugugaga233.dysoncubeprojectaddon.DysonCubeProject;
import com.gugugaga233.dysoncubeprojectaddon.network.ClientSubscribeSphereMessage;
import com.gugugaga233.dysoncubeprojectaddon.network.ClientUnsubscribeSphereMessage;
import net.minecraft.client.Minecraft;
import com.hrznstudio.titanium.api.client.AssetTypes;
import com.hrznstudio.titanium.api.client.IAsset;
import com.hrznstudio.titanium.client.screen.addon.BasicScreenAddon;
import com.hrznstudio.titanium.client.screen.asset.IAssetProvider;
import com.hrznstudio.titanium.network.Message;
import com.hrznstudio.titanium.util.AssetUtil;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class SubscribeDysonGuiAddon
extends BasicScreenAddon {
    private static boolean realtimeSubscriptionActive;
    private final String dysonID;
    private int guiX;
    private int guiY;

    public SubscribeDysonGuiAddon(String dysonID, int posX, int posY) {
        super(posX, posY);
        this.dysonID = dysonID;
    }

    public int getXSize() {
        return 16;
    }

    public int getYSize() {
        return 16;
    }

    public void drawForegroundLayer(GuiGraphics guiGraphics, Screen screen, IAssetProvider iAssetProvider, int guiX, int guiY, int mouseX, int mouseY, float partialTicks) {
    }

    public void drawBackgroundLayer(GuiGraphics guiGraphics, Screen screen, IAssetProvider assets, int guiX, int guiY, int mouseX, int mouseY, float partialTicks) {
        int x = guiX + this.getPosX();
        int y = guiY + this.getPosY();
        AssetUtil.drawAsset((GuiGraphics)guiGraphics, (Screen)screen, (IAsset)assets.getAsset(AssetTypes.BUTTON_SIDENESS_PULL), (int)x, (int)y);
        this.guiX = guiX;
        this.guiY = guiY;
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return super.isMouseOver(mouseX, mouseY);
    }

    public List<Component> getTooltipLines() {
        return List.of(Component.translatable((String)"gui.dysoncubeproject.subscribe"));
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isMouseOver(mouseX - (double)this.guiX, mouseY - (double)this.guiY)) {
            realtimeSubscriptionActive = true;
            DysonCubeProject.NETWORK.sendToServer((Message)new ClientSubscribeSphereMessage(this.dysonID));
            return true;
        }
        return false;
    }

    /** Called when the client closes the screen that owns this addon. */
    public static void unsubscribeActive() {
        if (!realtimeSubscriptionActive) return;
        realtimeSubscriptionActive = false;
        if (Minecraft.getInstance().getConnection() != null) {
            DysonCubeProject.NETWORK.sendToServer((Message)new ClientUnsubscribeSphereMessage());
        }
    }
}


