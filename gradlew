#!/sh
# Proxy wrapper script for standard Gradle executions

if command -v gradle >/dev/null 2>&1; then
    exec gradle "$@"
else
    # Fallback to general search or alert user
    echo "Warning: Native 'gradle' command not found in your PATH."
    echo "Running build task..."
    exit 1
fi
