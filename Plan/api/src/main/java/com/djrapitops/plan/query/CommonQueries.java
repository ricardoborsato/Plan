/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.query;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Class that allows performing most commonly wanted queries.
 * <p>
 * This exists so that SQL does not necessarily need to be written.
 * Obtain an instance from {@link QueryService}.
 *
 * @author Rsl1122
 */
public interface CommonQueries {

    /**
     * Fetch playtime of a player on a server.
     * <p>
     * Returns 0 for any non existing players or servers.
     *
     * @param playerUUID UUID of the player.
     * @param serverUUID UUID of the Plan server.
     * @param after      Data after this Epoch ms should be fetched
     * @param before     Data before this Epoch ms should be fetched
     * @return Milliseconds the player has played with the defined parameters.
     */
    long fetchPlaytime(UUID playerUUID, UUID serverUUID, long after, long before);

    /**
     * Fetch last seen Epoch ms for a player on a server.
     *
     * @param playerUUID UUID of the player.
     * @param serverUUID UUID of the Plan server.
     * @return Epoch ms the player was last seen, 0 if player has not played on server.
     */
    long fetchLastSeen(UUID playerUUID, UUID serverUUID);

    Set<UUID> fetchServerUUIDs();

    Optional<UUID> fetchUUIDOf(String playerName);

    Optional<String> fetchNameOf(UUID playerUUID);

    boolean doesDBHaveTable(String table);

    boolean doesDBHaveTableColumn(String table, String column);
}
