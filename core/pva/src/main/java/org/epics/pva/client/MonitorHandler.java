/*******************************************************************************
 * Copyright (c) 2019 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.epics.pva.client;

import java.nio.ByteBuffer;

import org.epics.pva.common.CommandHandler;
import org.epics.pva.common.PVAHeader;

/** Handle a server's MONITOR reply
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
class MonitorHandler implements CommandHandler<ClientTCPHandler>
{
    @Override
    public byte getCommand()
    {
        return PVAHeader.CMD_MONITOR;
    }

    @Override
    public void handleCommand(final ClientTCPHandler tcp, final ByteBuffer buffer) throws Exception
    {
        // Dispatch to the initiating MonitorRequest
        if (buffer.remaining() < 4)
            throw new Exception("Incomplete Monitor Response");
        final int request_id = buffer.getInt(buffer.position());

        final ResponseHandler handler = tcp.getResponseHandler(request_id);
        if (handler == null)
            throw new Exception("Received unsolicited Monitor Response for request " + request_id);
        handler.handleResponse(buffer);
        tcp.markAlive();
    }
}
