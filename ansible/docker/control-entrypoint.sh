#!/bin/sh
# Creates an SSH key pair (once) in the shared /keys volume, then keeps the container running.
set -e
if [ ! -f /keys/id_ed25519 ]; then
  ssh-keygen -q -t ed25519 -N "" -C "ansible-control" -f /keys/id_ed25519
  echo "Generated SSH key for Ansible"
fi
chmod 600 /keys/id_ed25519
exec sleep infinity
