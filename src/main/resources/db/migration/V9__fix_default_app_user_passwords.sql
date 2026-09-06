UPDATE app_users
SET password = '$2a$10$GM/fV7dS.FI.MulbjcEFu.7kYo3sWHMlARx2eOib0YZIgW77SXqLq',
    enabled = true
WHERE username IN ('admin', 'auditor');
