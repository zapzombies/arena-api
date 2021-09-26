package io.github.zap.arenaapi;

import io.github.zap.arenaapi.event.Event;
import io.github.zap.arenaapi.event.EventHandler;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public abstract class ReloadablePlugin<T extends ReloadablePlugin<T>> extends JavaPlugin {
    private final Event<T> loadEvent = new Event<>();
    private final Event<T> enableEvent = new Event<>();
    private final Event<T> disableEvent = new Event<>();

    @Override
    public final void onLoad() {
        super.onLoad();
        loadEvent.callEvent(getPlugin());
    }

    @Override
    public final void onEnable() {
        super.onEnable();
        enableEvent.callEvent(getPlugin());
    }

    @Override
    public final void onDisable() {
        super.onDisable();
        disableEvent.callEvent(getPlugin());
    }

    public void registerLoadHandler(@NotNull EventHandler<T> handler) {
        loadEvent.registerHandler(handler);
    }

    public void registerEnableHandler(@NotNull EventHandler<T> handler) {
        enableEvent.registerHandler(handler);
    }

    public void registerDisableHandler(@NotNull EventHandler<T> handler) {
        disableEvent.registerHandler(handler);
    }

    protected abstract @NotNull T getPlugin();
}
