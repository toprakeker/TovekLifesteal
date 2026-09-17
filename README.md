# 🩸 TovekLifesteal

<div align="center">

[![Platform](https://img.shields.io/badge/Platform-Paper%20%7C%20Purpur-F78C40?style=flat-square&logo=minecraft&logoColor=white)](https://papermc.io)
[![Java](https://img.shields.io/badge/Java-21%2B-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)](LICENSE)
[![Version](https://img.shields.io/badge/Version-1.21.x-blue?style=flat-square)](https://papermc.io)

**High-throughput Lifesteal SMP core for Paper & Purpur 1.21+.**

[📦 Download Latest Release](https://github.com/toprakeker/TovekLifesteal/releases/latest) • [🌐 Developer Portfolio](https://toprakeker.com)

</div>

---

## Technical Architecture

TovekLifesteal is engineered for production SMP networks requiring deterministic data handling and dupe protection:
- **Zero Blocking I/O on Server Thread:** All text generation, MiniMessage deserialization, and math calculations operate entirely in memory without stalling world ticks.
- **NBT & PDC Verification:** Physical heart items are backed by PersistentDataContainer keys (`NamespacedKey`) with strict NBT sanitization to prevent inventory duplication and offhand desync glitches.
- **Defensive Attribute Clamping:** Heart adjustments are clamped against server bounds to prevent invalid `GENERIC_MAX_HEALTH` exceptions or attribute overflows.
- **Transactional Withdraw Logic:** Heart withdrawal guarantees atomic inventory verification before adjusting player health attributes, preventing loss on full inventories.

---

## Features

- **Heart Theft on Kill:** Slay opponents to claim their max health hearts.
- **Physical Heart Items:** PDC-tagged items that can be consumed or safely traded.
- **Withdraw System:** Players can withdraw their health into items (`/withdrawheart`).
- **Custom Crafting Recipe:** Craft Heart Fragments using Netherite, Totems of Undying, and Nether Stars.
- **Elimination Handling:** Automated spectator mode or custom actions when players reach 0 hearts.
- **Revive System:** Admins can revive eliminated players back into the survival world.

---

## Commands & Permissions

| Command | Permission | Description |
| :--- | :--- | :--- |
| `/withdrawheart [amount]` | *Everyone* | Withdraw hearts into physical consumable items |
| `/lifesteal reload` | `lifesteal.admin` | Reload configuration & crafting recipe |
| `/lifesteal give <player> [amt]` | `lifesteal.admin` | Directly give Heart Fragment items |
| `/lifesteal set <player> <hearts>` | `lifesteal.admin` | Set player's max health |
| `/lifesteal revive <player>` | `lifesteal.admin` | Revive an eliminated spectator back to survival |

---

## Crafting Recipe

```text
[ Netherite Ingot ] [ Totem of Undying ] [ Netherite Ingot ]
[ Totem of Undying ] [   Nether Star   ] [ Totem of Undying ]
[ Netherite Ingot ] [ Totem of Undying ] [ Netherite Ingot ]
```

---

## Building from Source

```bash
git clone https://github.com/toprakeker/TovekLifesteal.git
cd TovekLifesteal
./gradlew build
```

Compiled jar will be located in `build/libs/`.

---

## License

MIT License. Free for public server networks and modification.
