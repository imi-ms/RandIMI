CREATE TABLE minimization_parameter
(
    force_ratio        BOOLEAN                NOT NULL,
    id                 BIGINT                 NOT NULL,
    imbalance_bias     DECIMAL                NOT NULL,
    imbalance_function character varying(255) NOT NULL,
    study_id           BIGINT                 NOT NULL,
    CONSTRAINT minimization_parameter_pkey PRIMARY KEY (id),
    CONSTRAINT minimization_parameter_fkey FOREIGN KEY (study_id) REFERENCES study (id)
);
