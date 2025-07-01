# SWE-Bench TeamCity Evaluation Project

A comprehensive TeamCity CI/CD configuration project for evaluating AI agents against the [SWE-Bench](https://www.swebench.com/) benchmark. [SWE-Bench](https://www.swebench.com/) is a dataset of real-world Python software engineering tasks used to evaluate AI agents' ability to solve coding problems.

## Getting Started

1. **Setup Infrastructure**: Configure TeamCity agents with Docker support
2. **Configure Services**: Set up Redis, YTsaurus (optional), and API access
3. **Import Project**: Load configuration into TeamCity
4. **Set Parameters**: Configure environment variables
5. **Run Validation**: Execute validation pipeline to verify setup
6. **Start Evaluation**: Run small-scale evaluation (e.g., DevSub10)
7. **Scale Up**: Execute larger evaluations as needed

## Project Overview

This project automates the execution, evaluation, and validation of AI agents (specifically "Matterhorn" agents) against software engineering tasks from the [SWE-Bench](https://www.swebench.com/) dataset. It provides:

- **Automated evaluation pipelines** for AI coding agents
- **Statistical analysis** with success rate calculations
- **Multi-environment support** (Docker, conda, TeamCity)
- **Scalable infrastructure** supporting hundreds of test instances
- **Comprehensive artifact collection** and result reporting

## Prerequisites

### TeamCity Agent Requirements

The build agents executing these configurations need:

- **Operating System**: Ubuntu/Debian-based Linux system
- **Docker**: Latest stable version with execution permissions
- **Internet Connectivity**: For downloading packages and dependencies
- **Resources**: Minimum 8GB RAM recommended for AI agent execution
- **Permissions**: Sudo access for package installation

Note: Java, Maven, Python, and other dependencies are automatically installed during build execution.

### External Services

#### Required Services
- **Redis Cache**: AWS ElastiCache or compatible Redis instance
  - Configure `MATTERHORN_REDIS` environment variable
  - Purpose: Caching [SWE-Bench](https://www.swebench.com/) evaluation results

- **OpenAI API**: AI model provider
  - Configure `MATTERHORN_DEFAULT_MODEL_PROVIDER`
  - Purpose: Code generation and analysis

#### Analytics Tools
- **DuckDB**: In-memory analytics database
  - Automatically installed during build execution
  - Purpose: Local statistics aggregation and analysis
- **YTsaurus**: Distributed storage system (legacy)
  - Configure `YT_PROXY`, `YT_TOKEN`, `YT_CONFIG_PATCHES`
  - Purpose: External statistics storage (optional)

#### Authentication Requirements
- **TeamCity Token**: For API access (`%TeamCityToken%`)
- **YT Token**: For YTsaurus access (if used)
- **OpenAI API Key**: For model inference

## Setup Instructions

### 1. Import Project to TeamCity

1. **Login to TeamCity**: Access your TeamCity server admin panel
2. **Import Project**: 
   - Go to "Administration" → "Projects"
   - Click "Create project" → "From repository URL"
   - Provide repository URL containing this configuration
3. **Configure VCS Root**: Set up version control settings for your repository

### 2. Configure Environment Parameters

Set the following parameters at the project level in TeamCity:

```bash
# Core Configuration
env.MATTERHORN_REDIS=your-redis-endpoint:6379
env.MATTERHORN_DEFAULT_MODEL_PROVIDER=OpenAI
env.TIMEOUT_FOR_INDEXING_MINUTES=3
env.REPLACE_PATH_TO_PROJECT_BY=/path/to/work/directory/

# YTsaurus Configuration (Optional)
env.YT_PROXY=your-yt-proxy
env.YT_TOKEN=your-yt-token
env.YT_CONFIG_PATCHES={"proxy":{"force_ipv4":true}}

# TeamCity Settings
env.IS_TEAMCITY_EVALUATION=true
```

### 3. Docker Registry Access

Configure access to the required Docker registries:

```bash
# Private Registry (Primary)
registry.jetbrains.team/p/matterhorn/swe-bench

# Public Registry (Fallback)
registry.jetbrains.team/p/matterhorn/swebench-public-images
```

Current stable image version: `v0.0.3-fix-collisions`

### 4. Agent Preparation

The following components are automatically installed during build execution:

#### System Packages
- jq (JSON processing)
- python3-dev (Python development headers)
- build-essential (compilation tools)
- curl, wget (downloading utilities)
- git (version control)
- unzip, tar (archive utilities)

#### Analytics Dependencies
- DuckDB (in-memory analytics)
- pandas (data manipulation)
- pyarrow (Parquet file processing)

#### Python Environment
- Conda package manager
- Python virtual environments
- Required Python packages for [SWE-Bench](https://www.swebench.com/)

#### Java/Maven Dependencies
- TeamCity DSL Kotlin
- Apache Parquet and Hadoop libraries
- Jackson JSON processing

## Available Build Configurations

### Validation Pipelines
- **ValidateDevSlice**: Validate development instances
- **ValidateTestSlice**: Validate test instances
- **ValidateTestLiteSlice**: Validate test-lite instances
- **ValidateExtendedSlice**: Validate extended dataset instances

### Execution Pipelines
- **RunTestLite**: Execute on test-lite instances
- **RunTestLiteO1**: Execute with O1 model on test-lite
- **RunExtended**: Execute on extended dataset
- **RunDev**: Execute on development instances

### Matrix Builds
- **RunDevSub95x3**: Execute 95 dev instances 3 times each
- Multi-seed evaluation for statistical significance

### Agent Modes
- **ElectricJunior**: Standard agent mode
- **ElectricJuniorChat**: Chat-based interaction
- **ElectricJuniorCloud**: Cloud-based execution
- **ElectricJuniorAIA**: AI-assisted mode

## Data Configuration

### SWE-Bench Datasets
The project includes pre-configured [SWE-Bench](https://www.swebench.com/) datasets in `/data/`:

- **dev-00000-of-00001.json**: Development instances
- **test-00000-of-00001.json**: Test instances
- **slices/**: Filtered instance collections

### Available Data Slices
- **Development Sets**: devSub10, devSub95 (10, 95 instances)
- **Test Sets**: testVerified399, testVerified500 (399, 500 instances)
- **Extended Sets**: Full [SWE-Bench](https://www.swebench.com/) extended collections
- **Filtered Sets**: Pre-validated, dockerized instances

## Running Evaluations

### Basic Execution
1. **Select Build Configuration**: Choose appropriate evaluation pipeline
2. **Configure Parameters**: Set agent mode and target instances
3. **Execute Build**: Run evaluation on selected instances
4. **Monitor Progress**: Track execution through TeamCity interface
5. **Review Results**: Check artifacts and statistics

### Example: Running 10 Development Instances
1. Navigate to "RunDevSub10" build configuration
2. Click "Run" to start evaluation
3. Monitor execution logs and agent outputs
4. Review success rate and detailed results

### Advanced Options
- **Custom Instance Selection**: Modify instance lists in configuration
- **Multiple Seeds**: Use matrix builds for statistical analysis
- **Quota Management**: Enable for expensive model usage (O1)
- **Docker Image Selection**: Choose between private/public images

## Results and Monitoring

### Automated Statistics
- **Success Rate Calculation**: Automated pass/fail analysis
- **TeamCity Integration**: Build statistics and metrics
- **Local Storage**: Artifact collection and local result storage
- **DuckDB Analytics**: Advanced statistical analysis and aggregation
- **Cache Performance**: Redis hit/miss ratios

### Artifact Collection
- **Agent Outputs**: Code solutions and patches
- **Evaluation Results**: Test execution outcomes
- **Logs**: Detailed execution and error logs
- **Statistics**: JSON reports with performance metrics
- **Trajectories**: Agent execution trajectories and decision paths
- **Extended Data**: Parquet files with detailed execution metrics
- **Aggregated Statistics**: DuckDB-processed statistical summaries

## Project Architecture

### Core Components

**settings.kts** - Main TeamCity project configuration entry point

**Data Layer** (`/data/`)
- `dev-00000-of-00001.json`, `test-00000-of-00001.json` - [SWE-Bench](https://www.swebench.com/) datasets
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
- `save_and_log_results.sh` - Local artifact collection and storage
- `aggregate_statistics_duckdb.py` - DuckDB-based statistical analysis

### Build Pipeline Types

1. **Validation** - Validates [SWE-Bench](https://www.swebench.com/) instances
2. **Execution** - Runs agents on instances
3. **Evaluation** - Evaluates outputs against gold standard
4. **Aggregation** - Collects results and calculates statistics

## Contributing

### Local Development Setup

For local development and testing of TeamCity DSL configurations:

#### Prerequisites
- **Java**: Java 11+ for Maven and Kotlin compilation
- **Maven**: 3.6+ for building configurations
- **Git**: For version control

#### Local Build Commands

```bash
# Clone the repository
git clone <repository-url>
cd swe-bench-demo

# Compile TeamCity DSL configurations
mvn compile

# Generate TeamCity configuration files
mvn teamcity-configs:generate

# Run Parquet to JSON conversion (if needed)
mvn exec:java -Dexec.mainClass="ParquetParserKt"
```

### Key Files for Development

- `settings.kts` - Main configuration entry point
- `utilities/helpers.kt` - Core utility functions
- `data/slices.kt` - Data filtering and loading
- `utilities/builds/` - Build type definitions
- `ParquetParser.kt` - Data conversion utility

### Development Guidelines

#### Configuration Changes
- Test changes on small instance sets first (e.g., devSub10)
- Update documentation for new build types
- Maintain backward compatibility with existing pipelines
- Follow established naming conventions

#### Data Management
- Use existing data slices when possible
- Add new filtered collections to `/data/slices/`
- Update slice definitions in `slices.kt`
- Validate new instances before adding to production sets

#### Build Type Creation
- Follow patterns in `utilities/builds/` directory
- Use factory methods from `buildConfigurations.kt`
- Configure appropriate agent parameters
- Add proper artifact collection and statistics

#### Script Development
- Place execution scripts in `utilities/scripts/`
- Follow bash scripting best practices
- Include proper error handling and logging
- Test scripts independently before integration

### Testing Configurations

#### Local Validation
```bash
# Validate DSL syntax
mvn compile

# Check configuration generation
mvn teamcity-configs:generate
```
