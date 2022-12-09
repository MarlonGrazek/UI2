package com.marlongrazek.ui;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public final class UI {

    private final Plugin plugin;
    private final Player player;

    public UI(Plugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public void open() {

    }

    public void close() {

    }

    public void update() {

    }

    public static class Page {

        private String title;
        private int size;
        private final Map<Integer, Item> items = new HashMap<>();
        private boolean preventClose = false;
        private Consumer<Player> openAction;
        private Consumer<Player> closeAction;

        public Page(String title, int size) {
            this.title = title;
            this.size = size;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public String getTitle() {
            return title;
        }

        public int getSize() {
            return size;
        }

        public void clear() {
            this.items.clear();
        }

        public void setItem(Item item, int slot) {
            items.put(slot, item);
        }

        public void addItem(Item item) {

            // FIRST ITEM
            if (this.items.isEmpty()) {
                items.put(0, item);
                return;
            }

            // ANY ITEM
            for (int i = 0; i < this.items.size(); i++) {
                if (this.items.containsKey(i)) continue;
                items.put(i, item);
                break;
            }
        }

        public Map<Integer, Item> getItems() {
            return items;
        }

        public Item getItem(int slot) {
            return items.get(slot);
        }

        public void preventClose(boolean preventClose) {
            this.preventClose = preventClose;
        }

        public boolean isPreventingClose() {
            return preventClose;
        }

        public void onOpen(Consumer<Player> openAction) {
            this.openAction = openAction;
        }

        public void onClose(Consumer<Player> closeAction) {
            this.closeAction = closeAction;
        }

        public Consumer<Player> getOpenAction() {
            return openAction;
        }

        public Consumer<Player> getCloseAction() {
            return closeAction;
        }
    }

    public static class Item {

    }
}
