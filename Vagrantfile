Vagrant.configure("2") do |config|
  config.vm.box = "bento/ubuntu-24.04"
  config.vm.box_version = "202510.26.0"

  config.vm.provider "virtualbox" do |virtualbox|
    virtualbox.memory = 4096
    virtualbox.cpus = 4
  end

  # Reach the reverse proxy at http://localhost:8080 and the app directly at
  # http://localhost:8081 from the host machine.
  config.vm.network "forwarded_port", guest: 80, host: 8080, auto_correct: true
  config.vm.network "forwarded_port", guest: 8080, host: 8081, auto_correct: true

  config.vm.provision "shell", privileged: true, inline: <<-SHELL
    set -e
    export DEBIAN_FRONTEND=noninteractive

    apt-get update
    apt-get install -y docker.io docker-compose-v2 ripgrep

    systemctl enable --now docker
    usermod -aG docker vagrant

    # The project is available through Vagrant's default synced folder.
    cd /vagrant
    export $(grep -v '^#' .env | xargs)
    echo $GHCR_TOKEN | docker login ghcr.io -u $GHCR_USERNAME --password-stdin
    docker compose up --build --detach
  SHELL
end
