require_relative "scripts/vagrant_port_check" if RUBY_PLATFORM.include?("darwin")

Vagrant.configure("2") do |config|
  config.vm.box = "bento/ubuntu-24.04"
  config.vm.box_version = "202510.26.0"

  config.vm.provider "virtualbox" do |virtualbox|
    virtualbox.memory = 4096
    virtualbox.cpus = 4
  end

  config.vm.provider "vmware_desktop" do |vmware, override|
    vmware.vmx["memsize"] = "4096"
    vmware.vmx["numvcpus"] = "4"
    vmware.ssh_info_public = true

    # VMware ignores host_ip on NAT forwards. Use the VM's private IP for SSH
    # and explicit localhost SSH tunnels for web access (see README.md).
    override.vm.network "forwarded_port", id: "web", guest: 80, host: 8080, disabled: true
    override.vm.network "forwarded_port", id: "app", guest: 8080, host: 8081, disabled: true
    override.vm.network "forwarded_port", id: "ssh", guest: 22, host: 2222, disabled: true
  end

  # Reach the reverse proxy at http://localhost:8080 and the app directly at
  # http://localhost:8081 from the host machine (VMware uses README's tunnels).
  config.vm.network "forwarded_port", id: "web", guest: 80, host: 8080, host_ip: "127.0.0.1", auto_correct: true
  config.vm.network "forwarded_port", id: "app", guest: 8080, host: 8081, host_ip: "127.0.0.1", auto_correct: true

  config.vm.provision "shell", privileged: true, inline: <<-'SHELL'
    set -euo pipefail
    export DEBIAN_FRONTEND=noninteractive

    # The project is available through Vagrant's default synced folder.
    cd /vagrant
    if [ ! -r .env ]; then
      echo "Missing readable /vagrant/.env; see README.md for required variables." >&2
      exit 1
    fi

    apt-get update
    apt-get install -y docker.io docker-compose-v2 ripgrep python3 curl

    systemctl enable --now docker
    usermod -aG docker vagrant

    # Let Compose parse and validate dotenv values without evaluating shell code
    # or splitting credentials on spaces. The validation service is never run.
    python3 - <<'PYTHON'
import json
import subprocess
import sys

required = (
    "GHCR_USERNAME", "GHCR_TOKEN", "MYSQL_ROOT_PASSWORD",
    "RABBITMQ_DEFAULT_USER", "RABBITMQ_DEFAULT_PASS",
)
validation_config = "services:\n  credentials:\n    image: scratch\n    environment:\n"
for name in required:
    validation_config += f"      {name}: ${{{name}:?{name} must be set in .env}}\n"
result = subprocess.run(
    ["docker", "compose", "--env-file", ".env", "-f", "-", "config", "--format", "json"],
    input=validation_config, text=True, capture_output=True,
)
if result.returncode:
    sys.stderr.write(result.stderr)
    sys.exit(result.returncode)
credentials = json.loads(result.stdout)["services"]["credentials"]["environment"]
# Compose escapes literal dollars when serializing a reusable configuration.
credentials = {name: value.replace("$$", "$") for name, value in credentials.items()}
subprocess.run(
    ["docker", "login", "ghcr.io", "--username", credentials["GHCR_USERNAME"], "--password-stdin"],
    input=credentials["GHCR_TOKEN"] + "\n", text=True, check=True,
)
PYTHON

    docker compose --env-file .env config --quiet
    docker compose --env-file .env up --build --detach --wait --wait-timeout 300

    # A running container can still serve errors while Tomcat starts or fails.
    for url in http://127.0.0.1/ http://127.0.0.1:8080/; do
      curl --fail --silent --show-error --retry 30 --retry-all-errors \
        --retry-delay 2 --max-time 10 --output /dev/null "$url"
    done
  SHELL
end
