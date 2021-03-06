/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.lealone.p2p.gossip.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lealone.common.logging.Logger;
import org.lealone.common.logging.LoggerFactory;
import org.lealone.net.NetNode;
import org.lealone.p2p.gossip.Gossiper;
import org.lealone.p2p.gossip.NodeState;
import org.lealone.p2p.gossip.protocol.GossipDigest;
import org.lealone.p2p.gossip.protocol.GossipDigestAck;
import org.lealone.p2p.gossip.protocol.GossipDigestAck2;
import org.lealone.p2p.gossip.protocol.P2pPacketIn;
import org.lealone.p2p.gossip.protocol.P2pPacketOut;
import org.lealone.p2p.server.MessagingService;

public class GossipDigestAckHandler implements P2pPacketHandler<GossipDigestAck> {

    private static final Logger logger = LoggerFactory.getLogger(GossipDigestAckHandler.class);

    @Override
    public void handle(P2pPacketIn<GossipDigestAck> packetIn, int id) {
        NetNode from = packetIn.from;
        if (logger.isTraceEnabled())
            logger.trace("Received a GossipDigestAckMessage from {}", from);
        if (!Gossiper.instance.isEnabled() && !Gossiper.instance.isInShadowRound()) {
            if (logger.isTraceEnabled())
                logger.trace("Ignoring GossipDigestAckMessage because gossip is disabled");
            return;
        }

        GossipDigestAck gDigestAckMessage = packetIn.packet;
        List<GossipDigest> gDigestList = gDigestAckMessage.getGossipDigestList();
        Map<NetNode, NodeState> epStateMap = gDigestAckMessage.getNodeStateMap();

        if (logger.isTraceEnabled())
            logger.trace("Received ack with {} digests and {} states", gDigestList.size(), epStateMap.size());

        if (epStateMap.size() > 0) {
            /* Notify the Failure Detector */
            Gossiper.instance.notifyFailureDetector(epStateMap);
            Gossiper.instance.applyStateLocally(epStateMap);
        }

        if (Gossiper.instance.isInShadowRound()) {
            if (logger.isDebugEnabled())
                logger.debug("Finishing shadow round with {}", from);
            Gossiper.instance.finishShadowRound();
            return; // don't bother doing anything else, we have what we came for
        }

        /* Get the state required to send to this gossipee - construct GossipDigestAck2Message */
        Map<NetNode, NodeState> deltaEpStateMap = new HashMap<>();
        for (GossipDigest gDigest : gDigestList) {
            NetNode addr = gDigest.getNode();
            NodeState localEpStatePtr = Gossiper.instance.getStateForVersionBiggerThan(addr, gDigest.getMaxVersion());
            if (localEpStatePtr != null)
                deltaEpStateMap.put(addr, localEpStatePtr);
        }

        P2pPacketOut<GossipDigestAck2> gDigestAck2Message = new P2pPacketOut<>(new GossipDigestAck2(deltaEpStateMap));
        if (logger.isTraceEnabled())
            logger.trace("Sending a GossipDigestAck2Message to {}", from);
        MessagingService.instance().sendOneWay(gDigestAck2Message, from);
    }
}
