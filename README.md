# TeamCity SWE-Bench AI Agent Testing

This TeamCity configuration provides a complete framework for testing AI agents against the SWE-Bench Lite dataset, which contains 300+ software engineering tasks from popular Python repositories.

## Overview

The system evaluates AI agents by:
1. Preparing isolated Docker environments for each SWE-Bench task
2. Running AI agents against specific coding problems  
3. Evaluating solutions using the official SWE-Bench evaluation harness
4. Collecting performance metrics and success rates

## Architecture

### Projects Structure

- **JetBrains Junie AI Agent** (`JetBrain_Junie_AI_Agent.kt`)
  - Downloads Junie CLI from GitHub releases and IntelliJ IDEA
  - Creates task subsets for progressive testing
  - Individual task execution builds for all 300+ SWE-Bench tasks
  
- **Google Gemini CLI AI Agent** (`Google_Gemini_CLI_AI_Agent.kt`)
  - Builds from the official Google Gemini CLI repository (`https://github.com/google-gemini/gemini-cli.git`)
  - Uses Node.js execution environment with npm build process
  - Creates task subsets for progressive testing

- **SWE-Bench Lite** (`SWE_Bench_Lite.kt`): Core dataset and environment management
  - **Dataset Build** (`SWE_Bench_Lite_Dataset`): Downloads and caches the SWE-Bench Lite dataset
  - **Task Environments** (`TaskEnvironments`): 300+ builds for Docker environment preparation
  - **Task Execution**: Generic agent-agnostic task execution framework

### Step Scripts

- `scripts/download_dataset.py`: Downloads SWE-Bench Lite dataset from Hugging Face Hub
- `scripts/extract_task_data.py`: Extracts problem statements and hints for specific tasks
- `scripts/calculate_statistics.py`: Aggregates evaluation results and calculates success rates
- `scripts/formating_solution.py`: Formats agent output patches for SWE-Bench evaluation
- `scripts/tag_task_execution.py`: Tags builds with success/failure indicators (✅❌⚠️)
- `scripts/run_junie.sh`: Executes JetBrains Junie agent
- `scripts/run_gemini.sh`: Executes Google Gemini CLI agent
- `scripts/get_patch.sh`: Extracts a git patch from task execution

## Prerequisites

### TeamCity Server Requirements
- TeamCity 2025.03+
- Kotlin DSL support enabled
- Docker support on build agents

### Build Agent Requirements
- **Resources**: 4+ CPU cores, 16+ GB RAM, 10+ GB free disk space
- **Software**: Docker, Python 3.11+, git, Node.js (for Gemini CLI)

**Note!**
https://demo.teamcity.com specific: agent names start with `DemoAgent4Cpu16Gb` (configurable in `SWE_Bench_Lite_TaskEnv.kt:16` and `SWE_Bench_Lite_Tasks_For_Agent.kt:18`)

## Setup Instructions

### 1. Import Configuration

1. Fork this repository
2. Clone the repository and introduce changes
3. Push changes to your fork
2. In TeamCity Admin → Projects → Import Project from Repository

### 2. Configure Build Agents

#### Option A: AWS Cloud Agents
The configuration includes AWS EC2 cloud agents in `settings.kts:13-55`:

1. Update AWS credentials in `settings.kts:18-21`
2. Update launch template IDs in `settings.kts:38,46,54` to match your AWS setup
3. Verify region settings (`eu-west-1` by default)

