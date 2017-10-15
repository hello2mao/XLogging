package com.hello2mao.xlogging.urlconnection;

import java.io.FileDescriptor;
import java.net.InetAddress;

public class SocketDescriptor {

    /**
     * The file descriptor object for this socket.
     */
    private FileDescriptor fd;

    /**
     * The IP address of the remote end of this socket.
     */
    private InetAddress address;

    /**
     * The port number on the remote host to which this socket is connected.
     */
    private int port;

    /**
     * The local port number to which this socket is connected.
     */
    private int localPort;

    public SocketDescriptor(FileDescriptor fd, InetAddress address, int port, int localPort) {
        this.fd = fd;
        this.address = address;
        this.port = port;
        this.localPort = localPort;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SocketDescriptor that = (SocketDescriptor) o;

        if (port != that.port) return false;
        if (localPort != that.localPort) return false;
        if (fd != null ? !fd.equals(that.fd) : that.fd != null) return false;
        return address != null ? address.equals(that.address) : that.address == null;

    }

    @Override
    public int hashCode() {
        int result = fd != null ? fd.hashCode() : 0;
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + port;
        result = 31 * result + localPort;
        return result;
    }
}
