using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Shuttle_Tracker_Service
{
    class ShuttleJson
    {
        public long last_update_time { get; set; }
        public double latitude { get; set; }
        public double longitude { get; set; }
    }
}
