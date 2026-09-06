# 🩸 TovekLifesteal

<div align="center">

[![Platform](https://img.shields.io/badge/Platform-Paper%20%7C%20Purpur-F78C40?style=flat-square&logo=minecraft&logoColor=white)](https://papermc.io)
[![Java](https://img.shields.io/badge/Java-21%2B-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)](LICENSE)
[![Version](https://img.shields.io/badge/Version-1.21.x-blue?style=flat-square)](https://papermc.io)

**Lightweight, lag-free Lifesteal SMP core for Paper & Purpur 1.21+.**

[📦 Download Latest Release](https://github.com/toprakeker/TovekLifesteal/releases/latest) • [🌐 Developer Portfolio](https://toprakeker.com)

</div>

---

## Features

- **Heart Theft on Kill:** Slay opponents to claim their max health hearts.
- **Physical Heart Items:** PDC-tagged items that can be consumed or safely traded.
- **Withdraw System:** Players can withdraw their health into items (`/withdrawheart`).
- **Custom Crafting Recipe:** Craft Heart Fragments using Netherite, Totems of Undying, and Nether Stars.
- **Elimination Handling:** Automated spectator mode or custom actions when players reach 0 hearts.
- **Revive System:** Admins can revive eliminated players back into the survival world.
- **Zero Lag:** Native Adventure Component & MiniMessage rendering, zero main-thread blocking operations.

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

MIT License. Feel free to use on public servers, fork, or modify.
