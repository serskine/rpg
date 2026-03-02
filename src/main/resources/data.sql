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
    metadata TEXT,
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

-- ============================================================================
-- ENUMERATION DATA MIGRATION TO ROLL TABLES
-- All static enum data now stored in database with metadata
-- ============================================================================

-- Room Sizes Table (ID: 16)
INSERT INTO roll_tables (title, equation) VALUES ('Room Sizes (Enum)', '1d3');
INSERT INTO roll_table_records (roll_table_id, title, weight, metadata) VALUES 
    (16, 'CRAMPED', 1, '{"numSquares":4}'),
    (16, 'ROOMY', 1, '{"numSquares":16}'),
    (16, 'VAST', 1, '{"numSquares":36}');

-- Room Features Table (ID: 17) - 32 features with width/height
INSERT INTO roll_tables (title, equation) VALUES ('Room Features (Enum)', '1d32');
INSERT INTO roll_table_records (roll_table_id, title, weight, metadata) VALUES 
    (17, 'SMALL_TABLE', 1, '{"width":2,"height":2}'),
    (17, 'BIG_TABLE', 1, '{"width":3,"height":2}'),
    (17, 'WORK_BENCH', 1, '{"width":3,"height":2}'),
    (17, 'TOMB', 1, '{"width":3,"height":2}'),
    (17, 'COFFIN', 1, '{"width":2,"height":1}'),
    (17, 'THRONE', 1, '{"width":1,"height":1}'),
    (17, 'ALTAR', 1, '{"width":2,"height":1}'),
    (17, 'STATUE', 1, '{"width":1,"height":1}'),
    (17, 'LARGE_STATUE', 1, '{"width":2,"height":2}'),
    (17, 'HUGE_STATUE', 1, '{"width":3,"height":3}'),
    (17, 'DESK', 1, '{"width":2,"height":1}'),
    (17, 'SPELL_ALTAR', 1, '{"width":3,"height":2}'),
    (17, 'BOOK_SHELF', 1, '{"width":3,"height":1}'),
    (17, 'STORAGE_CABINET', 1, '{"width":3,"height":1}'),
    (17, 'SMALL_SHELF', 1, '{"width":3,"height":1}'),
    (17, 'BRAZIER', 1, '{"width":1,"height":1}'),
    (17, 'TELEPORTATION_CIRCLE', 1, '{"width":2,"height":2}'),
    (17, 'FOUNTAIN', 1, '{"width":2,"height":2}'),
    (17, 'TRAP_DOOR', 1, '{"width":1,"height":1}'),
    (17, 'DOOR', 1, '{"width":1,"height":1}'),
    (17, 'PIT', 1, '{"width":2,"height":2}'),
    (17, 'PILE_OF_RUBBLE', 1, '{"width":3,"height":2}'),
    (17, 'PILE_OF_TREASURE', 1, '{"width":2,"height":2}'),
    (17, 'CHEST', 1, '{"width":1,"height":1}'),
    (17, 'TWIN_BED', 1, '{"width":2,"height":1}'),
    (17, 'DOUBLE_BED', 1, '{"width":3,"height":2}'),
    (17, 'STOVE', 1, '{"width":2,"height":1}'),
    (17, 'ANVIL', 1, '{"width":1,"height":1}'),
    (17, 'FORGE', 1, '{"width":3,"height":2}'),
    (17, 'CAMPFIRE', 1, '{"width":1,"height":1}'),
    (17, 'FIREPLACE', 1, '{"width":3,"height":1}'),
    (17, 'BEDROLL', 1, '{"width":2,"height":1}');

-- Alignments Table (ID: 18)
INSERT INTO roll_tables (title, equation) VALUES ('Alignments (Enum)', '1d9');
INSERT INTO roll_table_records (roll_table_id, title, weight) VALUES 
    (18, 'LAWFUL_GOOD', 1),
    (18, 'NEUTRAL_GOOD', 1),
    (18, 'CHAOTIC_GOOD', 1),
    (18, 'LAWFUL_NEUTRAL', 1),
    (18, 'NEUTRAL', 1),
    (18, 'CHAOTIC_NEUTRAL', 1),
    (18, 'LAWFUL_EVIL', 1),
    (18, 'NEUTRAL_EVIL', 1),
    (18, 'CHAOTIC_EVIL', 1);

-- Creature Sizes Table (ID: 19)
INSERT INTO roll_tables (title, equation) VALUES ('Creature Sizes (Enum)', '1d6');
INSERT INTO roll_table_records (roll_table_id, title, weight) VALUES 
    (19, 'TINY', 1),
    (19, 'SMALL', 1),
    (19, 'MEDIUM', 1),
    (19, 'LARGE', 1),
    (19, 'HUGE', 1),
    (19, 'GARGANTUAN', 1);

-- Movement Types Table (ID: 20)
INSERT INTO roll_tables (title, equation) VALUES ('Movement Types (Enum)', '1d6');
INSERT INTO roll_table_records (roll_table_id, title, weight) VALUES 
    (20, 'WALKING', 1),
    (20, 'FLYING', 1),
    (20, 'SWIMMING', 1),
    (20, 'CLIMBING', 1),
    (20, 'BURROWING', 1),
    (20, 'TELEPORTING', 1);

-- Rarity Table (ID: 21)
INSERT INTO roll_tables (title, equation) VALUES ('Rarities (Enum)', '1d3');
INSERT INTO roll_table_records (roll_table_id, title, weight) VALUES 
    (21, 'COMMON', 3),
    (21, 'UNCOMMON', 2),
    (21, 'RARE', 1);

