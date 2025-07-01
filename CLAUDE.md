# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a TeamCity CI/CD configuration project for evaluating AI agents against the SWE-Bench benchmark. SWE-Bench is a dataset of real-world Python software engineering tasks used to evaluate AI agents' ability to solve coding problems.

## Common Commands

### Build and Compile
```bash
mvn compile
```

### Generate TeamCity Configurations
```bash
mvn teamcity-configs:generate
```

### Run Parquet to JSON Conversion
The project includes data conversion utilities:
```bash
# Convert Parquet files to JSON format
mvn exec:java -Dexec.mainClass="ParquetParserKt"
```

### Analytics and Statistics
```bash
# Install DuckDB dependencies
python3 -m venv .venv
source .venv/bin/activate
pip install duckdb pandas pyarrow

# Run statistical analysis
python utilities/scripts/aggregate_statistics_duckdb.py
```

## Architecture

### Core Components

**settings.kts** - Main TeamCity project configuration entry point

**Data Layer** (`/data/`)
- `dev-00000-of-00001.json`, `test-00000-of-00001.json` - SWE-Bench datasets
- `slices/` - Predefined instance subsets (devSub10, testVerified399, etc.)
- `slices.kt` - Data loading and filtering logic

**Build Configuration** (`/utilities/`)
- `buildConfigurations.kt` - Build type factory methods
- `builds/` - Individual build type definitions
- `AgentParameters.kt` - Agent configuration parameters

**Execution Scripts** (`/utilities/scripts/`)
- `run_junie.sh` - Agent execution
- `run_eval.sh` - Evaluation execution
- `validate_instance.sh` - Instance validation
- `calculate_success_rate.py` - Success rate calculation
- `save_and_log_results.sh` - Local artifact collection and metadata creation
- `aggregate_statistics_duckdb.py` - DuckDB-based statistical aggregation

### Data Schema

Each SWE-Bench instance contains:
- `instance_id` - Unique identifier
- `repo` - Repository name  
- `problem_statement` - Task description
- `patch` - Gold standard solution
- `test_patch` - Test modifications
- `FAIL_TO_PASS`/`PASS_TO_PASS` - Test expectations

### Build Pipeline Types

1. **Validation** - Validates SWE-Bench instances
2. **Execution** - Runs agents on instances
3. **Evaluation** - Evaluates outputs against gold standard
4. **Aggregation** - Collects results and calculates statistics

### Agent Modes

- `ElectricJunior` - Standard agent
- `ElectricJuniorChat` - Chat-based interaction
- `ElectricJuniorCloud` - Cloud execution
- `ElectricJuniorAIA` - AI-assisted mode

## Key Files to Understand

- `settings.kts` - Main configuration
- `utilities/helpers.kt` - Core utility functions
- `data/slices.kt` - Data filtering and loading
- `utilities/builds/` - Build type definitions
- `ParquetParser.kt` - Data conversion utility
- `utilities/scripts/aggregate_statistics_duckdb.py` - Statistical analysis with DuckDB

## Environment Configuration

The project uses several environment parameters:
- Redis configuration for caching
- Model provider settings (OpenAI)
- Local artifact storage with DuckDB analytics
- YT (Yandex Tables) for external data storage (optional)
- Docker and conda for execution environments

## Testing

Testing is performed through:
- Instance validation builds
- Multi-seed evaluation runs
- Docker-based isolated environments
- Statistical significance testing

## Development Notes

- Uses TeamCity DSL (Kotlin) for CI/CD configuration
- Parquet files are converted to JSON for processing
- Agent execution is containerized using Docker
- Results are collected locally and aggregated using DuckDB
- Supports matrix builds for multiple runs per instance
- Enhanced artifact collection including trajectories and extended data
- Local-first approach with optional external storage integration