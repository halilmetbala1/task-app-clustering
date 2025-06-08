CREATE TYPE task_status AS ENUM ('draft', 'ready_for_approval', 'approved');
CREATE TYPE submission_mode AS ENUM ('run', 'diagnose', 'submit');

CREATE CAST (CHARACTER VARYING as task_status) WITH INOUT AS IMPLICIT;
CREATE CAST (CHARACTER VARYING as submission_mode) WITH INOUT AS IMPLICIT;

CREATE TABLE task
(
    id                          BIGINT          NOT NULL,
    max_points                  NUMERIC(7, 2)   NOT NULL,
    status                      TASK_STATUS     NOT NULL,
    solution_instructions_de    TEXT            NOT NULL default '',
    solution_instructions_en    TEXT            NOT NULL default '',
    deduction_wrong_clusters    INTEGER         NOT NULL,
    deduction_wrong_labels      INTEGER         NOT NULL,
    deduction_wrong_centroids   INTEGER         NOT NULL,
    CONSTRAINT task_pk PRIMARY KEY (id)
);

CREATE TABLE submission
(
    id                UUID                     DEFAULT gen_random_uuid(),
    user_id           VARCHAR(255),
    assignment_id     VARCHAR(255),
    task_id           BIGINT,
    submission_time   TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    language          VARCHAR(2)      NOT NULL DEFAULT 'en',
    mode              submission_mode NOT NULL,
    feedback_level    INT             NOT NULL,
    evaluation_result JSONB,
    submission        TEXT, -- custom column
    CONSTRAINT submission_pk PRIMARY KEY (id),
    CONSTRAINT submission_task_fk FOREIGN KEY (task_id) REFERENCES task (id)
        ON DELETE CASCADE
);
