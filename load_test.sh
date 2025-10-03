#!/bin/bash

# =======================================================
# CONFIGURATION
# =======================================================

# Set TOTAL_REQUESTS: use the first argument ($1) if provided, otherwise default to 100.
TOTAL_REQUESTS=${1:-100}

# Define mapping of ports to image names for automated launch
# Note: The port is the *host* port (e.g., 8111), the container port is always 8080.
CONTAINER_MAP=(
    "8111:spring-boot-efficient-search-api_1_default"
    "8222:spring-boot-efficient-search-api_2_with_layer"
    "8333:spring-boot-efficient-search-api_3_distroless"
    "8444:spring-boot-efficient-search-api_4_custom_jre"
    "8555:spring-boot-efficient-search-api_5_custom_jre_with_layer"
)

# List of ports to be used for requests (extracted from the map)
PORTS=(8111 8222 8333 8444 8555)

# List of available countries for the search by country endpoint
COUNTRIES=(
    "Japan"
    "Germany"
    "Republic of Korea"
    "Italy"
    "United States of America"
    "Sweden"
    "United Kingdom of Great Britain and Northern Ireland"
    "Canada"
    "France"
    "Russian Federation"
    "Spain"
    "Switzerland"
    "Norway"
    "India"
    "China"
    "South Africa"
    "Australia"
    "Mexico"
)

# Maximum ID for the /api/cars/XXX endpoint
MAX_CAR_ID=192

# =======================================================
# CONTAINER MANAGEMENT
# =======================================================

launch_containers() {
    echo "--- Checking and launching required Docker containers ---"

    for entry in "${CONTAINER_MAP[@]}"; do
        IFS=':' read -r PORT IMAGE_NAME <<< "$entry"

        # Check if a container for this image is already running on the host port
        # Filters by port AND image name (optional: check only port for robustness)
        RUNNING_CHECK=$(docker ps -q --filter "ancestor=$IMAGE_NAME" --filter "publish=$PORT/tcp")

        if [ -n "$RUNNING_CHECK" ]; then
            echo "✅ Container for $IMAGE_NAME (Port $PORT) is already running."
        else
            echo "⚠️ Container for $IMAGE_NAME (Port $PORT) NOT found. Launching..."

            # Use 'docker run' to launch the container in detached mode (-d)
            docker run -d -p "$PORT:8080" "$IMAGE_NAME"

            if [ $? -eq 0 ]; then
                echo "🚀 Launched $IMAGE_NAME successfully."
                # Give the container a moment to start up
                sleep 2
            else
                echo "❌ ERROR: Failed to launch $IMAGE_NAME. Check if the image exists or if the port $PORT is free."
                exit 1
            fi
        fi
    done
    echo "--- Container check complete ---"
}

# =======================================================
# DEPENDENCY INSTALLATION
# =======================================================

install_dependencies() {
    if command -v shuf &> /dev/null; then
        echo "✅ 'shuf' utility found."
        return 0
    fi

    echo "⚠️ 'shuf' utility not found. Attempting to install 'coreutils'..."

    if command -v apt &> /dev/null; then
        sudo apt update && sudo apt install -y coreutils
    elif command -v dnf &> /dev/null; then
        sudo dnf install -y coreutils
    elif command -v yum &> /dev/null; then
        sudo yum install -y coreutils
    elif command -v brew &> /dev/null; then
        brew install coreutils
    else
        echo "❌ ERROR: Cannot find a supported package manager. Please install 'coreutils' manually."
        exit 1
    fi

    if command -v shuf &> /dev/null; then
        echo "✅ 'shuf' installed successfully."
    else
        echo "❌ ERROR: 'shuf' installation failed."
        exit 1
    fi
}

# =======================================================
# EXECUTION
# =======================================================

# 1. Install necessary shell tools
install_dependencies

# 2. Check and launch all required containers
launch_containers

# 3. LOGGING FUNCTION
log_request() {
    # $1 = Port, $2 = Request Type, $3 = Full Endpoint
    echo "[$1] Request #$REQUEST_COUNT/$TOTAL_REQUESTS ($2): $3"
}

# 4. MAIN TEST LOOP
echo "--- Starting Load Test: $TOTAL_REQUESTS requests ---"

for ((REQUEST_COUNT=1; REQUEST_COUNT<=$TOTAL_REQUESTS; REQUEST_COUNT++)); do

    # a. Choose a random port (targets the container)
    RANDOM_PORT=${PORTS[$RANDOM % ${#PORTS[@]}]}
    BASE_URL="localhost:$RANDOM_PORT/api/cars"

    # b. Choose a random endpoint type (ID or Country)
    REQUEST_TYPE=$(shuf -i 1-2 -n 1) # 1 for ID, 2 for Country

    ENDPOINT=""
    QUERY_TYPE=""

    if [ "$REQUEST_TYPE" -eq 1 ]; then
        # Endpoint by ID: localhost:ZZZZ/api/cars/XXX
        RANDOM_ID=$(shuf -i 1-$MAX_CAR_ID -n 1)
        ENDPOINT="$BASE_URL/$RANDOM_ID"
        QUERY_TYPE="ID"
    else
        # Endpoint by Country: localhost:ZZZZ/api/cars?country=YYY
        RANDOM_COUNTRY="${COUNTRIES[$RANDOM % ${#COUNTRIES[@]}]}"
        ENCODED_COUNTRY=$(echo "$RANDOM_COUNTRY" | sed 's/ /%20/g')

        ENDPOINT="$BASE_URL?country=$ENCODED_COUNTRY"
        QUERY_TYPE="Country"
    fi

    log_request "$RANDOM_PORT" "$QUERY_TYPE" "$ENDPOINT"

    # c. Execute the HTTP request with curl (output time and code)
    CURL_OUTPUT=$(curl -s -o /dev/null -w "%{http_code}:%{time_total}s" "$ENDPOINT")

    HTTP_CODE=$(echo "$CURL_OUTPUT" | cut -d ':' -f 1)
    TIME_TAKEN=$(echo "$CURL_OUTPUT" | cut -d ':' -f 2)

    echo "  -> RESULT: Code $HTTP_CODE | Time: $TIME_TAKEN"

done

echo "--- Load Test Finished ---"