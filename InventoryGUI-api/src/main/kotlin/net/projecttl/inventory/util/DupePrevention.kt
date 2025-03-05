package net.projecttl.inventory.util

import net.projecttl.inventory.InventoryGUI.plugin
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryPickupItemEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType


val PDC_KEY = NamespacedKey(plugin, "is_inventory_item")

class DupePrevention {
    companion object : Listener {
        private var registered = false

        fun isInventoryItem(item: ItemStack?): Boolean {
            return item?.itemMeta?.persistentDataContainer?.has(PDC_KEY, PersistentDataType.BOOLEAN) == true
        }

        fun setInventoryItem(item: ItemStack) {
            item.editMeta { it.persistentDataContainer.set(PDC_KEY, PersistentDataType.BOOLEAN, true) }
        }

        fun tryRegisterEvents() {
            if (!registered) {
                plugin.server.pluginManager.registerEvents(this, plugin)
                registered = true
            }
        }


        // Prevents the item from being picked up by blocks (like hoppers)
        @EventHandler
        fun onItemPickup(event: InventoryPickupItemEvent) {
            if (isInventoryItem(event.item.itemStack)) {
                event.isCancelled = true
                event.item.remove()
            }
        }

        // Prevents the item from being picked up by players or other entities
        @EventHandler
        fun onEntityItemPickup(event: EntityPickupItemEvent) {
            if (isInventoryItem(event.item.itemStack)) {
                event.isCancelled = true
                event.item.remove()
            }
        }

        // Prevents the item from being dropped by a player
        @EventHandler
        fun onItemDropped(event: PlayerDropItemEvent) {
            if (isInventoryItem(event.itemDrop.itemStack))
                event.isCancelled = true
        }

        // If somehow the player still has the item, prevent them from clicking it
        @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
        fun onItemClicked(event: InventoryClickEvent) {
            if (isInventoryItem(event.currentItem)) { //if the player is trying to take the item
                event.isCancelled = true
                event.currentItem?.let { event.clickedInventory?.remove(it) }
            }
            if (isInventoryItem(event.cursor)) { //if the player is trying to "place" the item in an inventory
                event.isCancelled = true
                event.clickedInventory?.remove(event.cursor)
            }
        }

    }
}