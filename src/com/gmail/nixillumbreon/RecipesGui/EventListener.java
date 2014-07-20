package com.gmail.nixillumbreon.RecipesGui;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class EventListener implements Listener {
  @EventHandler
  public void invClick(InventoryClickEvent event) {
    Inventory inv = event.getInventory();
    Inventories invs = Inventories.getLatest();
    if (invs.craftingHas(inv)) {
      event.setResult(Result.DENY);
      return;
    }
    int pNum = invs.getPageNumber(inv);
    if (pNum != -1) {
      int slot = event.getSlot();
      if (slot < 54) {
        event.setResult(Result.DENY);
        Inventory nextInv = null;
        if (slot < 45) {
          int count = pNum * 45 + slot;
          nextInv = invs.getCraft(count);
        } else {
          nextInv = invs.getPage(slot - 45);
        }
        final HumanEntity ent = event.getWhoClicked();
        final Inventory nInv = nextInv; 
        if (nextInv != null) {
          new BukkitRunnable() {
            @Override
            public void run() {
              ent.closeInventory();
              ent.openInventory(nInv);
            }
          }.runTask(RecipesGui.getPlugin());
        }
      }
    }
  }
  
  @EventHandler
  public void interact(PlayerInteractEvent event) {
    if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
      if (event.hasItem()) {
        Material type = event.getItem().getType();
        if (type == Material.BOOK || type == Material.WRITTEN_BOOK) {
          ItemMeta meta = event.getItem().getItemMeta();
          String name = "";
          
          // Find out if it has a name, either as a title or a display name
          if (meta instanceof BookMeta) {
            BookMeta bMeta = (BookMeta) meta;
            if (bMeta.hasTitle()) name = bMeta.getTitle();
          } else if (meta.hasDisplayName()) name = meta.getDisplayName();
          
          if (name.equalsIgnoreCase("Recipe Book")) {
            event.setCancelled(true);
            event.getPlayer().openInventory(Inventories.getLatest().getPage(0));
          }
        }
      }
    }
  }
}
