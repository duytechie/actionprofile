# Vagrant 2.4.9's bundled Ruby can report refused macOS connections as open.
# Check the actual socket state so collision detection still rejects busy ports.
# https://github.com/hashicorp/vagrant/issues/13845
require "socket"
require "vagrant/util/is_port_open"

module Vagrant
  module Util
    module IsPortOpen
      def is_port_open?(host, port)
        Socket.tcp(host, port, connect_timeout: 0.1) do |socket|
          socket.getsockopt(Socket::SOL_SOCKET, Socket::SO_ERROR).int.zero? &&
            socket.remote_address.ip_port == port
        end
      rescue Errno::ETIMEDOUT, Errno::ECONNREFUSED, Errno::EHOSTUNREACH,
             Errno::ENETUNREACH, Errno::EACCES, Errno::ENOTCONN,
             Errno::EALREADY, Errno::EINVAL
        false
      end
    end
  end
end
