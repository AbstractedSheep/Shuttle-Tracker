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
using System.Collections;
using System.IO;
using Newtonsoft.Json;
using System.Net;
using AbstractedSheep.ShuttleTrackerWorld;

namespace AbstractedSheep.ShuttleTrackerService
{
    class JsonHelper
    {
        /// <summary>
        /// Parses the Netlink JSON from a given URL.
        /// </summary>
        /// <param name="url">The URL pointing to the Netlink JSON file.</param>
        /// <returns>A Netlink object constructed from the Netlink JSON.</returns>
        public static Netlink ParseNetlink(string url)
        {
            HttpWebRequest req = (HttpWebRequest)WebRequest.Create(url);
            HttpWebResponse res = (HttpWebResponse)req.GetResponse();

            JsonTextReader reader = new JsonTextReader(new StreamReader(res.GetResponseStream()));
            JsonSerializer jc = new JsonSerializer();

            return jc.Deserialize<Netlink>(reader);
        }
    }
}
