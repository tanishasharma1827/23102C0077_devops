#!/bin/sh
# Waits for the control node's public key, authorises it for the 'deploy' user, then runs sshd.
set -e
until [ -f /keys/id_ed25519.pub ]; do sleep 1; done
mkdir -p /home/deploy/.ssh
cp /keys/id_ed25519.pub /home/deploy/.ssh/authorized_keys
chown -R deploy:deploy /home/deploy/.ssh
chmod 700 /home/deploy/.ssh
chmod 600 /home/deploy/.ssh/authorized_keys
exec /usr/sbin/sshd -D -e
