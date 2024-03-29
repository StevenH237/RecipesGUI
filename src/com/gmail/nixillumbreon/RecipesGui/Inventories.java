package com.gmail.nixillumbreon.RecipesGui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Wool;

public class Inventories {
  private static Inventories latestInstance;
    public static Inventories getLatest() { return latestInstance; }
  
  private ArrayList<Inventory> pages;
    public boolean pagesHas(Inventory i) { return pages.contains(i); }
    public Inventory getPage(int i) { return pages.get(i); }
    public int getPageNumber(Inventory i) { return pages.indexOf(i); }
  private ArrayList<Inventory> crafting;
    public boolean craftingHas(Inventory i) { return crafting.contains(i); }
    public Inventory getCraft(int i) { if (i < crafting.size()) return crafting.get(i); else return null; }
    public int getCraftNumber(Inventory i) { return crafting.indexOf(i); }
  
  public Inventories() {
    // When this class is created, iterate through all recipes and set up the inventories.
    // This class should not be called more than once, but some plugins may have reason to.
    // These inventories are expected to be unmodified, and those generated by the latest instance will be protected.
    
    // This is the most recent instance of Inventories.
    latestInstance = this;
    
    // This grabs the iterator through recipes.
    Iterator<Recipe> iter = Bukkit.recipeIterator();
    
    // This sets up a multi-use count variable.
    int count = 0;
    
    // Initialize pages and crafting pages.
    pages = new ArrayList<Inventory>(9);
    crafting = new ArrayList<Inventory>(405);
    
    // This uses the count to count pages, creating one for each page tab at the bottom.
    for (; count < 9; count++) {
      Inventory inv = (Inventory) Bukkit.createInventory(null, 54, "Recipes: Page " + String.valueOf(count + 1)); // Create an empty double-chest inventory with the name "Recipes: Page _"
      pages.add(inv); // Add it to the list
    }
    
    // Reset the count variable and begin a loop that breaks when there are no more recipes
    // 405 is 9 (pages) times 5 (rows) times 9 (columns).
    for (count = 0; iter.hasNext() && count < 405; count++) {
      // Get one recipe
      Recipe rec = iter.next();
      
      // Find its result
      ItemStack result = rec.getResult();
      
      // Create variable for inventory to add
      Inventory craftingInventory = null;
      
      // Create inventory based on type of recipe
      if (rec instanceof ShapelessRecipe) craftingInventory = shapelessInv((ShapelessRecipe) rec);
      else if (rec instanceof ShapedRecipe) craftingInventory = shapedInv((ShapedRecipe) rec);
      else if (rec instanceof FurnaceRecipe) craftingInventory = furnaceInv((FurnaceRecipe) rec);
      else {
        // No recipe found, perhaps new type of recipe was added? Add an update notice.
        ItemMeta meta = result.getItemMeta();
        List<String> lore;
        if (meta.hasLore()) lore = meta.getLore();
        else lore = new ArrayList<String>(2);
        lore.add("This type of recipe is unknown.");
        lore.add("Perhaps the RecipeBook plugin is outdated.");
        meta.setLore(lore);
        result.setItemMeta(meta);
      }
      
      // Add inventory to list of crafting recipe inventories.
      crafting.add(craftingInventory);
      
      // Add item to the proper slot of the main pages.
      int page = count / 45;
      int slot = count % 45;
      
      // Pick out that inventory
      Inventory inv = pages.get(page);
      
      // Set the item in that inventory
      inv.setItem(slot, result);
    }
    
    // Part 2 - on every page add wool blocks
    ItemStack whiteBlock = new Wool(DyeColor.WHITE).toItemStack();
    ItemStack blackBlock = new Wool(DyeColor.BLACK).toItemStack();
    
    ItemMeta woolMeta = whiteBlock.getItemMeta();
    
    ArrayList<String> onPageLore = new ArrayList<String>(1);
    onPageLore.add("You are on this page.");
    
    ArrayList<String> offPageLore = new ArrayList<String>(1);
    
    for (int page = 0; page < 9; page++) {
      Inventory inv = pages.get(page);
      for (count = 0; count < 9; count++) {
        woolMeta.setDisplayName("Page " + String.valueOf(count + 1));
        if (page == count) {
          woolMeta.setLore(onPageLore);
          blackBlock.setItemMeta(woolMeta);
          blackBlock.setAmount(count + 1);
          inv.setItem(count + 45, blackBlock);
        } else {
          woolMeta.setLore(offPageLore);
          whiteBlock.setItemMeta(woolMeta);
          whiteBlock.setAmount(count + 1);
          inv.setItem(count + 45, whiteBlock);
        }
      }
    }
  }

  private Inventory shapedInv(ShapedRecipe rec) {
    ItemStack result = rec.getResult();
    
    String title = "(Shaped) Recipe for " + result.getType().toString().replace('_', ' ');
    if (title.length() > 32) title = title.substring(0, 29) + "...";
    
    Inventory resInv = (Inventory) Bukkit.createInventory(null, InventoryType.WORKBENCH, title);
    resInv.setItem(0, result);
    
    // Get shape of recipe
    // Assumed to be roughly [ab, cd, ef]
    // equivalent to
    // [[ab ]
    //  [cd ]
    //  [ef ]]
    String[] shape = rec.getShape();
    
    // Flatten shape to correspond to nine characters
    String crst = "";
    for (String s : shape) {
      while (s.length() < 3) {
        s += " "; // pad single line to length 3
      }
      crst += s;
    }
    while (crst.length() < 9) {
      crst += " "; // pad entire line to length 9
    }
    String str = crst;
    
    // Create a CharArray from the above string
    char[] chs = str.toCharArray();
    
    // Get the "legend" of ingredients, mapping one symbol to an ingredient
    Map<Character, ItemStack> ing = rec.getIngredientMap();
    
    // Add the ingredients
    for (int slot = 0; slot < 9; slot++) {
      Character ch = chs[slot];
      if (ing.containsKey(ch)) resInv.setItem(slot + 1, ing.get(ch));
    }
    
    // Return this inventory
    return resInv;
  }

  private Inventory shapelessInv(ShapelessRecipe rec) {
    ItemStack result = rec.getResult();
    
    String title = "(Unshaped) Recipe for " + result.getType().toString().replace('_', ' ');
    if (title.length() > 32) title = title.substring(0, 29) + "...";

    Inventory resInv = Bukkit.createInventory(null, InventoryType.WORKBENCH, title);
    resInv.setItem(0, result);
    
    List<ItemStack> items = rec.getIngredientList();
    Iterator<ItemStack> iter = items.iterator();
    for (int a = 1; iter.hasNext(); a++) {
      resInv.setItem(a, iter.next());
    }
    
    return resInv;
  }

  private Inventory furnaceInv(FurnaceRecipe rec) {
    ItemStack result = rec.getResult();
    
    String title = "(Furnace) Recipe for " + result.getType().toString().replace('_', ' ');
    if (title.length() > 32) title = title.substring(0, 29) + "...";
    
    Inventory resInv = (Inventory) Bukkit.createInventory(null, InventoryType.FURNACE, "Recipe for " + result.getType().toString());
    resInv.setItem(0, result);
    resInv.setItem(1, rec.getInput());
    
    return resInv;
  }

}
