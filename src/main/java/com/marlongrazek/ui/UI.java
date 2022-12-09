package com.marlongrazek.ui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class UI {

    private final Plugin plugin;
    private final Player player;
    private Inventory inventory;
    private Consumer<Player> openAction;
    private Consumer<Player> closeAction;

    private final List<Page> history = new ArrayList<>();

    public UI(Plugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public void open() {
        player.openInventory(inventory);
        if(openAction != null) openAction.accept(player);
    }

    public void setPage(Page page) {
        inventory = Bukkit.createInventory(null, page.getSize(), page.getTitle());
        page.getItems().keySet().forEach(slot -> inventory.setItem(slot, page.getItems().get(slot).toItemStack()));
        if(page.getOpenAction() != null) page.getOpenAction().accept(player);
        history.add(page);
    }

    public void setPageFromHistory(int index) {

        // HISTORY PAGE IS NULL
        if(history.get(history.size() - 1 - index) == null) {
            close();
            return;
        }

        // SET PAGE
        Page page = history.get(history.size() - 1 - index);
        for(int i = 0; i < index; i++) history.remove(history.size() - 1);
        setPage(page);
    }

    public Page getPageFromHistory(int index) {

        // HISTORY PAGE IS NULL
        if(history.get(history.size() - 1 - index) == null) return null;

        // PAGE FROM HISTORY
        return history.get(history.size() - 1 - index);
    }

    public void close() {
        player.closeInventory();
        if(closeAction != null) this.closeAction.accept(player);
        history.clear();
    }

    public void onOpen(Consumer<Player> openAction) {
        this.openAction = openAction;
    }

    public void onClose(Consumer<Player> closeAction) {
        this.closeAction = closeAction;
    }

    public void update() {
        Page page = getPageFromHistory(0);
        page.getItems().keySet().forEach(slot -> inventory.setItem(slot, page.getItems().get(slot).toItemStack()));
        if(page.getUpdateAction() != null) page.getUpdateAction().accept(player);
    }

    public static class Page {

        private String title;
        private int size;
        private final Map<Integer, Item> items = new HashMap<>();
        private boolean preventClose = false;
        private Consumer<Player> openAction;
        private Consumer<Player> closeAction;
        private Consumer<Player> updateAction;

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

        public void onUpdate(Consumer<Player> updateAction) {
            this.updateAction = updateAction;
        }

        public Consumer<Player> getOpenAction() {
            return openAction;
        }

        public Consumer<Player> getCloseAction() {
            return closeAction;
        }

        public Consumer<Player> getUpdateAction() {
            return updateAction;
        }
    }

    public static class Item {

    }
}
