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
package com.djrapitops.plan.gathering.timed;

import com.djrapitops.plan.gathering.ServerSensor;
import com.djrapitops.plan.gathering.SystemUsage;
import com.djrapitops.plan.gathering.domain.builders.TPSBuilder;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.transactions.events.TPSStoreTransaction;
import com.djrapitops.plan.utilities.analysis.Average;
import com.djrapitops.plan.utilities.analysis.Maximum;
import com.djrapitops.plan.utilities.analysis.TimerAverage;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

/**
 * TPSCounter extension for game server platforms.
 *
 * @author Rsl1122
 */
@Singleton
public class ProxyTPSCounter extends TPSCounter {

    private final ServerSensor<Object> serverSensor;
    private final DBSystem dbSystem;
    private final ServerInfo serverInfo;
    private Maximum.ForInteger playersOnline;
    private Average cpu;
    private TimerAverage ram;

    @Inject
    public ProxyTPSCounter(
            ServerSensor<Object> serverSensor,
            DBSystem dbSystem,
            ServerInfo serverInfo,
            PluginLogger logger,
            ErrorHandler errorHandler
    ) {
        super(logger, errorHandler);

        this.serverSensor = serverSensor;
        this.dbSystem = dbSystem;
        this.serverInfo = serverInfo;
        playersOnline = new Maximum.ForInteger(0);
        cpu = new Average();
        ram = new TimerAverage();
    }

    @Override
    public void pulse() {
        long time = System.currentTimeMillis();
        boolean shouldSave = ram.add(time, SystemUsage.getUsedMemory());
        playersOnline.add(serverSensor.getOnlinePlayerCount());
        cpu.add(SystemUsage.getAverageSystemLoad());
        if (shouldSave) save(time);
    }

    private void save(long time) {
        long timeLastMinute = time - TimeUnit.MINUTES.toMillis(1L);
        int maxPlayers = playersOnline.getMaxAndReset();
        double averageCPU = cpu.getAverageAndReset();
        long averageRAM = (long) ram.getAverageAndReset(time);
        long freeDiskSpace = getFreeDiskSpace();

        dbSystem.getDatabase().executeTransaction(new TPSStoreTransaction(
                serverInfo.getServerUUID(),
                TPSBuilder.get()
                        .date(timeLastMinute)
                        .playersOnline(maxPlayers)
                        .usedCPU(averageCPU)
                        .usedMemory(averageRAM)
                        .freeDiskSpace(freeDiskSpace)
                        .toTPS()
        ));
    }
}
