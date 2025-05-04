DROP TABLE IF EXISTS pokemon CASCADE;

CREATE TABLE IF NOT EXISTS pokemon (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    pokedex_number VARCHAR(10) NOT NULL,
    description TEXT,
    type1 VARCHAR(50),
    type2 VARCHAR(50),
    height FLOAT,
    weight FLOAT,
    image_url TEXT,
    category VARCHAR(100),
    ability VARCHAR(100),
    evolution_chain_id INTEGER,
    evolves_from_id INTEGER,
    evolution_trigger VARCHAR(100),
    evolution_condition TEXT
);

-- Glumanda Evolution-Kette (ID: 2)
INSERT INTO pokemon (name, pokedex_number, description, type1, type2, height, weight, image_url, category, ability, evolution_chain_id, evolves_from_id, evolution_trigger, evolution_condition) VALUES
('Glumanda', '004', 'Die Flamme auf der Schwanzspitze zeigt seine Lebensenergie an. Ist es gesund, leuchtet sie hell.', 'FEUER', NULL, 0.6, 8.5, 'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/4.png', 'Echsen-Pokémon', 'Großbrand', 2, NULL, NULL, NULL);

INSERT INTO pokemon (name, pokedex_number, description, type1, type2, height, weight, image_url, category, ability, evolution_chain_id, evolves_from_id, evolution_trigger, evolution_condition) VALUES
('Glutexo', '005', 'Es wird sehr zornig, wenn sein Territorium bedroht wird. Seine scharfen Krallen können selbst Eisen zerschneiden.', 'FEUER', NULL, 1.1, 19.0, 'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/5.png', 'Flammen-Pokémon', 'Großbrand', 2, 1, 'level-up', 'Level 16');

INSERT INTO pokemon (name, pokedex_number, description, type1, type2, height, weight, image_url, category, ability, evolution_chain_id, evolves_from_id, evolution_trigger, evolution_condition) VALUES
('Glurak', '006', 'Es kann mit seinen Flügeln in große Höhen aufsteigen. Je intensiver es kämpft, desto heißer wird die Flamme auf seiner Schwanzspitze.', 'FEUER', 'FLUG', 1.7, 90.5, 'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/6.png', 'Flammen-Pokémon', 'Großbrand', 2, 2, 'level-up', 'Level 36');

-- Schiggy Evolution-Kette (ID: 3)
INSERT INTO pokemon (name, pokedex_number, description, type1, type2, height, weight, image_url, category, ability, evolution_chain_id, evolves_from_id, evolution_trigger, evolution_condition) VALUES
('Schiggy', '007', 'Wenn es sich in seinen Panzer zurückzieht, spritzt es Wasser aus seinem Mund mit erstaunlicher Kraft.', 'WASSER', NULL, 0.5, 9.0, 'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/7.png', 'Minikröte-Pokémon', 'Überbrause', 3, NULL, NULL, NULL);

INSERT INTO pokemon (name, pokedex_number, description, type1, type2, height, weight, image_url, category, ability, evolution_chain_id, evolves_from_id, evolution_trigger, evolution_condition) VALUES
('Schillok', '008', 'Sein dicker Pelz schützt vor Angriffen. Bei Gefahr zieht es Kopf und Gliedmaßen ein und versteckt sich in seinem harten Panzer.', 'WASSER', NULL, 1.0, 22.5, 'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/8.png', 'Kröte-Pokémon', 'Überbrause', 3, 4, 'level-up', 'Level 16');

INSERT INTO pokemon (name, pokedex_number, description, type1, type2, height, weight, image_url, category, ability, evolution_chain_id, evolves_from_id, evolution_trigger, evolution_condition) VALUES
('Turtok', '009', 'Es kann die mächtigen Wasserkanonen auf seinen Schultern mit Präzision abfeuern. Ihre Schüsse können sogar Stahl durchdringen.', 'WASSER', NULL, 1.6, 85.5, 'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/9.png', 'Panzertier-Pokémon', 'Überbrause', 3, 5, 'level-up', 'Level 36');

-- Pikachu Evolution-Kette (ID: 10)
INSERT INTO pokemon (name, pokedex_number, description, type1, type2, height, weight, image_url, category, ability, evolution_chain_id, evolves_from_id, evolution_trigger, evolution_condition) VALUES
('Pichu', '172', 'Es ist noch nicht gut im Speichern von Elektrizität. Es entlädt bei Überraschung oder Lachen ungewollt Strom.', 'ELEKTRO', NULL, 0.3, 2.0, 'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/172.png', 'Babymaus-Pokémon', 'Statik', 10, NULL, NULL, NULL);

INSERT INTO pokemon (name, pokedex_number, description, type1, type2, height, weight, image_url, category, ability, evolution_chain_id, evolves_from_id, evolution_trigger, evolution_condition) VALUES
('Pikachu', '025', 'Ein elektrisches Pokémon, das Elektrizität in seinen Backentaschen speichert. Es entlädt sie bei Gefahr oder Überraschung.', 'ELEKTRO', NULL, 0.4, 6.0, 'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/25.png', 'Maus-Pokémon', 'Statik', 10, 7, 'level-up', 'Hohe Freundschaft');

INSERT INTO pokemon (name, pokedex_number, description, type1, type2, height, weight, image_url, category, ability, evolution_chain_id, evolves_from_id, evolution_trigger, evolution_condition) VALUES
('Raichu', '026', 'Seine langen Schweif dient als Erdung, um sich selbst vor Hochspannung zu schützen. Es kann bis zu 100.000 Volt entladen.', 'ELEKTRO', NULL, 0.8, 30.0, 'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/26.png', 'Maus-Pokémon', 'Statik', 10, 8, 'item', 'Donnerstein');
