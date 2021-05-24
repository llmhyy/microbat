/*
 Navicat Premium Data Transfer

 Source Server         : microbat
 Source Server Type    : SQLite
 Source Server Version : 3030001
 Source Schema         : main

 Target Server Type    : SQLite
 Target Server Version : 3030001
 File Encoding         : 65001

 Date: 02/01/2021 17:31:10
*/

PRAGMA foreign_keys = false;

-- ----------------------------
-- Table structure for ControlScope
-- ----------------------------
DROP TABLE IF EXISTS "ControlScope";
CREATE TABLE "ControlScope" (
  "location_id" text(100),
  "class_name" text(255),
  "line_number" integer,
  "is_loop" integer
);

-- ----------------------------
-- Table structure for Location
-- ----------------------------
DROP TABLE IF EXISTS "Location";
CREATE TABLE "Location" (
  "location_id" text(100) NOT NULL,
  "trace_id" integer,
  "class_name" text(255),
  "line_number" integer,
  "is_conditional" integer,
  "is_return" integer,
  CONSTRAINT "locid" UNIQUE ("location_id" ASC, "trace_id" ASC) ON CONFLICT IGNORE
);

-- ----------------------------
-- Table structure for LoopScope
-- ----------------------------
DROP TABLE IF EXISTS "LoopScope";
CREATE TABLE "LoopScope" (
  "location_id" text(100),
  "trace_id" integer,
  "class_name" text(255),
  "start_line" text,
  "end_line" text
);

-- ----------------------------
-- Table structure for Run
-- ----------------------------
DROP TABLE IF EXISTS "Run";
CREATE TABLE "Run" (
  "run_id" text(100),
  "project_name" text(255),
  "project_version" text(255),
  "launch_method" text(255),
  "thread_status" integer,
  "launch_class" text(255),
  "created_at" text DEFAULT CURRENT_TIMESTAMP
);

-- ----------------------------
-- Table structure for Step
-- ----------------------------
DROP TABLE IF EXISTS "Step";
CREATE TABLE "Step" (
  "trace_id" text(100),
  "step_order" integer,
  "control_dominator" integer,
  "step_in" integer,
  "step_over" integer,
  "invocation_parent" integer,
  "loop_parent" integer,
  "location_id" integer,
  "read_vars" text,
  "written_vars" text,
  "forking_trace" integer,
  "timestamp" text
);

-- ----------------------------
-- Table structure for StepVariableRelation
-- ----------------------------
DROP TABLE IF EXISTS "StepVariableRelation";
CREATE TABLE "StepVariableRelation" (
  "var_id" text(100),
  "step_order" integer,
  "RW" integer,
  "value_string" text,
  "trace_id" text(100)
);

-- ----------------------------
-- Table structure for Trace
-- ----------------------------
DROP TABLE IF EXISTS "Trace";
CREATE TABLE "Trace" (
  "run_id" text(100),
  "trace_id" text(100),
  "thread_id" text,
  "thread_name" text(255),
  "generated_time" integer,
  "isMain" integer
);

-- ----------------------------
-- Table structure for Variable
-- ----------------------------
DROP TABLE IF EXISTS "Variable";
CREATE TABLE "Variable" (
  "var_id" text,
  "type" text
);

-- ----------------------------
-- Table structure for sqlite_stat1
-- ----------------------------
-- DROP TABLE IF EXISTS "sqlite_stat1";
-- CREATE TABLE "sqlite_stat1" (
--  "tbl",
--  "idx",
--  "stat"
--);

-- ----------------------------
-- Indexes structure for table Step
-- ----------------------------
CREATE UNIQUE INDEX "index1"
ON "Step" (
  "trace_id" ASC,
  "step_order" ASC
);

-- ----------------------------
-- Indexes structure for table StepVariableRelation
-- ----------------------------
CREATE INDEX "traceid"
ON "StepVariableRelation" (
  "trace_id" ASC
);

PRAGMA foreign_keys = true;
