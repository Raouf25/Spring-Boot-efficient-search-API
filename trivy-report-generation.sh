#!/bin/sh

# --- Configuration ---
# Official Trivy HTML template URL
TEMPLATE_URL="https://raw.githubusercontent.com/aquasecurity/trivy/main/contrib/html.tpl"
TEMPLATE_FILE="html.tpl"
# Directory name for saving reports
REPORT_DIR="./target/trivy-reports"

# --- 1. Install Trivy with Brew if not installed ---
echo "✅ Checking Trivy installation..."

# Try to run trivy --version and check if the command succeeded (exit code 0) and returned a version
if ! command -v trivy >/dev/null 2>&1 || [ -z "$(trivy --version 2>/dev/null)" ]; then
    echo "Trivy not found or version not returned. Attempting to install with Homebrew..."

    # Check if Homebrew is installed
    if ! command -v brew >/dev/null 2>&1; then
        echo "❌ Error: Homebrew ('brew') is not installed. Please install it first." >&2
        exit 1
    fi

    # Install Trivy
    if brew install trivy; then
        echo "Trivy installed successfully."
    else
        echo "❌ Error: Trivy installation with Homebrew failed." >&2
        exit 1
    fi
else
    echo "Trivy is already installed: $(trivy --version | head -n 1)"
fi

# Get the absolute path of the trivy binary
TRIVY_BIN_PATH=$(command -v trivy)
if [ -z "$TRIVY_BIN_PATH" ]; then
    echo "❌ Critical Error: Could not locate the trivy binary." >&2
    exit 1
fi
TRIVY_DIR=$(dirname "$TRIVY_BIN_PATH")
echo "Trivy binary directory detected: $TRIVY_DIR"



## 2. Create Reports Directory

echo "⚙️ Checking and creating reports directory: $REPORT_DIR"

# 'mkdir -p' creates the directory (and any necessary parent directories) only if it doesn't exist.
# The '-p' flag prevents errors if the directory is already there.
if mkdir -p "$REPORT_DIR"; then
    echo "The '$REPORT_DIR' directory is ready."
else
    echo "❌ Critical Error: Failed to create the reports directory." >&2
    exit 1
fi



## 3. Manage HTML Template

TEMPLATE_PATH="$TRIVY_DIR/$TEMPLATE_FILE"

if [ -f "$TEMPLATE_PATH" ]; then
    echo "ℹ️ The template $TEMPLATE_FILE is already present at: $TEMPLATE_PATH. Skipping download."
else
    echo "Downloading HTML template ($TEMPLATE_FILE) from GitHub..."

    # Download temporarily to the current directory
    if curl -sSL "$TEMPLATE_URL" -o "./$TEMPLATE_FILE"; then
        echo "The $TEMPLATE_FILE template has been downloaded to the current directory."
    else
        echo "❌ Error: Downloading the $TEMPLATE_FILE template failed." >&2
        exit 1
    fi

    # Move the template next to the Trivy binary
    echo "Moving $TEMPLATE_FILE to $TRIVY_DIR..."

    if mv "./$TEMPLATE_FILE" "$TRIVY_DIR/"; then
        echo "The $TEMPLATE_FILE template was successfully moved."
    else
        echo "⚠️ Direct move failed. Attempting with 'sudo' (permissions might be required)..." >&2

        if sudo mv "./$TEMPLATE_FILE" "$TRIVY_DIR/"; then
            echo "Move with 'sudo' succeeded."
        else
            echo "❌ Error: Move failed even with 'sudo'. Script aborted." >&2
            exit 1
        fi
    fi
fi



## 4. Conclusion and Usage Example

echo "---"
echo "🎉 Script finished. The Trivy binary and HTML template are ready."
echo "Template path for the command: $TEMPLATE_PATH"
echo "Example command to generate an HTML report:"
echo "trivy image --format template --template \"@$TEMPLATE_PATH\" -o $REPORT_DIR/trivy_report_alpine.html alpine:latest"


trivy image --format template --template "@$TEMPLATE_PATH" -o $REPORT_DIR/trivy_report_1_default.html spring-boot-efficient-search-api_1_default:latest

trivy image --format template --template "@$TEMPLATE_PATH" -o $REPORT_DIR/trivy_report_2_with_layer.html spring-boot-efficient-search-api_2_with_layer:latest

trivy image --format template --template "@$TEMPLATE_PATH" -o $REPORT_DIR/trivy_report_3_distroless.html spring-boot-efficient-search-api_3_distroless:latest

trivy image --format template --template "@$TEMPLATE_PATH" -o $REPORT_DIR/trivy_report_4_custom_jre.html spring-boot-efficient-search-api_4_custom_jre:latest

trivy image --format template --template "@$TEMPLATE_PATH" -o $REPORT_DIR/trivy_report_5_custom_jre_with_layer.html  spring-boot-efficient-search-api_5_custom_jre_with_layer:latest