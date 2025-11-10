-- This script is run once when the database is first initialized

-- Enable PostGIS extension
CREATE EXTENSION IF NOT EXISTS postgis;

-- (Optional) Other DB-level setup that should only happen once:
-- CREATE EXTENSION IF NOT EXISTS postgis_topology;
-- GRANT usage, create on schema public TO your_app_user;
