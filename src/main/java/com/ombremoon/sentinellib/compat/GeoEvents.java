package com.ombremoon.sentinellib.compat;

public class GeoEvents {

    private static GeoEvents instance;

    public static GeoEvents getInstance() {
        if (instance == null) {
            instance = new GeoEvents();
        }
        return instance;
    }

/*    @SubscribeEvent
    public void renderGeckolibSentinelBoxes(GeoRenderEvent.Entity.Post event) {
        Entity entity = event.getEntity();
        Minecraft minecraft = Minecraft.getInstance();

        if (entity.level() == null)
            return;

        if (entity instanceof ISentinel sentinel) {
            if (minecraft.getEntityRenderDispatcher().shouldRenderHitBoxes() && !minecraft.showOnlyReducedInfo()) {
                BoxInstanceManager manager = sentinel.getBoxManager();
                for (BoxInstance instance : manager.getInstances()) {
                    instance.getSentinelBox().renderBox(instance, entity, event.getPoseStack(), event.getBufferSource().getBuffer(RenderType.lines()), event.getPartialTick(), instance.isActive() ? 0.0F : 1.0F);
                }
            }

            if (event.getRenderer() instanceof ISentinelRenderer<?> renderer)
                renderer.trackSentinelModel(event.getModel());

            var s = sentinel.getBoxManager().getInstances();
            for (var m : s) {
                Constants.LOG.info(String.valueOf(m.getKey().getWorldPosition().x));
                Constants.LOG.info(String.valueOf(m.getCenter()));
            }
        }
    }*/
}
