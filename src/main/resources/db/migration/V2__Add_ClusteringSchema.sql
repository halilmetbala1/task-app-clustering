-- V2__Add_ClusteringSchema.sql
-- Migration to adapt schema for clustering task model: Adds columns and new tables for clusters and data points

-- Add new columns to task table
ALTER TABLE task
    ADD COLUMN num_clusters INTEGER,
    ADD COLUMN num_data_points INTEGER,
    ADD COLUMN distance_metric VARCHAR(50),
    ADD COLUMN difficulty VARCHAR(50);

-- Drop obsolete column
ALTER TABLE task
    DROP COLUMN solution;


CREATE SEQUENCE cluster_id_seq START WITH 1 INCREMENT BY 1;
-- Grant access to the sequence
DO $$
    BEGIN
        IF EXISTS (
            SELECT FROM pg_roles WHERE rolname = 'etutor_clustering_test'
        ) THEN
            GRANT USAGE, SELECT ON SEQUENCE cluster_id_seq TO etutor_clustering_test;
        END IF;

        IF EXISTS (
            SELECT FROM pg_roles WHERE rolname = 'etutor_clustering'
        ) THEN
            GRANT USAGE, SELECT ON SEQUENCE cluster_id_seq TO etutor_clustering;
        END IF;
    END $$;

-- Create table for cluster
CREATE TABLE cluster (
                         id BIGINT PRIMARY KEY DEFAULT nextval('cluster_id_seq'),
                         task_id BIGINT NOT NULL REFERENCES task(id) ON DELETE CASCADE,
                         type VARCHAR(50) NOT NULL,
                         x DOUBLE PRECISION NOT NULL,
                         y DOUBLE PRECISION NOT NULL
);

-- Create table for data point
CREATE SEQUENCE data_point_id_seq START WITH 1 INCREMENT BY 1;
-- Grant access to the sequence
DO $$
    BEGIN
        IF EXISTS (
            SELECT FROM pg_roles WHERE rolname = 'etutor_clustering_test'
        ) THEN
            GRANT USAGE, SELECT ON SEQUENCE data_point_id_seq TO etutor_clustering_test;
        END IF;

        IF EXISTS (
            SELECT FROM pg_roles WHERE rolname = 'etutor_clustering'
        ) THEN
            GRANT USAGE, SELECT ON SEQUENCE data_point_id_seq TO etutor_clustering;
        END IF;
    END $$;
CREATE TABLE data_point (
                            id BIGINT PRIMARY KEY DEFAULT nextval('data_point_id_seq'),
                            cluster_id BIGINT NOT NULL REFERENCES cluster(id) ON DELETE CASCADE,
                            x DOUBLE PRECISION NOT NULL,
                            y DOUBLE PRECISION NOT NULL,
                            name CHAR NOT NULL
);

-- Create table for solution iterations
CREATE SEQUENCE solution_iteration_id_seq START WITH 1 INCREMENT BY 1;

-- Grant access to the sequence
DO $$
    BEGIN
        IF EXISTS (
            SELECT FROM pg_roles WHERE rolname = 'etutor_clustering_test'
        ) THEN
            GRANT USAGE, SELECT ON SEQUENCE solution_iteration_id_seq TO etutor_clustering_test;
        END IF;

        IF EXISTS (
            SELECT FROM pg_roles WHERE rolname = 'etutor_clustering'
        ) THEN
            GRANT USAGE, SELECT ON SEQUENCE solution_iteration_id_seq TO etutor_clustering;
        END IF;
    END
$$;

CREATE TABLE solution_iteration (
                                    id BIGINT PRIMARY KEY DEFAULT nextval('solution_iteration_id_seq'),
                                    task_id BIGINT NOT NULL REFERENCES task(id) ON DELETE CASCADE,
                                    order_id INTEGER NOT NULL,
                                    iteration_string TEXT NOT NULL
);
