package com.gugugaga233.dysoncubeprojectaddon.mixin.client;

import com.buuz135.dysoncubeproject.block.tile.EMRailEjectorBlockEntity;
import com.gugugaga233.dysoncubeprojectaddon.client.gui.DysonProgressGuiAddon;
import com.gugugaga233.dysoncubeprojectaddon.client.gui.SubscribeDysonGuiAddon;
import com.hrznstudio.titanium.api.IFactory;
import com.hrznstudio.titanium.api.client.IScreenAddon;
import java.util.ArrayList;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EMRailEjectorBlockEntity.class)
public abstract class EMRailEjectorScreenMixin {
    @Inject(method = "getScreenAddons", at = @At("HEAD"), cancellable = true)
    private void dcpAddon$screenAddons(
            CallbackInfoReturnable<List<IFactory<? extends IScreenAddon>>> cir) {
        EMRailEjectorBlockEntity self = (EMRailEjectorBlockEntity) (Object) this;
        List<IFactory<? extends IScreenAddon>> addons = new ArrayList<>();
        addons.addAll(self.getInput().getScreenAddons());
        addons.add(() -> new DysonProgressGuiAddon(self.getDysonSphereId(), 62, 24));
        addons.add(() -> new SubscribeDysonGuiAddon(self.getDysonSphereId(), 9, 84));
        addons.addAll(self.getPower().getScreenAddons());
        addons.addAll(self.getProgressBarComponent().getScreenAddons());
        cir.setReturnValue(addons);
    }
}
