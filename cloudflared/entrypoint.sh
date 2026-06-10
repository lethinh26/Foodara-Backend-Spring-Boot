#!/bin/sh
# Cloudflared wrapper: start tunnel, capture URL, share with other services

# Start cloudflared in background and capture stdout
cloudflared tunnel --no-autoupdate --metrics 0.0.0.0:20000 --url http://api-gateway:8080 2>&1 | while IFS= read -r line; do
  echo "$line"

  # Extract tunnel URL: https://xxx.trycloudflare.com
  TUNNEL_URL=$(echo "$line" | grep -oP 'https://[a-zA-Z0-9.-]+\.trycloudflare\.com')

  if [ -n "$TUNNEL_URL" ]; then
    echo "TUNNEL_URL=$TUNNEL_URL" > /shared/tunnel_url.env
    echo "Tunnel ready: $TUNNEL_URL"
    break
  fi
done

# Keep running (cloudflared runs in foreground after URL capture)
wait
