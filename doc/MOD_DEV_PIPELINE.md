# MOD_DEV_PIPELINE.md

AI Development Pipeline for Minecraft Forge Mod

This document defines the step-by-step workflow AI agents must follow when developing this mod.

Agents must strictly follow this pipeline.

---

## STEP 1 — Load Context

Before starting any development task, the agent must load the following files:

AGENTS.md
AI_RULES.md
ARCHITECTURE.md
SYSTEM_MAP.md
MODULE_INDEX.md

Then load the gameplay specification:

docs/V51_Specification.md

No development work may start before these files are read.

---

## STEP 2 — Analyze Specification

The agent must parse docs/V51_Specification.md and identify:

* required gameplay systems
* blocks and items
* entities
* GUI elements
* networking requirements
* events
* data generation requirements

The specification is the single source of truth.

---

## STEP 3 — Identify Target Modules

Using SYSTEM_MAP.md and MODULE_INDEX.md, the agent must determine:

which source modules need modification or creation.

Example:

New block → registry + block class + model + blockstate
New GUI → menu + screen + network sync

---

## STEP 4 — Implement Code

Generate or modify source code according to:

ARCHITECTURE.md

Implementation rules:

* follow package structure
* use Forge DeferredRegister
* avoid deprecated APIs
* follow Forge lifecycle

---

## STEP 5 — Generate Resources

If new content is added, the agent must generate resource files.

Required resource types:

blockstates
models
recipes
loot tables
language entries

Assets location:

src/main/resources/assets/modid/

Data location:

src/main/resources/data/modid/

---

## STEP 6 — Data Generation

Run Forge DataGenerator when required.

Command:

gradlew runData

Ensure generated assets match registry objects.

---

## STEP 7 — Testing

The agent must create automated tests.

Testing frameworks:

GameTest
JUnit

Test areas include:

* block behavior
* item functionality
* event triggers
* network synchronization
* GUI interactions

Target coverage:

≥ 95%

---

## STEP 8 — Build Verification

The agent must verify the project builds successfully.

Run:

gradlew clean
gradlew build
gradlew runGameTestServer

Requirements:

0 compile errors
0 runtime crashes
no major warnings

---

## STEP 9 — Performance Validation

The agent must ensure server performance stays within limits.

Maximum allowed tick execution time:

50 ms/tick

Rules:

avoid heavy loops in event handlers
cache repeated calculations

---

## STEP 10 — Error Recovery

If errors occur during build or runtime:

1 analyze error logs
2 locate faulty code
3 apply fixes
4 rebuild the project

Repeat until:

0 errors
0 crashes

---

## STEP 11 — Final Output

The final deliverable must include:

Production mod JAR

Location:

build/libs/

Reports:

build_report.md
test_report.md
performance_report.md
