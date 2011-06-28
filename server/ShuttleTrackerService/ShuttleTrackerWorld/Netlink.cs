/* Copyright 2011 Austin Wagner
 *     
 * This file is part of Mobile Shuttle Tracker.
 *
 *  Mobile Shuttle Tracker is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Mobile Shuttle Tracker is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Mobile Shuttle Tracker.  If not, see <http://www.gnu.org/licenses/>.  
 */

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Newtonsoft.Json;

namespace AbstractedSheep.ShuttleTrackerWorld
{
    public class Netlink
    {
        public List<StopJson> stops { get; set; }
        public List<RouteJson> routes { get; set; }
    }

    public class StopJson
    {
        public decimal latitude { get; set; }
        public decimal longitude { get; set; }
        public string name { get; set; }
        public string short_name { get; set; }
        public List<StopRouteJson> routes { get; set; }      
    }

    public class StopRouteJson
    {
        public int id { get; set; }
        public string name { get; set; }
    }

    public class RouteJson
    {
        public string color { get; set; }
        public int id { get; set; }
        public string name { get; set; }
        public int width { get; set; }
        public List<RouteCoordinateJson> coords { get; set; }       
    }

    public class RouteCoordinateJson
    {
        public decimal latitude { get; set; }
        public decimal longitude { get; set; }
    }
}
