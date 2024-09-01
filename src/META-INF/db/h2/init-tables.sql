DROP TABLE IF EXISTS regions;
DROP TABLE IF EXISTS nodes;
DROP TABLE IF EXISTS ways;
DROP TABLE IF EXISTS isolated_nodes;

DROP SEQUENCE IF EXISTS global_seq;

-- CREATE SEQUENCE global_seq START WITH 100000;

CREATE TABLE nodes
(
--     id                INTEGER DEFAULT nextval('global_seq') PRIMARY KEY,
    osm_id            BIGINT,
    osm_way_ids       BIGINT ARRAY,
--     visited           VARCHAR,
    con_component_id  UUID,
    neighbor_node_ids BIGINT ARRAY
);
CREATE UNIQUE INDEX marked_nodes_unique_wayid_order_idx ON nodes (con_component_id, osm_id);

CREATE TABLE isolated_nodes
(
--     id           INTEGER DEFAULT nextval('global_seq') PRIMARY KEY,
    osm_id       BIGINT PRIMARY KEY NOT NULL,
    way_osm_id   INTEGER,
    region_id    INTEGER,
    order_in_way INTEGER,
    lat          float4             NOT NULL,
    lon          float4             NOT NULL
);
CREATE UNIQUE INDEX nodes_unique_wayid_order_idx ON isolated_nodes (way_osm_id, order_in_way, osm_id);

CREATE TABLE IF NOT EXISTS regions
(
    id        INTEGER PRIMARY KEY UNIQUE,
    name      VARCHAR,
    neighbors INTEGER ARRAY,
    path      VARCHAR
);

CREATE TABLE IF NOT EXISTS components
(
    id        VARCHAR,
    region_id INTEGER,
--     size      INTEGER
    isolated  BOOLEAN
);