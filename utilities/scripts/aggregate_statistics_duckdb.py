#!/usr/bin/env python3

import os
import json
import glob
import shutil
from pathlib import Path
import sys

def copy_artifact_if_exists(source_path, dest_path):
    """Helper function to copy an artifact if it exists"""
    if source_path.exists():
        if source_path.is_file():
            shutil.copy2(source_path, dest_path)
            return 1
        elif source_path.is_dir():
            # Copy all files from directory
            count = 0
            for file_path in source_path.rglob('*'):
                if file_path.is_file():
                    relative_path = file_path.relative_to(source_path)
                    dest_file_path = dest_path / relative_path
                    dest_file_path.parent.mkdir(parents=True, exist_ok=True)
                    shutil.copy2(file_path, dest_file_path)
                    count += 1
            return count
    return 0

def collect_artifacts():
    """Collect artifacts from dependency builds"""
    print("##teamcity[blockOpened name='Collecting artifacts from dependencies']")
    
    # Create directories
    os.makedirs('trajectories', exist_ok=True)
    os.makedirs('dependency_artifacts', exist_ok=True)
    
    # Look for dependency build directories
    parent_dir = Path('..').resolve()
    dependency_dirs = [d for d in parent_dir.iterdir() if d.is_dir() and d.name != Path.cwd().name]
    
    # Define artifact mapping: (source_filename, dest_template, dest_dir)
    artifact_mappings = [
        ('data.parquet', 'dependency_artifacts/{}_data.parquet', None),
        ('stats_per_run.json', 'dependency_artifacts/{}_stats_per_run.json', None),
        ('stats_per_task.jsonl', 'dependency_artifacts/{}_stats_per_task.jsonl', None),
        ('trajectory.json', 'trajectories/{}_trajectory.json', None),
        ('trajectory_extended.json', 'trajectories/{}_trajectory_extended.json', None),
        ('trajectories', 'trajectories/{}', 'trajectories')  # Special case for directory
    ]
    
    collected_files = 0
    for dep_dir in dependency_dirs:
        dep_name = dep_dir.name
        
        for source_name, dest_template, dest_dir in artifact_mappings:
            source_path = dep_dir / source_name
            
            if source_name == 'trajectories':  # Special handling for trajectories directory
                if source_path.exists():
                    for traj_file in source_path.rglob('*'):
                        if traj_file.is_file():
                            dest_path = Path('trajectories') / f'{dep_name}_{traj_file.name}'
                            shutil.copy2(traj_file, dest_path)
                            collected_files += 1
            else:
                dest_path = dest_template.format(dep_name)
                collected_files += copy_artifact_if_exists(source_path, dest_path)
    
    print(f"Collected {collected_files} artifact files from {len(dependency_dirs)} dependency directories")
    print("##teamcity[blockClosed name='Collecting artifacts from dependencies']")
    
    return collected_files > 0

