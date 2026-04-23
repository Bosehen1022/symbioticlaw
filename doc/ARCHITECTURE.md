# ARCHITECTURE.md

Minecraft Forge Mod Architecture Specification

---

# 1. Project Overview

This project is a mod for **Minecraft 1.20.1** built using **Forge 47.4.16**.

The purpose of this document is to define the architectural structure of the mod so that automated agents and developers follow a consistent design.

All implementations must follow the specification defined in:

docs/V51_Specification.md

This document defines **how the code is structured**, not the gameplay logic itself.

---

# 2. Technology Stack

Minecraft Version
1.20.1

Forge Version
47.4.16

Java Version
17

Build System
Gradle (ForgeGradle)

Mappings
Mojang official mappings

---

# 3. High-Level Architecture

The mod follows a modular architecture.

Core components:

* Core bootstrap system
* Registry system
* Event system
* Gameplay systems
* GUI systems
* Network synchronization
* Data generation
* Automated testing

The architecture ensures:

* clear separation of concerns
* maintainable code structure
* compatibility with Forge lifecycle

---

# 4. Project Directory Structure

src/main/java/modid/

Core packages:

core
registry
events
systems
blocks
items
entities
gui
network
util

Example structure:

modid
├─ core
│  └─ ModMain.java
│
├─ registry
│  ├─ ModBlocks.java
│  ├─ ModItems.java
│  ├─ ModEntities.java
│
├─ events
│  ├─ ServerEvents.java
│  ├─ PlayerEvents.java
│
├─ systems
│  ├─ EconomySystem.java
│  ├─ SurveillanceSystem.java
│
├─ blocks
├─ items
├─ entities
│
├─ gui
│  ├─ menu
│  └─ screen
│
├─ network
│  ├─ packets
│  └─ NetworkHandler.java
│
└─ util

---

# 5. Forge Registry Architecture

All game objects must be registered using **DeferredRegister**.

Registry types:

Blocks
Items
Entities
BlockEntities
MenuTypes
CreativeTabs

Rules:

* All registry logic must be centralized
* Registrations occur during mod initialization
* No direct registry calls outside registry classes

Example registry classes:

ModBlocks
ModItems
ModEntities

---

# 6. Event System Architecture

The mod relies heavily on the **Forge Event Bus**.

Event categories:

Server events
Player events
Entity events
World events
Block events

Rules:

* Event handlers must be lightweight
* Heavy logic must be delegated to systems
* Event execution time must stay under **50 ms per tick**

---

# 7. Gameplay Systems

Game logic is implemented inside the **systems** package.

Examples:

EconomySystem
ClassSystem
SurveillanceSystem
LaborSystem

Rules:

* Systems must be independent modules
* Systems communicate via events or APIs
* No direct cross-system dependencies

---

# 8. GUI Architecture

All UI must follow the Forge client-server pattern.

Components:

MenuType
Container/Menu
Screen
Network Sync

Rules:

* Server owns the data
* Client renders the UI
* Synchronization uses network packets

---

# 9. Networking Architecture

Networking must use **SimpleChannel**.

Components:

NetworkHandler
Packet classes

Rules:

* All packets must be registered during mod setup
* Client-server communication must be explicit
* No direct client data mutation

---

# 10. Data Generation

The project uses **Forge DataGenerator**.

Generated assets:

blockstates
models
recipes
loot tables
tags
language files

Command:

gradlew runData

Rules:

* No manually duplicated data
* Generated assets must match registry objects

---

# 11. Testing Architecture

The project uses:

GameTest framework
JUnit unit tests

Coverage goals:

≥ 95%

Test categories:

Block behavior
Item logic
Event triggers
Network synchronization
System logic

---

# 12. Performance Requirements

Server tick time:

≤ 50 ms/tick

Rules:

* Avoid heavy loops inside events
* Cache frequently accessed data
* Use asynchronous tasks where appropriate

---

# 13. Build Pipeline

Standard build process:

gradlew clean
gradlew build
gradlew runData
gradlew runGameTestServer

Build requirements:

* zero compilation errors
* zero runtime crashes
* no major warnings

---

# 14. Agent Development Rules

Automated agents must follow this workflow:

1. Read docs/V51_Specification.md
2. Follow the architecture defined here
3. Implement features exactly as specified
4. Validate via automated tests
5. Ensure build success

Agents must **not introduce features not defined in the specification**.

---

# 15. Final Output

The final deliverable must include:

Production mod JAR

build/libs/

Verification reports:

build_report.md
test_report.md
performance_report.md
