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
using System.Net;
using System.IO;
using System.Collections;
using AbstractedSheep.ShuttleTrackerWorld;

namespace AbstractedSheep.ShuttleTrackerService
{
    class Program
    {
        static void Main(string[] args)
        {
            World world = World.GenerateWorld(JsonHelper.ParseNetlink("http://shuttles.rpi.edu/displays/netlink.js"));
            EtaCalculator etaCalc = new EtaCalculator(world);

            Coordinate ep1 = new Coordinate(37760814, -77030853);
            Coordinate ep2 = new Coordinate(40366681, -123921572);
            Coordinate c = new Coordinate(55891119, -92376489);
            c = c.ClosestPoint(ep1, ep2);
            Console.WriteLine(c.ToString());

            Console.WriteLine("Press any key to continue...");
            Console.ReadKey();
            //while (true)
            //{
			//	etaCalc.Recalculate();
            //}
        }
    }
}
