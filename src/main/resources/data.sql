-- SQL initialization script for roll tables
-- This script creates the tables and inserts game data

CREATE TABLE IF NOT EXISTS roll_tables (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    equation VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS roll_table_records (
    id INT AUTO_INCREMENT PRIMARY KEY,
    roll_table_id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    referenced_table_id INT,
    weight INT NOT NULL DEFAULT 1,
    FOREIGN KEY (roll_table_id) REFERENCES roll_tables(id) ON DELETE CASCADE,
    FOREIGN KEY (referenced_table_id) REFERENCES roll_tables(id) ON DELETE SET NULL
);

-- Room Types Table (ID: 1)
INSERT INTO roll_tables (title, equation) VALUES ('Room Types', '1d19');
INSERT INTO roll_table_records (roll_table_id, title, referenced_table_id, weight) VALUES 
    (1, 'COURTYARD', NULL, 1),
    (1, 'CRYPT', NULL, 2),
    (1, 'DUNGEON', NULL, 3),
    (1, 'TOWER', NULL, 1),
    (1, 'KITCHEN', NULL, 1),
    (1, 'LIBRARY', NULL, 2),
    (1, 'THRONE_ROOM', NULL, 1),
    (1, 'MESS_HALL', NULL, 1),
    (1, 'STORAGE_ROOM', NULL, 1),
    (1, 'SLEEPING_QUARTERS', NULL, 1),
    (1, 'ARMORY', NULL, 1),
    (1, 'GARDEN', NULL, 1),
    (1, 'CHAPEL', NULL, 1),
    (1, 'WORKSHOP', NULL, 1),
    (1, 'LABORATORY', NULL, 1),
    (1, 'VAULT', NULL, 1),
    (1, 'TORTURE_CHAMBER', NULL, 1),
    (1, 'RITUAL_ROOM', NULL, 2),
    (1, 'STAIRWELL', NULL, 1);

-- Room Sizes Table (ID: 2)
INSERT INTO roll_tables (title, equation) VALUES ('Room Sizes', '1d3');
INSERT INTO roll_table_records (roll_table_id, title, referenced_table_id, weight) VALUES 
    (2, 'CRAMPED', NULL, 1),
    (2, 'ROOMY', NULL, 1),
    (2, 'VAST', NULL, 1);

-- Room Features Table (ID: 3)
INSERT INTO roll_tables (title, equation) VALUES ('Room Features', '1d32');
INSERT INTO roll_table_records (roll_table_id, title, referenced_table_id, weight) VALUES 
    (3, 'SMALL_TABLE', NULL, 1),
    (3, 'BIG_TABLE', NULL, 1),
    (3, 'WORK_BENCH', NULL, 1),
    (3, 'TOMB', NULL, 1),
    (3, 'COFFIN', NULL, 1),
    (3, 'THRONE', NULL, 1),
    (3, 'ALTAR', NULL, 1),
    (3, 'STATUE', NULL, 1),
    (3, 'LARGE_STATUE', NULL, 1),
    (3, 'HUGE_STATUE', NULL, 1),
    (3, 'DESK', NULL, 1),
    (3, 'SPELL_ALTAR', NULL, 2),
    (3, 'BOOK_SHELF', NULL, 1),
    (3, 'STORAGE_CABINET', NULL, 1),
    (3, 'SMALL_SHELF', NULL, 1),
    (3, 'BRAZIER', NULL, 1),
    (3, 'TELEPORTATION_CIRCLE', NULL, 1),
    (3, 'FOUNTAIN', NULL, 1),
    (3, 'TRAP_DOOR', NULL, 1),
    (3, 'DOOR', NULL, 1),
    (3, 'PIT', NULL, 1),
    (3, 'PILE_OF_RUBBLE', NULL, 1),
    (3, 'PILE_OF_TREASURE', NULL, 1),
    (3, 'CHEST', NULL, 1),
    (3, 'TWIN_BED', NULL, 1),
    (3, 'DOUBLE_BED', NULL, 1),
    (3, 'STOVE', NULL, 1),
    (3, 'ANVIL', NULL, 1),
    (3, 'FORGE', NULL, 1),
    (3, 'CAMPFIRE', NULL, 1),
    (3, 'FIREPLACE', NULL, 1),
    (3, 'BEDROLL', NULL, 1);

-- Alignments Table (ID: 4)
INSERT INTO roll_tables (title, equation) VALUES ('Alignments', '1d9');
INSERT INTO roll_table_records (roll_table_id, title, referenced_table_id, weight) VALUES 
    (4, 'LAWFUL_GOOD', NULL, 1),
    (4, 'NEUTRAL_GOOD', NULL, 1),
    (4, 'CHAOTIC_GOOD', NULL, 1),
    (4, 'LAWFUL_NEUTRAL', NULL, 1),
    (4, 'NEUTRAL', NULL, 1),
    (4, 'CHAOTIC_NEUTRAL', NULL, 1),
    (4, 'LAWFUL_EVIL', NULL, 1),
    (4, 'NEUTRAL_EVIL', NULL, 1),
    (4, 'CHAOTIC_EVIL', NULL, 1);

-- Creature Sizes Table (ID: 5)
INSERT INTO roll_tables (title, equation) VALUES ('Creature Sizes', '1d6');
INSERT INTO roll_table_records (roll_table_id, title, referenced_table_id, weight) VALUES 
    (5, 'TINY', NULL, 1),
    (5, 'SMALL', NULL, 1),
    (5, 'MEDIUM', NULL, 1),
    (5, 'LARGE', NULL, 1),
    (5, 'HUGE', NULL, 1),
    (5, 'GARGANTUAN', NULL, 1);

-- Movement Types Table (ID: 6)
INSERT INTO roll_tables (title, equation) VALUES ('Movement Types', '1d6');
INSERT INTO roll_table_records (roll_table_id, title, referenced_table_id, weight) VALUES 
    (6, 'WALKING', NULL, 1),
    (6, 'FLYING', NULL, 1),
    (6, 'SWIMMING', NULL, 1),
    (6, 'CLIMBING', NULL, 1),
    (6, 'BURROWING', NULL, 1),
    (6, 'TELEPORTING', NULL, 1);

-- Path Distances Table (ID: 7)
INSERT INTO roll_tables (title, equation) VALUES ('Path Distances', '1d4');
INSERT INTO roll_table_records (roll_table_id, title, referenced_table_id, weight) VALUES 
    (7, 'MELEE', NULL, 1),
    (7, 'SHORT', NULL, 1),
    (7, 'FAR', NULL, 1),
    (7, 'VERY_FAR', NULL, 1);

-- Rarities Table (ID: 8)
INSERT INTO roll_tables (title, equation) VALUES ('Rarities', '1d3');
INSERT INTO roll_table_records (roll_table_id, title, referenced_table_id, weight) VALUES 
    (8, 'COMMON', NULL, 3),
    (8, 'UNCOMMON', NULL, 2),
    (8, 'RARE', NULL, 1);

-- NPC Jobs Table (ID: 9)
INSERT INTO roll_tables (title, equation) VALUES ('NPC Jobs', '1d6');
INSERT INTO roll_table_records (roll_table_id, title, referenced_table_id, weight) VALUES 
    (9, 'COMMONER', NULL, 1),
    (9, 'MINION', NULL, 1),
    (9, 'TOUGH', NULL, 1),
    (9, 'ELITE', NULL, 1),
    (9, 'BOSS', NULL, 1),
    (9, 'DIETY', NULL, 1);

-- Player Character Jobs (Common) Table (ID: 10)
INSERT INTO roll_tables (title, equation) VALUES ('PC Jobs (Common)', '1d7');
INSERT INTO roll_table_records (roll_table_id, title, referenced_table_id, weight) VALUES 
    (10, 'BARBARIAN', NULL, 1),
    (10, 'BARD', NULL, 1),
    (10, 'CLERIC', NULL, 1),
    (10, 'FIGHTER', NULL, 1),
    (10, 'PALADIN', NULL, 1),
    (10, 'RANGER', NULL, 1),
    (10, 'ROGUE', NULL, 1);

-- Player Character Jobs (Uncommon) Table (ID: 11)
INSERT INTO roll_tables (title, equation) VALUES ('PC Jobs (Uncommon)', '1d3');
INSERT INTO roll_table_records (roll_table_id, title, referenced_table_id, weight) VALUES 
    (11, 'DRUID', NULL, 1),
    (11, 'MONK', NULL, 1),
    (11, 'WARLOCK', NULL, 1);

-- Player Character Jobs (Rare) Table (ID: 12)
INSERT INTO roll_tables (title, equation) VALUES ('PC Jobs (Rare)', '1d3');
INSERT INTO roll_table_records (roll_table_id, title, referenced_table_id, weight) VALUES 
    (12, 'ARTIFICER', NULL, 1),
    (12, 'SORCERER', NULL, 1),
    (12, 'WIZARD', NULL, 1);

-- All Player Character Jobs Table (ID: 13)
INSERT INTO roll_tables (title, equation) VALUES ('All PC Jobs', '1d13');
INSERT INTO roll_table_records (roll_table_id, title, referenced_table_id, weight) VALUES 
    (13, 'ARTIFICER', NULL, 1),
    (13, 'BARBARIAN', NULL, 1),
    (13, 'BARD', NULL, 1),
    (13, 'CLERIC', NULL, 1),
    (13, 'DRUID', NULL, 1),
    (13, 'FIGHTER', NULL, 1),
    (13, 'MONK', NULL, 1),
    (13, 'PALADIN', NULL, 1),
    (13, 'RANGER', NULL, 1),
    (13, 'ROGUE', NULL, 1),
    (13, 'SORCERER', NULL, 1),
    (13, 'WARLOCK', NULL, 1),
    (13, 'WIZARD', NULL, 1);

-- Sample: Simple Encounter Table (ID: 14)
INSERT INTO roll_tables (title, equation) VALUES ('Simple Encounter', '1d6');
INSERT INTO roll_table_records (roll_table_id, title, referenced_table_id, weight) VALUES 
    (14, 'Peaceful', NULL, 2),
    (14, 'Wary', NULL, 2),
    (14, 'Hostile', NULL, 1),
    (14, 'Very Hostile', NULL, 1),
    (14, 'Aggressive Combat', NULL, 1),
    (14, 'Deadly Encounter', NULL, 1);

-- Sample: Treasure Chest Table (ID: 15)
INSERT INTO roll_tables (title, equation) VALUES ('Treasure Chest', '2d10');
INSERT INTO roll_table_records (roll_table_id, title, referenced_table_id, weight) VALUES 
    (15, 'Empty', NULL, 2),
    (15, '10 GP', NULL, 2),
    (15, '25 GP', NULL, 3),
    (15, '50 GP', NULL, 3),
    (15, '100 GP', NULL, 2),
    (15, '250 GP', NULL, 2),
    (15, '500 GP', NULL, 1),
    (15, '1000 GP', NULL, 1),
    (15, 'Magical Item', NULL, 1),
    (15, 'Legendary Item', NULL, 1),
    (15, 'Cursed Item', NULL, 1),
    (15, 'Ancient Relic', NULL, 1),
    (15, 'Dragon Hoard', NULL, 1),
    (15, 'Lost Treasure', NULL, 1),
    (15, 'Kings Ransom', NULL, 1),
    (15, 'Bottomless Wealth', NULL, 1),
    (15, 'Divine Blessing', NULL, 1),
    (15, 'Artifact of Power', NULL, 1),
    (15, 'Forbidden Knowledge', NULL, 1),
    (15, 'Ultimate Prize', NULL, 1);
