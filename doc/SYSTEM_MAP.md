# SYSTEM_MAP.md

System Responsibility Map

This document maps game systems to source code locations.

AI agents must use this file to locate implementation modules.

---

Core System

Location:
src/main/java/modid/core

Responsibilities:

* mod bootstrap
* lifecycle initialization
* registry setup

---

Registry System

Location:
src/main/java/modid/registry

Responsibilities:

* block registration
* item registration
* entity registration
* menu registration

Classes:

ModBlocks
ModItems
ModEntities
ModMenus

---

Event System

Location:
src/main/java/modid/events

Responsibilities:

* player events
* world events
* entity events
* server tick logic

---

Gameplay Systems

Location:
src/main/java/modid/systems

Responsibilities:

* core gameplay mechanics
* simulation logic
* system controllers

---

GUI System

Location:
src/main/java/modid/gui

Responsibilities:

* menus
* screens
* container logic

---

Network System

Location:
src/main/java/modid/network

Responsibilities:

* packet definitions
* client-server synchronization
* network handler

---

Utility Layer

Location:
src/main/java/modid/util

Responsibilities:

* helper classes
* math utilities
* configuration utilities
