package rogo.renderingculling.mixin.fabric;

import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import rogo.renderingculling.api.CullingHandler;
import rogo.renderingculling.api.ModLoader;

@Mixin(Screen.class)
public class MixinScreen {

    @ModifyConstant(
            method = "renderTooltipInternal",
            constant = @Constant(intValue = -267386864)
    )
    private int onBG(int constant) {
        if (CullingHandler.reColorToolTip) {
            return ModLoader.getBG();
        }
        return constant;
    }

    @ModifyConstant(
            method = "renderTooltipInternal",
            constant = @Constant(intValue = 1347420415)
    )
    private int onBT(int constant) {
        if (CullingHandler.reColorToolTip) {
            return ModLoader.getB();
        }
        return constant;
    }

    @ModifyConstant(
            method = "renderTooltipInternal",
            constant = @Constant(intValue = 1344798847)
    )
    private int onBB(int constant) {
        if (CullingHandler.reColorToolTip) {
            return ModLoader.getB();
        }
        return constant;
    }
}
