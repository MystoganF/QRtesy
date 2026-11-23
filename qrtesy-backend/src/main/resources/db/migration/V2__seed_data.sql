-- 1. Cebu Dev Meetup 2026 (Today in Asia/Manila, capacity 30, 2 confirmed -> 28 spots remaining)
INSERT INTO qrtesy.events (id, title, description, venue, starts_at, ends_at, capacity, status)
VALUES (
           '11111111-1111-1111-1111-111111111101',
           'Cebu Dev Meetup 2026',
           'Community gathering for Cebu software engineers and IT students covering clean architecture, web APIs, and practical engineering.',
           'Ayala Center Cebu, Function Room 2',
           (((CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Manila')::date + TIME '13:00:00') AT TIME ZONE 'Asia/Manila'),
    (((CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Manila')::date + TIME '22:00:00') AT TIME ZONE 'Asia/Manila'),
    30,
    'PUBLISHED'
)
ON CONFLICT (id) DO NOTHING;

-- 2. Full Event (2 of 2 spots taken so "Full" state is testable right away)
INSERT INTO qrtesy.events (id, title, description, venue, starts_at, ends_at, capacity, status)
VALUES (
           '11111111-1111-1111-1111-111111111102',
           'Spring Boot & PostgreSQL Clinic',
           'Hands-on session on relational modeling, transactions, and API security in Spring Boot.',
           'IT Park Cebu, Skyrise 4B',
           (((CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Manila')::date + INTERVAL '3 days' + TIME '10:00:00') AT TIME ZONE 'Asia/Manila'),
    (((CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Manila')::date + INTERVAL '3 days' + TIME '16:00:00') AT TIME ZONE 'Asia/Manila'),
    2,
    'PUBLISHED'
)
ON CONFLICT (id) DO NOTHING;

-- Sample confirmed registrations for Cebu Dev Meetup 2026
INSERT INTO qrtesy.registrations (id, event_id, full_name, email, mobile, ticket_code, status)
VALUES
    ('22222222-2222-2222-2222-222222222201', '11111111-1111-1111-1111-111111111101', 'Maria Santos', 'maria.santos@example.com', '09171234567', 'CDM-7K3P9Q', 'CONFIRMED'),
    ('22222222-2222-2222-2222-222222222202', '11111111-1111-1111-1111-111111111101', 'Jose Reyes', 'jose.reyes@example.com', '09189876543', 'CDM-4M8R2X', 'CONFIRMED')
    ON CONFLICT (id) DO NOTHING;

-- Sample confirmed registrations filling Spring Boot & PostgreSQL Clinic (2 / 2)
INSERT INTO qrtesy.registrations (id, event_id, full_name, email, mobile, ticket_code, status)
VALUES
    ('22222222-2222-2222-2222-222222222203', '11111111-1111-1111-1111-111111111102', 'Ana Villanueva', 'ana.v@example.com', '09201112233', 'SBQ-9N2L5T', 'CONFIRMED'),
    ('22222222-2222-2222-2222-222222222204', '11111111-1111-1111-1111-111111111102', 'Carlo Mendoza', 'carlo.m@example.com', '09274445566', 'SBQ-6H4W8Y', 'CONFIRMED')
    ON CONFLICT (id) DO NOTHING;