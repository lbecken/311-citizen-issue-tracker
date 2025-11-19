-- Migration V3: Add view for historical resolution time statistics
-- Used by AI prediction service to estimate resolution times

-- Add resolved_at column to issues table if it doesn't exist
ALTER TABLE issues ADD COLUMN IF NOT EXISTS resolved_at TIMESTAMP;

-- Create view for aggregated historical resolution statistics
CREATE OR REPLACE VIEW historical_resolution_times AS
SELECT
    department_id,
    category,
    AVG(EXTRACT(EPOCH FROM (resolved_at - created_at))/3600) as avg_hours,
    PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY EXTRACT(EPOCH FROM (resolved_at - created_at))/3600) as median_hours,
    COUNT(*) as sample_count
FROM issues
WHERE status = 'CLOSED' AND resolved_at IS NOT NULL
GROUP BY department_id, category;

COMMENT ON VIEW historical_resolution_times IS 'Aggregated statistics for issue resolution times by department and category';
