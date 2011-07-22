/*
 * Copyright 2011
 *
 *   This file is part of Mobile Shuttle Tracker.
 *
 *   Mobile Shuttle Tracker is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Mobile Shuttle Tracker is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Mobile Shuttle Tracker.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.abstractedsheep.ShuttleTrackerService;

import com.abstractedsheep.world.*;

import java.util.ArrayList;

public class ETACalculator {
    private World world;
    private ArrayList<Eta> etaList;

    public class Eta {
        public int shuttleId;
        public int routeId;
        public int time;
        public long arrivalTime;
        public String stopId;
        public String stopName;
        public int Id;

        /**
         * @param shuttleId
         * @param routeId
         * @param time
         * @param stopId
         */
        public Eta(int shuttleId, int routeId, int time, String stopId,
                   String stopName, int etaId) {
            this.shuttleId = shuttleId;
            this.stopName = stopName;
            this.Id = etaId;
            this.routeId = routeId;
            this.time = time;
            this.stopId = stopId;
            this.arrivalTime = (System.currentTimeMillis() + time) / 1000L;
        }

    }

    public ETACalculator() {
        world = null;
        etaList = new ArrayList<Eta>();
    }

    public ArrayList<Eta> getETAs() {
        return this.etaList;
    }

    public void updateWorld(World world2) {
        this.world = world2;
        this.calculatateETAs();
    }

    private void calculatateETAs() {
        this.etaList.clear();

        for (Shuttle shuttle : world.getShuttleList().values()) {
            Route rt = shuttle.getCurrentRoute();
            int size = rt.getCoordinateList().size();
            for (Stop stop : rt.getStopList().values()) {
                int i = shuttle.getNextRouteCoordinate();
                int j;
                double distance;
                int stopPrecedingCoordinate = stop.getPrecedingCoordinate()
                        .get(rt.getIdNum());

                // If the shuttle and stop are between the same two route
                // points, the distance is
                // simply the distance from the shuttle to the stop
                if ((shuttle.getNextRouteCoordinate() == 0 && stopPrecedingCoordinate == size - 1)
                        || (shuttle.getNextRouteCoordinate() == stopPrecedingCoordinate + 1)) {
                    distance = shuttle.getCurrentLocation()
                            .distanceFromCoordiante(shuttle.getClosestPoint());
                } else {
                    // Start with the distance from the shuttle to the next
                    // route point
                    distance = shuttle.getCurrentLocation()
                            .distanceFromCoordiante(
                                    (rt.getCoordinateList().get(shuttle
                                            .getNextRouteCoordinate())));

                    // Go through each route point until we reach the one just
                    // before
                    // the stop and sum the distances
                    while (i != stopPrecedingCoordinate) {

                        if (i == size)
                            j = 0;
                        else
                            j = i + 1;

                        // TODO: use the nextStopDistance map to remove the need
                        // to recalculate
                        // the distance each time. Check execution times for
                        // both. Dictionary lookup
                        // may actually be slower than recalculating
                        Coordinate c1 = rt.getCoordinateList().get(i);
                        Coordinate c2 = rt.getCoordinateList().get(j);

                        distance += c1.distanceFromCoordiante(c2);

                        i++;
                        if (i == size)
                            i = 0;
                    }

                    // Finish by adding the distance from the stop to the last
                    // route point that the shuttle
                    // will go through
                    distance += stop.getPrecedingCoordinateDistance().get(
                            rt.getIdNum());
                }
                int time = (int) (distance / shuttle.getSpeed());
                this.etaList.add(new Eta(shuttle.getShuttleId(), shuttle
                        .getCurrentRoute().getIdNum(), time, stop
                        .getShortName(), stop.getName(), 0));
            }
        }
    }

}
