package io.github.zap.arenaapi;

import io.github.zap.arenaapi.event.Event;
import io.github.zap.arenaapi.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public abstract class ReloadablePlugin extends JavaPlugin {
    private final Event<ReloadablePlugin> loadEvent;
    private final Event<ReloadablePlugin> enableEvent;
    private final Event<ReloadablePlugin> disableEvent;

    protected ReloadablePlugin() {
        this.loadEvent = new Event<>();
        this.enableEvent = new Event<>();
        this.disableEvent = new Event<>();
    }

    @Override
    public final void onLoad() {
        super.onLoad();
        loadEvent.callEvent(this);
    }

    @Override
    public final void onEnable() {
        super.onEnable();
        enableEvent.callEvent(this);
    }

    @Override
    public final void onDisable() {
        super.onDisable();
        disableEvent.callEvent(this);
    }

    public void registerLoadHandler(@NotNull EventHandler<ReloadablePlugin> handler) {
        loadEvent.registerHandler(handler);
    }

    public void registerEnableHandler(@NotNull EventHandler<ReloadablePlugin> handler) {
        enableEvent.registerHandler(handler);
    }

    public void registerDisableHandler(@NotNull EventHandler<ReloadablePlugin> handler) {
        disableEvent.registerHandler(handler);
    }
}
