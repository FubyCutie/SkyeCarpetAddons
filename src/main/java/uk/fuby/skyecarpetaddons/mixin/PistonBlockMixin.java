package uk.fuby.skyecarpetaddons.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PistonBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import uk.fuby.skyecarpetaddons.Options;

@Mixin(PistonBlock.class)
public abstract class PistonBlockMixin {
	
	@Redirect(
			method = "isMovable",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z"
			)
	)
	private static boolean deepslateMovableRedirect(BlockState state, Block block) {
		if(Options.movableReinforcedDeepslate && state.isOf(Blocks.REINFORCED_DEEPSLATE)) {
			return false;
		}
		return state.isOf(block);
	}
	
}
