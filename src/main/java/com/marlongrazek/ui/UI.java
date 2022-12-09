package com.marlongrazek.ui;

import com.marlongrazek.builder.ItemStackBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.function.Consumer;

public final class UI {

    private final Plugin plugin;
    private final Player player;
    private Inventory inventory;
    private Consumer<Player> openAction;
    private Consumer<Player> closeAction;
    private final Events events = new Events();
    boolean open = false;

    private final List<Page> history = new ArrayList<>();

    public UI(Plugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public void open() {
        Bukkit.getPluginManager().registerEvents(events, plugin);
        open = true;
        if (openAction != null) openAction.accept(player);
        player.openInventory(inventory);
    }

    private void openPage(Page newPage) {
        Page previousPage = getPageFromHistory(0);
        inventory = Bukkit.createInventory(null, newPage.getSize(), newPage.getTitle());
        newPage.getItems().keySet().forEach(slot -> inventory.setItem(slot, newPage.getItems().get(slot).toItemStack()));
        if (open) player.openInventory(inventory);
        if (previousPage != null && previousPage.getCloseAction() != null) previousPage.getCloseAction().accept(player);
        if (newPage.getOpenAction() != null) newPage.getOpenAction().accept(player);
    }

    public void setPage(Page page) {
        openPage(page);
        if (history.isEmpty() || !getPageFromHistory(0).equals(page)) history.add(page);
    }

    public void setPageFromHistory(int index) {

        // HISTORY PAGE IS NULL
        if(getPageFromHistory(index) == null) {
            player.closeInventory();
            return;
        }

        // SET PAGE
        openPage(getPageFromHistory(index));
        for (int i = 0; i < index; i++) history.remove(history.size() - 1);
    }

    public Page getPageFromHistory(int index) {

        // HISTORY PAGE IS NULL
        if (history.isEmpty() || history.size() - 1 < index) return null;

        // PAGE FROM HISTORY
        return history.get(history.size() - 1 - index);
    }

    private void close() {
        HandlerList.unregisterAll(events);
        open = false;
        if (closeAction != null) closeAction.accept(player);
        history.clear();
    }

    public boolean isOpen() {
        return open;
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
        if (page.getUpdateAction() != null) page.getUpdateAction().accept(player);
    }

    public class Events implements Listener {

        @EventHandler
        public void onClose(InventoryCloseEvent e) {

            if (inventory == null || inventory != e.getInventory()) return;

            Player player = (Player) e.getPlayer();
            Page page = getPageFromHistory(0);

            // PREVENT CLOSE
            if (page.preventClose) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> player.openInventory(inventory), 1);
                return;
            }

            if (page.getCloseAction() != null) page.getCloseAction().accept(player);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.getOpenInventory().getTopInventory() != inventory) close();
            }, 1);
        }

        @EventHandler
        public void onClick(InventoryClickEvent e) {

            if (inventory == null) return;

            Page page = history.get(history.size() - 1);

            if (e.getInventory() != inventory) return;
            if (e.getView().getTopInventory() != e.getClickedInventory()) return;

            for (Item item : page.getItems().values()) {
                if (item == null || e.getCurrentItem() == null) continue;
                if (!e.getCurrentItem().equals(item.toItemStack())) continue;
                e.setCancelled(true);
                if (item.getClickAction() != null) item.getClickAction().accept(e.getClick());
                break;
            }
        }

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

        private int amount = 1;
        private Consumer<ClickType> clickAction;
        private Map<Enchantment, Integer> enchantments = new HashMap<>();
        private List<ItemFlag> itemFlags = new ArrayList<>();
        private ItemMeta itemMeta;
        private List<String> lore = new ArrayList<>();
        private Material material;
        private String name;

        public Item() {
        }

        public Item(String name) {
            this.name = name;
        }

        public Item(Material material) {
            this.material = material;
        }

        public Item(String name, Material material) {
            this.name = name;
            this.material = material;
        }

        public void addEnchantment(Enchantment enchantment, Integer level) {
            enchantments.put(enchantment, level);
        }

        public void addGlow() {
            addEnchantment(Enchantment.ARROW_DAMAGE, 1);
            addItemFlag(ItemFlag.HIDE_ENCHANTS);
        }

        public void addItemFlag(ItemFlag itemFlag) {
            itemFlags.add(itemFlag);
        }

        public void addLoreLines(String... lines) {
            Collections.addAll(lore, lines);
        }

        public void clearLore() {
            this.lore.clear();
        }

        public static Item fromItemStack(ItemStack itemStack) {
            ItemMeta itemMeta;
            Material material;
            Item item = new Item();

            if (itemStack.getItemMeta() != null) {
                itemMeta = itemStack.getItemMeta();
                material = itemStack.getType();
                item.setItemMeta(itemMeta);
                item.setMaterial(material);
            }
            return item;
        }

        public int getAmount() {
            return amount;
        }

        public Consumer<ClickType> getClickAction() {
            return this.clickAction;
        }

        public Map<Enchantment, Integer> getEnchantments() {
            return enchantments;
        }

        public List<ItemFlag> getItemFlags() {
            return itemFlags;
        }

        public ItemMeta getItemMeta() {
            return this.itemMeta;
        }

        public List<String> getLore() {
            return this.lore;
        }

        public Material getMaterial() {
            return material;
        }

        public String getName() {
            return name;
        }

        public void onClick(Consumer<ClickType> clickAction) {
            this.clickAction = clickAction;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }

        public void setEnchantments(HashMap<Enchantment, Integer> enchantments) {
            this.enchantments = enchantments;
        }

        public void setItemFlags(List<ItemFlag> itemFlags) {
            this.itemFlags = itemFlags;
        }

        public void setItemMeta(ItemMeta meta) {
            this.itemMeta = meta;
        }

        public void setLore(List<String> lore) {
            this.lore = lore;
        }

        public void setLore(String... lore) {
            this.lore = Arrays.asList(lore);
        }

        public void setLoreLine(String line, int index) {
            this.lore.set(index, line);
        }

        public void setMaterial(Material material) {
            this.material = material;
        }

        public void setName(String name) {
            this.name = name;
        }

        public ItemStack toItemStack() {

            ItemStackBuilder itemStack = new ItemStackBuilder(this.material);

            if (itemMeta != null) itemStack.setItemMeta(this.itemMeta);
            itemStack.setName(this.name);
            itemStack.setLore(new ArrayList<>(this.lore));
            itemStack.setItemFlags(new ArrayList<>(itemFlags));
            itemStack.setEnchantments(new HashMap<>(enchantments));
            itemStack.setAmount(amount);
            return itemStack.toItemStack();
        }
    }
}
