# CI/CD Pipeline test for a Java app

## Prerequisite  

Create an `.env` file first with these variables:  

```sh
GHCR_USERNAME=
GHCR_TOKEN=
MYSQL_ROOT_PASSWORD=
RABBITMQ_DEFAULT_USER=
RABBITMQ_DEFAULT_PASS=
```

Use Compose dotenv syntax; quote values containing spaces or `#`. Provisioning
requires all five variables to be nonempty and uses the token through stdin.

Start the VM with an installed Vagrant provider:

```sh
vagrant up --provider=vmware_desktop
# Or, with VirtualBox installed:
vagrant up --provider=virtualbox
```

VMware Fusion requires the `vagrant-vmware-desktop` plugin and Vagrant VMware
Utility. Both providers allocate 4 CPUs and 4 GiB of memory.

Provisioning installs Docker and Compose, authenticates to GHCR, waits for
the containers to start, and checks both HTTP endpoints. Access the reverse
proxy at <http://localhost:8080> and the app at <http://localhost:8081>.

VirtualBox binds these forwards to localhost and reports replacement ports if
either requested port is already in use. VMware ignores localhost bindings on
NAT forwards, so its NAT forwards are disabled. With VMware, run this command
in a separate terminal and leave it open while using the app:

```sh
vagrant ssh -- -N -o ExitOnForwardFailure=yes -L 127.0.0.1:8080:127.0.0.1:80 -L 127.0.0.1:8081:127.0.0.1:8080
```

On macOS, a local socket check workaround handles Vagrant 2.4.9's false port
collision reports while continuing to detect ports that are actually in use.

To rerun provisioning after a configuration change:

```sh
vagrant provision
```

