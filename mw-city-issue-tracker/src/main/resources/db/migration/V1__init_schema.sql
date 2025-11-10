CREATE TABLE departments (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    emoji VARCHAR(10),
    avg_resolution_hours INTEGER DEFAULT 48
);

CREATE TABLE issues (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(200) NOT NULL,
    description TEXT,
    category VARCHAR(50) NOT NULL CHECK (category IN ('POTHOLE', 'STREETLIGHT', 'GRAFFITI', 'TRASH', 'NOISE', 'OTHER')),
    status VARCHAR(20) DEFAULT 'REPORTED' CHECK (status IN ('REPORTED', 'VALIDATED', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED')),
    priority INTEGER DEFAULT 3 CHECK (priority BETWEEN 1 AND 5),
    location GEOMETRY(Point, 4326) NOT NULL,
    address TEXT,
    reported_by VARCHAR(100) NOT NULL,
    reporter_email VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    department_id INTEGER REFERENCES departments(id),
    worker_id VARCHAR(100)
);
CREATE INDEX idx_issues_location ON issues USING GIST(location);
CREATE INDEX idx_issues_status ON issues(status);
CREATE INDEX idx_issues_created_at ON issues(created_at);

-- Seed departments
INSERT INTO departments (name, emoji, avg_resolution_hours) VALUES
('Road Maintenance', '🛣️', 72),
('Public Lighting', '💡', 48),
('Sanitation', '🗑️', 24),
('Code Enforcement', '🎨', 96);