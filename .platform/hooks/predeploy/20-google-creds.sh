#!/bin/bash
set -euo pipefail

PARAM="/uninote/prod/google_service_account_json"
DEST="/opt/elasticbeanstalk/private/google-sa.json"

mkdir -p "$(dirname "$DEST")"

# Fetch secret from SSM into a file
aws ssm get-parameter --name "$PARAM" --with-decryption \
  --query 'Parameter.Value' --output text > "$DEST"

chmod 600 "$DEST"

# Export for app
echo "export GOOGLE_APPLICATION_CREDENTIALS=$DEST" >> /opt/elasticbeanstalk/deployment/env
