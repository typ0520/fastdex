/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fastdex.build.lib.fd;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.TimeoutException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Locale;

import fastdex.common.fd.ProtocolConstants;

/**
 * Wrapper for talking to either the hotswap service or the run-as service.
 */
public class ServiceCommunicator {

    private static final String LOCAL_HOST = "127.0.0.1";


    private final String mPackageName;


    private final ILogger mLogger;

    private final int mLocalPort;

    public ServiceCommunicator(String packageName) {
        this(packageName,new NullLogger(),46628);
    }

    public ServiceCommunicator(String packageName, int port) {
        this(packageName,new NullLogger(),port);
    }

    public ServiceCommunicator(String packageName, ILogger logger, int port) {
        mPackageName = packageName;
        mLogger = logger;
        mLocalPort = port;
    }

    public int getLocalPort() {
        return mLocalPort;
    }


    public <T> T talkToService(IDevice device, Communicator<T> communicator) throws IOException {
        try {
            device.createForward(mLocalPort, mPackageName, IDevice.DeviceUnixSocketNamespace.ABSTRACT);
        } catch (TimeoutException e) {
            throw new IOException(e);
        } catch (AdbCommandRejectedException e2) {
            throw new IOException(e2);
        }
        try {
            return talkToServiceWithinPortForward(communicator, mLocalPort);
        } finally {
            try {
                device.removeForward(mLocalPort, mPackageName,
                        IDevice.DeviceUnixSocketNamespace.ABSTRACT);
            } catch (IOException | TimeoutException | AdbCommandRejectedException e) {
                // we don't worry that much about failures while removing port forwarding
                mLogger.warning("Exception while removing port forward: " + e);
            }
        }
    }

    private static <T> T talkToServiceWithinPortForward(Communicator<T> communicator, int localPort) throws IOException {
        Socket socket = new Socket(LOCAL_HOST, localPort);
        DataInputStream input = new DataInputStream(socket.getInputStream());
        DataOutputStream output = new DataOutputStream(socket.getOutputStream());
        output.writeLong(ProtocolConstants.PROTOCOL_IDENTIFIER);
        output.writeInt(ProtocolConstants.PROTOCOL_VERSION);

        socket.setSoTimeout(2 * 1000); // Allow up to 2 seconds before timing out
        int version = input.readInt();
        if (version != ProtocolConstants.PROTOCOL_VERSION) {
            String msg = String.format(Locale.US, "Client and server protocol versions don't match (%1$d != %2$d)", version, ProtocolConstants.PROTOCOL_VERSION);
            throw new IOException(msg);
        }
        socket.setSoTimeout(communicator.getTimeout());
        T value = communicator.communicate(input, output);
        output.writeInt(ProtocolConstants.MESSAGE_EOF);
        return value;
    }
}
