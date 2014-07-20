package com.gmail.nixillumbreon.RecipesGui;

import org.bukkit.plugin.java.JavaPlugin;

public class RecipesGui extends JavaPlugin {
  private static RecipesGui plugin;
  
  public void onEnable() {
    plugin = this;
    new Inventories(); // Regenerate inventories
    getServer().getPluginManager().registerEvents(new EventListener(), this);
  }
  
  public static RecipesGui getPlugin() {
    return plugin;
  }
}
