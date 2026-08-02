#!/bin/sh
# Generates a self-signed TLS certificate into the shared ssl_certs volume for
# docker-compose.prod.yml's local/compose deployment target, unless one already
# exists (re-running `docker compose up` must not invalidate an existing cert).
#
# Production cloud deployment uses a real certificate (e.g. Let's Encrypt via
# certbot) instead — see nginx-proxy/README.md.
set -eu

CERT_DIR="/certs"
DOMAIN="${DOMAIN_NAME:-localhost}"

if [ -f "$CERT_DIR/fullchain.pem" ] && [ -f "$CERT_DIR/privkey.pem" ]; then
  echo "Existing certificate found at $CERT_DIR — skipping generation."
  exit 0
fi

echo "No certificate found — generating a self-signed certificate for $DOMAIN..."
openssl req -x509 -nodes -days 365 \
  -newkey rsa:2048 \
  -keyout "$CERT_DIR/privkey.pem" \
  -out "$CERT_DIR/fullchain.pem" \
  -subj "/C=US/ST=NA/L=NA/O=BuildNest/CN=$DOMAIN"

chmod 600 "$CERT_DIR/privkey.pem"
echo "Self-signed certificate generated at $CERT_DIR."
