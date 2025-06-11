-- import.sql
-- This file allows to write SQL commands that will be emitted in test and dev.

INSERT IGNORE INTO owners (uuid, timestamp, phone_number, email, description)
VALUES (0xF67EC8860051487E81D4932E298496AE,'2025-04-11 12:00:00.236678','+436608798045','prewaha.imker@example.org','Imkerverein Hagenberg im Mühlkreis'),
       (0x3A0087C4A4874800AB12CBBEF81C0A23, '2025-05-03 13:30:45.654321', '+436763780108', 'martin.imker@example.org', 'Private Kleinimkerei Wartberg ob der Aist');

INSERT IGNORE INTO locations (uuid, timestamp, latitude, longitude, comment)
VALUES (0x52743BFBE9A64470AFB7A8B161F3980B, '2025-04-11 13:34:56.355751', 48.362834, 14.520756, 'Bienenstöcke (Hagenberg im Mühlkreis)'),
       (0x848CD7D06B7241CD8BB47DCDC1455726, '2025-05-03 15:35:35.444354', 48.353418, 14.504155, 'Bienenstock im Wald (Wartberg ob der Aist)');

INSERT IGNORE INTO beehives (uuid, timestamp, location_uuid, owner_uuid, comment)
VALUES (0xD1280171E8BC4E8F80010B6F74A09847, '2025-04-11 13:34:56.355751',0x52743BFBE9A64470AFB7A8B161F3980B, 0xF67EC8860051487E81D4932E298496AE, 'Ein Bienenvolk (Blütenhonig)'),
       (0x8C1C230C4B2C4D88BEF8DB4D45E5E1B3, '2025-05-03 17:39:56.365897',0x52743BFBE9A64470AFB7A8B161F3980B, 0xF67EC8860051487E81D4932E298496AE, 'Ein Bienenvolk (Blütenhonig)'),
       (0x6C5A506607A84A218DC2766CBC63F0EB, '2025-05-03 15:35:35.444354',0x848CD7D06B7241CD8BB47DCDC1455726, 0x3A0087C4A4874800AB12CBBEF81C0A23, 'Ein Bienenvolk (Waldhonig)');
