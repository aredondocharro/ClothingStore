-- V5__admin_identity.sql
INSERT INTO users (id, email, password_hash, email_verified, created_at)
SELECT
    '${adminId}',
    '${adminEmail}',
    '${adminPasswordHash}',
    TRUE,
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM users u WHERE u.email = '${adminEmail}'
);

INSERT INTO user_roles (user_id, role)
SELECT u.id, 'ADMIN'
FROM users u
WHERE u.email = '${adminEmail}'
  AND NOT EXISTS (
      SELECT 1
      FROM user_roles ur
      WHERE ur.user_id = u.id
        AND ur.role = 'ADMIN'
  );