package rogo.renderingculling.mixin.fabric;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rogo.renderingculling.api.CullingHandler;
import rogo.renderingculling.api.ModLoader;

@Mixin(TooltipRenderUtil.class)
public class MixinTooltipRenderUtil {

    @ModifyConstant(
            method = "renderTooltipBackground",
            constant = @Constant(intValue = -267386864)
    )
    private static int onBG(int constant) {
        if (CullingHandler.reColorToolTip) {
            return ModLoader.getBG();
        }
        return constant;
    }

    @ModifyConstant(
            method = "renderTooltipBackground",
            constant = @Constant(intValue = 1347420415)
    )
    private static int onBT(int constant) {
        if (CullingHandler.reColorToolTip) {
            return ModLoader.getB();
        }
        return constant;
    }

    @ModifyConstant(
            method = "renderTooltipBackground",
            constant = @Constant(intValue = 1344798847)
    )
    private static int onBB(int constant) {
        if (CullingHandler.reColorToolTip) {
            return ModLoader.getB();
        }
        return constant;
    }
}