def aggregate_with_duckdb():
    """Aggregate statistics using DuckDB"""
    print("##teamcity[blockOpened name='Aggregating statistics with DuckDB']")
    
    try:
        import duckdb
        import pandas as pd
    except ImportError as e:
        print(f"Failed to import required libraries: {e}")
        return None
    
    # Initialize DuckDB connection
    conn = duckdb.connect(':memory:')
    
    try:
        # Look for parquet files first
        parquet_files = glob.glob('dependency_artifacts/*_data.parquet')
        
        if parquet_files:
            print(f"Found {len(parquet_files)} parquet files")
            
            # Create a union of all parquet files
            union_queries = []
            for i, pf in enumerate(parquet_files):
                source_name = Path(pf).stem.replace('_data', '')
                union_queries.append(f"SELECT *, '{source_name}' as source_build FROM '{pf}'")
            
            union_query = " UNION ALL ".join(union_queries)
            conn.execute(f"CREATE TABLE all_results AS {union_query}")
            
            # Basic statistics
            result = conn.execute("""
                SELECT 
                    COUNT(*) as total_tasks,
                    SUM(CASE WHEN status = 'SUCCESS' THEN 1 ELSE 0 END) as successful_tasks,
                    ROUND(AVG(CASE WHEN status = 'SUCCESS' THEN 1.0 ELSE 0.0 END) * 100, 2) as success_rate_percent,
                    COUNT(DISTINCT source_build) as total_builds
                FROM all_results
            """).fetchone()
            
            stats = {
                'total_tasks': result[0],
                'successful_tasks': result[1], 
                'success_rate_percent': float(result[2]),
                'total_builds': result[3],
                'data_source': 'parquet'
            }
            
        else:
            # Fallback to JSON files
            print("No parquet files found, analyzing JSON files...")
            
            json_files = glob.glob('dependency_artifacts/*_stats_per_run.json')
            all_stats = []
            
            for json_file in json_files:
                try:
                    with open(json_file, 'r') as f:
                        data = json.load(f)
                        if isinstance(data, list):
                            all_stats.extend(data)
                        else:
                            all_stats.append(data)
                except Exception as e:
                    print(f"Error reading {json_file}: {e}")
            
            if all_stats:
                total_tasks = len(all_stats)
                successful_tasks = sum(1 for stat in all_stats if stat.get('status') == 'SUCCESS')
                success_rate = (successful_tasks / total_tasks * 100) if total_tasks > 0 else 0
                
                stats = {
                    'total_tasks': total_tasks,
                    'successful_tasks': successful_tasks,
                    'success_rate_percent': round(success_rate, 2),
                    'total_builds': len(json_files),
                    'data_source': 'json'
                }
            else:
                stats = {
                    'total_tasks': 0,
                    'successful_tasks': 0,
                    'success_rate_percent': 0.0,
                    'total_builds': 0,
                    'data_source': 'none'
                }
                print("No statistics data found")
        
        # Add metadata
        stats.update({
            'run_id': os.environ.get('teamcity_run_id', 'unknown'),
            'git_branch': os.environ.get('teamcity_build_vcs_branch_Matterhorn_CoreCi_HttpsGithubComJetBrainsMatterhornCore', 'unknown'),
            'git_revision': os.environ.get('build_vcs_number_Matterhorn_CoreCi_HttpsGithubComJetBrainsMatterhornCore', 'unknown')
        })
        
        print(f"Aggregated Statistics:")
        print(f"  Total tasks: {stats['total_tasks']}")
        print(f"  Successful tasks: {stats['successful_tasks']}")
        print(f"  Success rate: {stats['success_rate_percent']}%")
        print(f"  Total builds: {stats['total_builds']}")
        print(f"  Data source: {stats['data_source']}")
        
        return stats
        
    finally:
        conn.close()
    
    print("##teamcity[blockClosed name='Aggregating statistics with DuckDB']")

def save_results(stats):
    """Save aggregated statistics and set TeamCity status"""
    if stats is None:
        print("No statistics to save")
        return
    
    # Save aggregated statistics
    with open('aggregated_statistics.json', 'w') as f:
        json.dump(stats, f, indent=2)
    
    print("Statistics saved to aggregated_statistics.json")
    
    # Set TeamCity build status
    success_rate = stats['success_rate_percent']
    successful = stats['successful_tasks']
    total = stats['total_tasks']
    
    print(f"##teamcity[buildStatus text='Success rate: {success_rate}% ({successful} of {total})']")

def main():
    """Main execution function"""
    print("Starting DuckDB-based statistics aggregation...")
    
    # Collect artifacts
    has_artifacts = collect_artifacts()
    
    if not has_artifacts:
        print("Warning: No artifacts collected")
    
    # Aggregate statistics
    stats = aggregate_with_duckdb()
    
    # Save results
    save_results(stats)
    
    print("##teamcity[blockOpened name='Summary']")
    if stats:
        print("Aggregation completed successfully!")
        print(json.dumps(stats, indent=2))
    else:
        print("Warning: No aggregated statistics generated")
    print("##teamcity[blockClosed name='Summary']")

if __name__ == '__main__':
    main()