set -eu

manifest="%teamcity.build.workingDir%/base_images.txt"
archive="%teamcity.build.workingDir%/base_images.tar"

images="$(tr '\n' ' ' < "$manifest")"
if [ -z "$images" ]; then
  echo "No base images listed in $manifest" >&2
  exit 1
fi

echo "Saving base images from $manifest"
docker save -o "$archive" $images
echo "Saved shared base image archive to $archive"
