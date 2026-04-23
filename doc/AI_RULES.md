# AI Development Rules
# Minecraft Forge Mod Development Constraints

AI MUST read this file before generating code.
This file defines strict development rules for AI agents working on this project.

The mod must strictly follow:

Minecraft 1.20.1
Forge 47.4.16
Official Mojang Mappings

All code must comply with the project specification in:

docs/V51_Specification.md

AI must never invent new gameplay mechanics.


------------------------------------------------
1. Specification First Rule
------------------------------------------------

Before performing any action, AI must read:

docs/V51_Specification.md

All features must strictly follow the specification.

AI is forbidden to create features not defined in the specification.



------------------------------------------------
2. Event Handling Rules
------------------------------------------------

Only classes inside the package:

com.symbioticlaw.event

are allowed to subscribe to Forge EventBus.

System classes are NOT allowed to register events.

Example (Allowed):

event.PlayerEvents
event.TickEvents

Example (Forbidden):

system.EconomySystem
system.VisaSystem



------------------------------------------------
3. Tick Handling Rules
------------------------------------------------

TickHandler is the ONLY class allowed to listen to:

ServerTickEvent

All system tick execution must be scheduled inside:

TickHandler.onServerTick()

Systems must never subscribe to tick events directly.



------------------------------------------------
4. Player Data Rules
------------------------------------------------

All player state must be stored in:

PlayerDataCapability

AI must NEVER store player state in:

NBT
BlockEntity
GUI
Static variables

PlayerData must always be retrieved through Capability.



------------------------------------------------
5. World Data Rules
------------------------------------------------

Global world data must be stored using:

SavedData

Class:

WorldEconomyData

AI must not store world state in files or JSON.



------------------------------------------------
6. Networking Rules
------------------------------------------------

All client-server communication must use:

Forge SimpleChannel

Network registration must be done in:

NetworkHandler

Packets must be located in:

com.symbioticlaw.network



------------------------------------------------
7. Registry Rules
------------------------------------------------

All Minecraft content must be registered using:

DeferredRegister

Allowed registry classes:

ModBlocks
ModItems
ModMenus
ModBlockEntities
ModCreativeTabs

AI must NOT use:

Registry.register()



------------------------------------------------
8. System Architecture Rules
------------------------------------------------

Systems must be stateless.

Systems must NOT store references to:

ServerPlayer
Player
Level

Systems must only operate on:

PlayerData



------------------------------------------------
9. Performance Rules
------------------------------------------------

Total system processing must remain under:

50 ms per tick.

Heavy operations must be batched.

Maximum scan per tick:

50 players
or
50 entities.



------------------------------------------------
10. Package Structure Rules
------------------------------------------------

All code must follow this package structure:


com.symbioticlaw

 ├─ registry
 ├─ capability
 ├─ data
 ├─ system
 ├─ event
 ├─ network
 ├─ gui
 └─ test



AI must not create additional top-level packages.



------------------------------------------------
11. Testing Rules
------------------------------------------------

All systems must include tests.

Testing frameworks:

JUnit
Forge GameTest

Test coverage must exceed:

95%



------------------------------------------------
12. Build Verification Rules
------------------------------------------------

Before declaring a build successful, AI must run:

gradlew clean build
gradlew runData
gradlew runGameTestServer

The build must result in:

0 compile errors
0 runtime crashes
0 major warnings.



------------------------------------------------
13. Security Rules
------------------------------------------------

Client-side code must NEVER modify:

Economy
Contracts
Player Class
Visa State

Server is the only authority.



------------------------------------------------
14. Code Quality Rules
------------------------------------------------

AI must ensure:

No duplicated logic
No unused registries
No unused imports
No placeholder methods



------------------------------------------------
15. Forbidden Behavior
------------------------------------------------

AI must never:

invent gameplay mechanics
modify specification
bypass capability system
write persistent data outside SavedData
register events inside system classes
create multiple tick handlers