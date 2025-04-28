CREATE TABLE IF NOT EXISTS pokemon (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    pokedex_number VARCHAR(10) NOT NULL,
    description TEXT,
    type1 VARCHAR(50),
    type2 VARCHAR(50),
    height FLOAT,
    weight FLOAT,
    image_url TEXT
);

INSERT INTO pokemon (name, pokedex_number, description, type1, type2, height, weight, image_url) VALUES
('Pikachu', '025', 'Ein elektrisches Pokémon.', 'Elektro', NULL, 0.4, 6.0, 'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/25.png'),
('Glumanda', '004', 'Ein Feuer-Pokémon.', 'Feuer', NULL, 0.6, 8.5, 'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/4.png'),
('Schiggy', '007', 'Ein Wasser-Pokémon.', 'Wasser', NULL, 0.5, 9.0, 'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/7.png');
