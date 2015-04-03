/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.internal.processors.spi;

import org.apache.ignite.cluster.*;
import org.apache.ignite.internal.*;
import org.apache.ignite.internal.processors.*;
import org.apache.ignite.internal.util.typedef.*;
import org.apache.ignite.internal.util.typedef.internal.*;
import org.apache.ignite.spi.*;
import org.jetbrains.annotations.*;

import static org.apache.ignite.internal.IgniteNodeAttributes.*;

/**
 * Node validator.
 */
public class OsNodeValidator extends GridProcessorAdapter implements NodeValidator {
    /**
     * @param ctx Kernal context.
     */
    public OsNodeValidator(GridKernalContext ctx) {
        super(ctx);
    }

    /** {@inheritDoc} */
    @Nullable @Override public IgniteSpiNodeValidationResult validateNode(ClusterNode node) {
        ClusterNode locNode = ctx.discovery().localNode();

        // Check version.
        String locBuildVer = locNode.attribute(ATTR_BUILD_VER);
        String rmtBuildVer = node.attribute(ATTR_BUILD_VER);

        if (!F.eq(rmtBuildVer, locBuildVer)) {
            // OS nodes don't support rolling updates.
            if (!locBuildVer.equals(rmtBuildVer)) {
                String errMsg = "Local node and remote node have different version numbers " +
                    "(node will not join, Ignite does not support rolling updates, " +
                    "so versions must be exactly the same) " +
                    "[locBuildVer=" + locBuildVer + ", rmtBuildVer=" + rmtBuildVer +
                    ", locNodeAddrs=" + U.addressesAsString(locNode) +
                    ", rmtNodeAddrs=" + U.addressesAsString(node) +
                    ", locNodeId=" + locNode.id() + ", rmtNodeId=" + node.id() + ']';

                LT.warn(log, null, errMsg);

                // Always output in debug.
                if (log.isDebugEnabled())
                    log.debug(errMsg);

                return new IgniteSpiNodeValidationResult(node.id(), errMsg, errMsg);
            }
        }

        return null;
    }
}
