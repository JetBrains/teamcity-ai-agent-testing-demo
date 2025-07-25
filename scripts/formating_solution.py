import json

# Read the patch file
with open('%teamcity.build.workingDir%/%instance_id%.patch', 'r') as f:
    patch_content = f.read()

# Create the prediction entry
prediction = {
    'instance_id': '%instance_id%',
    'model_patch': patch_content,
    'model_name_or_path': 'agent'
}

with open('%teamcity.build.workingDir%/%solution_file%', 'w') as f:
    f.write(json.dumps(prediction) + '\n')

print('Created predictions.jsonl successfully!')