#### Option B: Your Own Agents
[Installing and Configuring TeamCity Build Agents](https://www.jetbrains.com/help/teamcity/install-and-start-teamcity-agents.html)

### 3. Configure Shared Resources

1. **HuggingFace Connections** (`settings.kts:57-61`):
   - Quota limit of 20 concurrent connections to avoid rate limiting
   - Prevents 429 errors from Hugging Face Hub during dataset downloads

### 4. Configure Agent

For JetBrains Junie:
- Configure your Junie authentication token

For Google Gemini CLI:
- Configure API credentials as needed

### 5. Test Basic Setup

1. Run "SWE Bench Lite Dataset" build to cache the dataset
2. Run one task environment build (e.g., `Task env: django__django-10924`)
3. Run the corresponding task execution build for your chosen agent
4. Verify artifacts are produced and evaluation completes

## Task Execution Workflow

1. **Environment Preparation** (`createTaskEnvBuildType`):
   - Downloads SWE-Bench dataset 
   - Extracts task-specific problem statements and hints
   - Builds Docker images using `swebench.harness.prepare_images`
   - Saves Docker images as tar artifacts. These artifacts are used by subsequent builds. 
     We build them is to avoid rebuilding the environment on every task execution.

2. **Agent Execution** (`createTaskForAgentBuildType`):
   - Loads Docker environment from previous step
   - Runs AI agent with problem statement
   - Extracts generated patches using `get_patch.sh`
   - Formats solutions for SWE-Bench evaluation
   - Runs official SWE-Bench evaluation harness
   - Tags builds with success/failure indicators

3. **Aggregation** (`create_SWE_Bench_Lite_XxTaskSlice`):
   - Collects results from multiple task executions
   - Calculates overall success rates and statistics
   - Updates TeamCity build statistics for reporting

## Progressive Testing Strategy

Both agents support incremental testing via task slicing:

1. **10 tasks**: Quick validation and debugging (`tenTasksSet`)
2. **30 tasks**: Initial performance assessment (`thirtyTasksSet`) 
3. **50 tasks**: Comprehensive evaluation (`fiftyTasksSet`)
4. **Full dataset (300+ tasks)**: Complete validation

Task selection uses sampling only for demo purposes. You can modify the task slicing logic to suit your needs:
- 10 tasks: Every 10th task
- 30 tasks: Every 7th task
- 50 tasks: Every 5th task

## Monitoring and Results

### Build Artifacts
- **Docker Images**: `${taskId}.tar` - Prepared task environments
- **Problem Statements**: `${taskId}_issue.md`, `${taskId}_hints.md`
- **Patches**: `${taskId}.patch` - Generated code changes
- **Solutions**: `${taskId}_solution.jsonl` - Formatted for SWE-Bench evaluation
- **Reports**: `agent.${taskId}.json` - Detailed evaluation results

### Metrics
- **Success Rate**: Percentage of resolved tasks (tracked in TeamCity statistics)
- **Build Tags**: 
  - ✅ Good solution - if a generated solution passed task's tests 
  - ❌ Bad solution - if an agent successfully fixed the issue, but the tests still fail, 
  - ⚠️ Error - errors are appeared during evaluation
- **Statistics**: `SWEBLite_${size}x_SuccessRate`, `SWEBLite_${size}x_Resolved`, etc.
- **Custom Charts**: Task solving success rate visualization

## Customizing for Your AI Agent

### 1. Create New Agent Project

Follow the pattern from existing agents:

```kotlin
object YourAgent_AI_Agent : Project({
    id("YourAgent")
    name = "Your Agent AI Agent"
    
    buildType(BuildYourAgent)
    
    // Task slicing logic
    val tenTasksSet = mutableListOf<Task>()
    // ... create task configurations
    
    buildType(create_SWE_Bench_Lite_XxTaskSlice(tenTasksSet))
})
```

### 2. Implement Agent Build

```kotlin
object BuildYourAgent: BuildType({
    name = "Build Your Agent"
    
    steps {
        script {
            scriptContent = """
                # Download/build your agent
                curl -L -o agent.zip "https://your-agent-url"
                # Setup commands
            """.trimIndent()
        }
    }
    
    artifactRules = "agent.zip"
})
```

### 3. Create Agent Script

Create `scripts/run_youragent.sh`:
```bash
cd /testbed
echo "##teamcity[blockOpened name='Running Your Agent']"
your-agent-command \
    --input %teamcity.build.workingDir%/%instance_id%_issue.md \
    --project /testbed \
    --output-patch %teamcity.build.workingDir%/%instance_id%.patch
echo "##teamcity[blockClosed name='Running Your Agent']"
```

### 4. Wire Agent Tasks

```kotlin
fun createTaskForYourAgentBuildType(taskEnv: Task) = createTaskForAgentBuildType(
    "YourAgent",
    taskEnv,
    listOf(/* your agent parameters */),
    File("scripts/run_youragent.sh")
)
```

## Support

### TeamCity Features Used
- **Kotlin DSL**: [Configuration as code](https://www.jetbrains.com/help/teamcity/kotlin-dsl.html)
- **Shared Resources**: [Rate limiting for external services](https://www.jetbrains.com/help/teamcity/shared-resources.html)
- **Service Messages**: [Build tagging and statistics](https://www.jetbrains.com/help/teamcity/service-messages.html)
- **Docker Integration**: [Containerized task environments](https://www.jetbrains.com/help/teamcity/docker.html)
- **Artifact Dependencies**: [Build artifact sharing](https://www.jetbrains.com/help/teamcity/artifact-dependencies.html)
- **Cloud Agents**: [AWS EC2 integration](https://www.jetbrains.com/help/teamcity/setting-up-teamcity-for-amazon-ec2.html)

### External Documentation
- **SWE-Bench**: https://github.com/princeton-nlp/SWE-bench
- **TeamCity Kotlin DSL**: https://www.jetbrains.com/help/teamcity/kotlin-dsl.html
- **JetBrains Junie**: https://github.com/jetbrains-junie/junie
- **Google Gemini CLI**: https://github.com/google-gemini/gemini-cli
