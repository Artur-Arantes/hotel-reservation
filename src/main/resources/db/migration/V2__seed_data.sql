-- Hotéis
INSERT INTO hotels (name, city, state, description, created_at, updated_at) VALUES
('Hotel Atlântico',    'Rio de Janeiro', 'RJ', 'Hotel à beira-mar com vista para o Atlântico.',     NOW(), NOW()),
('Hotel Serra Verde',  'Gramado',        'RS', 'Charmoso hotel na serra gaúcha, clima europeu.',    NOW(), NOW()),
('Hotel Paulista',     'São Paulo',      'SP', 'Hotel executivo no coração da Av. Paulista.',       NOW(), NOW()),
('Hotel Recife Mar',   'Recife',         'PE', 'Resort na orla de Boa Viagem com praia privativa.', NOW(), NOW()),
('Hotel Floripa Bay',  'Florianópolis',  'SC', 'Hotel moderno próximo às melhores praias da ilha.', NOW(), NOW());

-- Quartos — Hotel Atlântico (id=1)
INSERT INTO rooms (hotel_id, room_number, type, capacity, price, created_at, updated_at) VALUES
(1, '101', 'STANDARD', 2, 350.00, NOW(), NOW()),
(1, '102', 'STANDARD', 2, 350.00, NOW(), NOW()),
(1, '201', 'DELUXE',   3, 520.00, NOW(), NOW()),
(1, '301', 'SUITE',    4, 980.00, NOW(), NOW());

-- Quartos — Hotel Serra Verde (id=2)
INSERT INTO rooms (hotel_id, room_number, type, capacity, price, created_at, updated_at) VALUES
(2, '101', 'STANDARD', 2, 420.00, NOW(), NOW()),
(2, '201', 'DELUXE',   2, 680.00, NOW(), NOW()),
(2, '301', 'SUITE',    4, 1200.00, NOW(), NOW());

-- Quartos — Hotel Paulista (id=3)
INSERT INTO rooms (hotel_id, room_number, type, capacity, price, created_at, updated_at) VALUES
(3, '101', 'STANDARD', 1, 290.00, NOW(), NOW()),
(3, '102', 'STANDARD', 2, 320.00, NOW(), NOW()),
(3, '201', 'DELUXE',   2, 490.00, NOW(), NOW());

-- Quartos — Hotel Recife Mar (id=4)
INSERT INTO rooms (hotel_id, room_number, type, capacity, price, created_at, updated_at) VALUES
(4, '101', 'STANDARD', 2, 380.00, NOW(), NOW()),
(4, '201', 'DELUXE',   3, 620.00, NOW(), NOW()),
(4, '301', 'SUITE',    4, 1100.00, NOW(), NOW());

-- Quartos — Hotel Floripa Bay (id=5)
INSERT INTO rooms (hotel_id, room_number, type, capacity, price, created_at, updated_at) VALUES
(5, '101', 'STANDARD', 2, 310.00, NOW(), NOW()),
(5, '201', 'DELUXE',   3, 540.00, NOW(), NOW()),
(5, '301', 'SUITE',    4, 950.00, NOW(), NOW());