-- Path Distances Table (ID: 22) - with minimum distance in feet
INSERT INTO roll_tables (title, equation) VALUES ('Path Distances (Enum)', '1d4');
INSERT INTO roll_table_records (roll_table_id, title, weight, metadata) VALUES 
    (22, 'MELEE', 1, '{"minimum":15}'),
    (22, 'SHORT', 1, '{"minimum":30}'),
    (22, 'FAR', 1, '{"minimum":60}'),
    (22, 'VERY_FAR', 1, '{"minimum":120}');

-- Player Character Jobs - Common (ID: 23)
INSERT INTO roll_tables (title, equation) VALUES ('PC Jobs - Common (Enum)', '1d7');
INSERT INTO roll_table_records (roll_table_id, title, weight, metadata) VALUES 
    (23, 'BARBARIAN', 1, '{"strength":15,"dexterity":13,"constitution":14,"intelligence":8,"wisdom":10,"charisma":12,"isCaster":false,"rarity":"COMMON"}'),
    (23, 'BARD', 1, '{"strength":10,"dexterity":14,"constitution":12,"intelligence":13,"wisdom":8,"charisma":15,"isCaster":true,"rarity":"COMMON"}'),
    (23, 'CLERIC', 1, '{"strength":13,"dexterity":8,"constitution":14,"intelligence":10,"wisdom":15,"charisma":12,"isCaster":true,"rarity":"COMMON"}'),
    (23, 'FIGHTER', 1, '{"strength":15,"dexterity":14,"constitution":13,"intelligence":8,"wisdom":12,"charisma":10,"isCaster":false,"rarity":"COMMON"}'),
    (23, 'PALADIN', 1, '{"strength":15,"dexterity":10,"constitution":13,"intelligence":8,"wisdom":12,"charisma":14,"isCaster":true,"rarity":"COMMON"}'),
    (23, 'RANGER', 1, '{"strength":13,"dexterity":15,"constitution":12,"intelligence":10,"wisdom":14,"charisma":8,"isCaster":true,"rarity":"COMMON"}'),
    (23, 'ROGUE', 1, '{"strength":8,"dexterity":15,"constitution":10,"intelligence":14,"wisdom":12,"charisma":13,"isCaster":false,"rarity":"COMMON"}');

-- Player Character Jobs - Uncommon (ID: 24)
INSERT INTO roll_tables (title, equation) VALUES ('PC Jobs - Uncommon (Enum)', '1d3');
INSERT INTO roll_table_records (roll_table_id, title, weight, metadata) VALUES 
    (24, 'DRUID', 1, '{"strength":12,"dexterity":13,"constitution":14,"intelligence":10,"wisdom":15,"charisma":8,"isCaster":true,"rarity":"UNCOMMON"}'),
    (24, 'MONK', 1, '{"strength":13,"dexterity":14,"constitution":10,"intelligence":8,"wisdom":15,"charisma":12,"isCaster":false,"rarity":"UNCOMMON"}'),
    (24, 'WARLOCK', 1, '{"strength":12,"dexterity":14,"constitution":13,"intelligence":10,"wisdom":8,"charisma":15,"isCaster":true,"rarity":"UNCOMMON"}');

-- Player Character Jobs - Rare (ID: 25)
INSERT INTO roll_tables (title, equation) VALUES ('PC Jobs - Rare (Enum)', '1d3');
INSERT INTO roll_table_records (roll_table_id, title, weight, metadata) VALUES 
    (25, 'ARTIFICER', 1, '{"strength":13,"dexterity":14,"constitution":12,"intelligence":15,"wisdom":8,"charisma":10,"isCaster":true,"rarity":"RARE"}'),
    (25, 'SORCERER', 1, '{"strength":8,"dexterity":14,"constitution":12,"intelligence":13,"wisdom":10,"charisma":15,"isCaster":true,"rarity":"RARE"}'),
    (25, 'WIZARD', 1, '{"strength":8,"dexterity":12,"constitution":10,"intelligence":15,"wisdom":14,"charisma":3,"isCaster":true,"rarity":"RARE"}');

-- NPC Jobs/Classes (ID: 26)
INSERT INTO roll_tables (title, equation) VALUES ('NPC Jobs (Enum)', '1d7');
INSERT INTO roll_table_records (roll_table_id, title, weight, metadata) VALUES 
    (26, 'SICKLY', 1, '{"strength":8,"dexterity":8,"constitution":8,"intelligence":8,"wisdom":8,"charisma":8,"isCaster":false,"rarity":"COMMON"}'),
    (26, 'COMMONER', 1, '{"strength":10,"dexterity":10,"constitution":10,"intelligence":10,"wisdom":10,"charisma":10,"isCaster":false,"rarity":"COMMON"}'),
    (26, 'MINION', 1, '{"strength":12,"dexterity":12,"constitution":12,"intelligence":12,"wisdom":12,"charisma":12,"isCaster":false,"rarity":"COMMON"}'),
    (26, 'TOUGH', 1, '{"strength":14,"dexterity":14,"constitution":14,"intelligence":14,"wisdom":14,"charisma":14,"isCaster":false,"rarity":"COMMON"}'),
    (26, 'ELITE', 1, '{"strength":16,"dexterity":16,"constitution":16,"intelligence":16,"wisdom":16,"charisma":16,"isCaster":false,"rarity":"UNCOMMON"}'),
    (26, 'BOSS', 1, '{"strength":18,"dexterity":18,"constitution":18,"intelligence":18,"wisdom":18,"charisma":18,"isCaster":true,"rarity":"UNCOMMON"}'),
    (26, 'DIETY', 1, '{"strength":20,"dexterity":20,"constitution":20,"intelligence":20,"wisdom":20,"charisma":20,"isCaster":true,"rarity":"RARE"}');
