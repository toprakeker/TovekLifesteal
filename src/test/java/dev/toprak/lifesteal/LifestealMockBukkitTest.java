package dev.toprak.lifesteal;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import dev.toprak.lifesteal.manager.HeartManager;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LifestealMockBukkitTest {

    private ServerMock server;
    private LifestealPlugin plugin;
    private HeartManager heartManager;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(LifestealPlugin.class);
        heartManager = plugin.getHeartManager();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void testHeartConsumptionIncreasesMaxHealth() {
        PlayerMock player = server.addPlayer();
        heartManager.setHearts(player, 10);
        assertEquals(10, heartManager.getHearts(player));

        ItemStack heartItem = heartManager.createHeartItem(1);
        player.getInventory().setItemInMainHand(heartItem);

        PlayerInteractEvent event = new PlayerInteractEvent(
                player, Action.RIGHT_CLICK_AIR, heartItem, null, null, EquipmentSlot.HAND
        );
        server.getPluginManager().callEvent(event);

        assertEquals(11, heartManager.getHearts(player));
        assertTrue(player.getInventory().getItemInMainHand().getType() == Material.AIR || player.getInventory().getItemInMainHand().getAmount() == 0);
    }

    @Test
    void testMaxHeartLimitPreventsConsumption() {
        PlayerMock player = server.addPlayer();
        int maxHearts = heartManager.getMaxHearts();
        heartManager.setHearts(player, maxHearts);

        ItemStack heartItem = heartManager.createHeartItem(1);
        player.getInventory().setItemInMainHand(heartItem);

        PlayerInteractEvent event = new PlayerInteractEvent(
                player, Action.RIGHT_CLICK_AIR, heartItem, null, null, EquipmentSlot.HAND
        );
        server.getPluginManager().callEvent(event);

        assertEquals(maxHearts, heartManager.getHearts(player));
        assertEquals(1, player.getInventory().getItemInMainHand().getAmount());
    }

    @Test
    void testPvPDeathTransfersHeartToKiller() {
        PlayerMock killer = server.addPlayer("Killer");
        PlayerMock victim = server.addPlayer("Victim");

        heartManager.setHearts(killer, 10);
        heartManager.setHearts(victim, 10);

        victim.setKiller(killer);
        server.getPluginManager().callEvent(new org.bukkit.event.entity.PlayerDeathEvent(victim, org.bukkit.damage.DamageSource.builder(org.bukkit.damage.DamageType.PLAYER_ATTACK).build(), new java.util.ArrayList<org.bukkit.inventory.ItemStack>(), 0, net.kyori.adventure.text.Component.text("Victim died")));

        assertEquals(9, heartManager.getHearts(victim));
        assertEquals(11, heartManager.getHearts(killer));
    }

    @Test
    void testVictimEliminationAtZeroHearts() {
        PlayerMock killer = server.addPlayer("Killer");
        PlayerMock victim = server.addPlayer("Victim");

        heartManager.setHearts(killer, 10);
        heartManager.setHearts(victim, 1);

        victim.setKiller(killer);
        server.getPluginManager().callEvent(new org.bukkit.event.entity.PlayerDeathEvent(victim, org.bukkit.damage.DamageSource.builder(org.bukkit.damage.DamageType.PLAYER_ATTACK).build(), new java.util.ArrayList<org.bukkit.inventory.ItemStack>(), 0, net.kyori.adventure.text.Component.text("Victim died")));

        assertTrue(heartManager.isEliminated(victim));
    }
}
