package com.github.crystal0404.permissionapifix.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "me.lucko.fabric.api.permissions.v0.Util")
public abstract class PermissionAPIUtilMixin {
    @Inject(
            method = "commandSourceFromEntity",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void getServerCommandSource(Entity entity, CallbackInfoReturnable<ServerCommandSource> cir) {
        cir.setReturnValue(entity.getCommandSource());
    }
}
